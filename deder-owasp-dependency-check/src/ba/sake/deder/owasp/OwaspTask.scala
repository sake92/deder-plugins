package ba.sake.deder.owasp

import ba.sake.deder.{*, given}
import ba.sake.deder.config.DederProject.ModuleType
import ba.sake.deder.deps.Dependency
import ba.sake.deder.plugins.Owasp
import scala.jdk.CollectionConverters.*
import java.nio.channels.FileChannel
import java.nio.file.StandardOpenOption

private[owasp] case class ScanConfig(enabled: Boolean, version: String, formats: Seq[String], threshold: Option[Double], suppressions: Seq[String], dataDirectory: String, extraArgs: Seq[String])

private[owasp] object OwaspTask {
  def normalize(id: String, config: Owasp.OwaspPluginConfig): ScanConfig = {
    val defaults = config.defaults
    val module = Option(config.modules.get(id))
    def list(f: Owasp.ModuleOverride => java.util.List[String], default: java.util.List[String]): Seq[String] =
      module.flatMap(m => Option(f(m))).getOrElse(default).asScala.toSeq
    val result = ScanConfig(
      module.flatMap(m => Option(m.enabled).map(_.booleanValue)).getOrElse(defaults.enabled),
      module.flatMap(m => Option(m.dependencyCheckVersion)).getOrElse(defaults.dependencyCheckVersion),
      list(_.formats, defaults.formats),
      module.flatMap(m => Option(m.failOnCvss)).orElse(Option(defaults.failOnCvss)).map(_.doubleValue),
      list(_.suppressionFiles, defaults.suppressionFiles),
      module.flatMap(m => Option(m.dataDirectory)).getOrElse(defaults.dataDirectory),
      list(_.extraArgs, defaults.extraArgs)
    )
    require(result.version.trim.nonEmpty, "Dependency-Check version must not be empty")
    require(result.formats.nonEmpty, "At least one report format is required")
    require(result.threshold.forall(x => x >= 0 && x <= 10), "failOnCvss must be between 0 and 10")
    val reserved = Set("--scan", "-s", "--out", "-o", "--data", "-d", "--format", "-f", "--failOnCVSS", "--project", "--updateonly", "--purge", "--nvdApiKey")
    require(!result.extraArgs.exists(arg => reserved.contains(arg.takeWhile(_ != '='))), "extraArgs contains an option managed by the OWASP plugin")
    result
  }

  def arguments(config: ScanConfig, module: String, jars: Seq[os.Path], report: os.Path, data: os.Path): Seq[String] = {
    val scans = jars.distinct.flatMap(path => Seq("--scan", path.toString))
    val formats = config.formats.flatMap(format => Seq("--format", format))
    val threshold = config.threshold.toSeq.flatMap(value => Seq("--failOnCVSS", value.toString))
    val suppressions = config.suppressions.flatMap { path =>
      Seq("--suppression", os.FilePath(path).resolveFrom(DederGlobals.projectRootDir).toString)
    }
    Seq("--project", module, "--out", report.toString, "--data", data.toString) ++ scans ++ formats ++ threshold ++ suppressions ++ config.extraArgs
  }

  def make(config: Owasp.OwaspPluginConfig, classpathTask: AbstractTask[Seq[os.Path]]): AbstractTask[String] =
    TaskBuilder.make[String](name = "owaspDependencyCheck", supportedModuleTypes = Set(ModuleType.JAVA, ModuleType.SCALA), category = "OWASP")
      .dependsOn(classpathTask)
      .build { ctx =>
        val settings = normalize(ctx.module.id, config)
        require(settings.enabled, s"OWASP dependency check is disabled for module ${ctx.module.id}")
        val jars = ctx.depResults._1.filter(p => os.isFile(p) && p.last.endsWith(".jar"))
        require(jars.nonEmpty, s"No runtime dependency jars found for module ${ctx.module.id}")
        val dependency = Dependency.make(s"org.owasp:dependency-check-cli:${settings.version}", "2.13.16")
        val toolClasspath = ctx.dependencyResolver.fetchFiles(Seq(dependency), Some(ctx.notifications))
        val data = os.FilePath(settings.dataDirectory).resolveFrom(DederGlobals.projectRootDir) / settings.version
        val report = ctx.out / "reports"
        os.makeDir.all(data)
        os.makeDir.all(report)
        val args = arguments(settings, ctx.module.id, jars, report, data)
        val key = sys.env.get("NVD_API_KEY").filter(_.nonEmpty).toSeq.flatMap(k => Seq("--nvdApiKey", k))
        val cmd = Seq("java", "-cp", toolClasspath.mkString(java.io.File.pathSeparator), "org.owasp.dependencycheck.App") ++ args ++ key
        val channel = FileChannel.open((data / ".deder.lock").toNIO, StandardOpenOption.CREATE, StandardOpenOption.WRITE)
        try {
          // File locks are process-wide; serialize threads in this JVM as well.
          OwaspTask.synchronized {
            val lock = channel.lock()
            try {
              val result = os.proc(cmd).call(cwd = DederGlobals.projectRootDir, stdout = os.Inherit, stderr = os.Inherit, check = false)
              if result.exitCode != 0 then throw new RuntimeException(s"OWASP Dependency-Check exited with code ${result.exitCode}; reports: $report")
            } finally lock.release()
          }
        } finally channel.close()
        s"OWASP Dependency-Check reports: $report"
      }
}
