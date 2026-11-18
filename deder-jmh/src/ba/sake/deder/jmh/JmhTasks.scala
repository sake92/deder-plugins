package ba.sake.deder.jmh

import ba.sake.deder.*
import ba.sake.deder.config.DederProject.ModuleType
import ba.sake.deder.deps.Dependency
import ba.sake.deder.plugins.Jmh
import scala.jdk.CollectionConverters.*

private[jmh] final case class JmhSettings(enabled: Boolean, version: String, jvmOptions: Seq[String])

private[jmh] object JmhSettings {
  def forModule(moduleId: String, config: Jmh.JmhPluginConfig): JmhSettings = {
    val defaults = config.defaults
    val overrideConfig = Option(config.modules.get(moduleId))
    JmhSettings(
      overrideConfig.flatMap(v => Option(v.enabled).map(_.booleanValue())).getOrElse(defaults.enabled),
      overrideConfig.flatMap(v => Option(v.jmhVersion)).getOrElse(defaults.jmhVersion),
      overrideConfig.flatMap(v => Option(v.jvmOptions).map(_.asScala.toSeq)).getOrElse(defaults.jvmOptions.asScala.toSeq)
    )
  }
}

private[jmh] object JmhTasks {
  def runTask(name: String, list: Boolean, config: Jmh.JmhPluginConfig, core: CoreTasksApi): AbstractTask[String] =
    TaskBuilder.make[String](
      name = name,
      singleton = true,
      supportedModuleTypes = Set(ModuleType.JAVA, ModuleType.SCALA),
      category = "JMH"
    )
      .dependsOn(core.compileTask)
      .dependsOn(core.runClasspathTask)
      .dependsOn(core.javaHomeTask)
      .build { ctx =>
        val (compiled, classpath, javaHome) = ctx.depResults
        val settings = JmhSettings.forModule(ctx.module.id, config)
        require(settings.enabled, s"JMH is disabled for module ${ctx.module.id}")
        require(compiled.success, s"Compilation failed for module ${ctx.module.id}")
        val expectedCore = s"jmh-core-${settings.version}.jar"
        require(classpath.exists(_.last == expectedCore),
          s"Add org.openjdk.jmh:jmh-core:${settings.version} to module ${ctx.module.id} dependencies")
        val dependency = Dependency.make(s"org.openjdk.jmh:jmh-generator-bytecode:${settings.version}", "3.9.0")
        val generator = ctx.dependencyResolver.fetchFiles(Seq(dependency), Some(ctx.notifications))
        val work = ctx.out / "jmh"
        os.remove.all(work)
        val sources = work / "sources"
        val resources = work / "resources"
        val classes = work / "classes"
        Seq(sources, resources, classes).foreach(os.makeDir.all(_))
        val toolClasspath = (classpath ++ generator).distinct.map(_.toString).mkString(java.io.File.pathSeparator)
        def jdkTool(tool: String): String = javaHome.map(home => (home / "bin" / tool).toString).getOrElse(tool)
        def run(command: Seq[String]): Unit = {
          val result = os.proc(command).call(cwd = DederGlobals.projectRootDir, stderr = os.Pipe, check = false)
          if result.exitCode != 0 then
            throw RuntimeException(s"JMH preparation failed with exit code ${result.exitCode}: ${result.err.text()} ${result.out.text()}")
        }
        run(Seq(jdkTool("java")) ++ settings.jvmOptions ++ Seq("-cp", toolClasspath,
          "org.openjdk.jmh.generators.bytecode.JmhBytecodeGenerator", compiled.classesDir.absPath.toString,
          sources.toString, resources.toString, "default"))
        val javaSources = os.walk(sources).filter(p => os.isFile(p) && p.ext == "java")
        if javaSources.nonEmpty then
          run(Seq(jdkTool("javac"), "-proc:none", "-cp", toolClasspath, "-d", classes.toString) ++ javaSources.map(_.toString))
        val runtimeClasspath = (Seq(classes, resources) ++ classpath).distinct.map(_.toString).mkString(java.io.File.pathSeparator)
        val args = if list then Seq("-l") ++ ctx.args else ctx.args
        val command = Seq(jdkTool("java")) ++ settings.jvmOptions ++ Seq("-cp", runtimeClasspath, "org.openjdk.jmh.Main") ++ args
        ctx.notifications.add(ServerNotification.RunSubprocess(command, Map.empty, ctx.watch))
        "JMH ready"
      }
}
