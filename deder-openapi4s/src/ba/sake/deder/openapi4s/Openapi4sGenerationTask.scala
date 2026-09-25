package ba.sake.deder.openapi4s

import ba.sake.deder.{*, given}
import ba.sake.deder.config.DederProject.{DederModule, ModuleType, ScalaModule}
import ba.sake.deder.deps.Dependency
import ba.sake.deder.plugins.Openapi4s
import scala.jdk.CollectionConverters.*

private[openapi4s] object Openapi4sGenerationTask:
  private val mainClass = "ba.sake.openapi4s.cli.OpenApi4sMain"
  private val moduleTypes = Set(ModuleType.SCALA, ModuleType.SCALA_JS, ModuleType.SCALA_NATIVE)

  def make(config: Openapi4s.Openapi4sPluginConfig): AbstractTask[String] =
    TaskBuilder
      .make[String](name = "openapi4sGenerate", supportedModuleTypes = moduleTypes, category = "OpenAPI4s")
      .build { ctx =>
        val settings = settingsFor(ctx.module, config)
        if !settings.enabled then "OpenAPI4s generation disabled"
        else
          val root = moduleRoot(ctx.module)
          val input = resolve(root, settings.input.getOrElse(defaultInput(ctx.module)))
          val target = resolve(root, settings.targetDir.getOrElse(defaultTarget(ctx.module)))
          require(settings.basePackage.nonEmpty, s"OpenAPI4s basePackage must be set for module ${ctx.module.id}")
          require(os.exists(input), s"OpenAPI4s input does not exist: $input")
          os.makeDir.all(target)

          val scalaVersion = ctx.module.asInstanceOf[ScalaModule].scalaVersion
          // The CLI runs in a separate JVM and its jars never enter the consumer compile classpath.
          val cliJars = ctx.dependencyResolver.fetchFiles(
            Seq(Dependency.make(s"ba.sake:openapi4s-cli_2.13:${settings.version}", scalaVersion)),
            Some(ctx.notifications)
          )
          val args = Seq(
            "--models", settings.models,
            "--url", input.toNIO.toUri.toString,
            "--baseFolder", target.toString,
            "--basePackage", settings.basePackage,
            "--validation", settings.validation
          ) ++ optional("--framework", settings.framework) ++
            optional("--client", settings.client) ++
            (if settings.tags.isEmpty then Seq.empty else Seq("--tags", settings.tags.mkString(",")))
          val command = Seq(
            sys.props.getOrElse("java.home", "") + "/bin/java",
            "-cp", cliJars.map(_.toString).mkString(java.io.File.pathSeparator), mainClass
          ) ++ args
          ctx.notifications.add(ServerNotification.logInfo(s"Generating OpenAPI4s sources in $target"))
          val result = os.proc(command).call(cwd = root, stdout = os.Pipe, stderr = os.Pipe, check = false, timeout = 600000)
          val stdout = result.out.text().trim
          if stdout.nonEmpty then stdout.linesIterator.foreach(line => ctx.notifications.add(ServerNotification.logInfo(line)))
          val stderr = result.err.text().trim
          if stderr.nonEmpty then stderr.linesIterator.foreach(line => ctx.notifications.add(ServerNotification.logWarning(line)))
          if result.exitCode != 0 then throw RuntimeException(s"OpenAPI4s exited with code ${result.exitCode}")
          s"Generated OpenAPI4s sources in $target"
      }

  private case class Settings(
      enabled: Boolean,
      basePackage: String,
      models: String,
      framework: String,
      client: String,
      validation: String,
      tags: Seq[String],
      input: Option[String],
      targetDir: Option[String],
      version: String
  )

  private def settingsFor(module: DederModule, config: Openapi4s.Openapi4sPluginConfig): Settings =
    val defaults = config.defaults
    val overrideConfig = Option(config.modules.get(module.id))
    def value(f: Openapi4s.ModuleOverride => String, default: String): String =
      overrideConfig.flatMap(v => Option(f(v))).orElse(Option(default)).getOrElse("").trim
    Settings(
      enabled = overrideConfig.flatMap(v => Option(v.enabled).map(_.booleanValue())).getOrElse(defaults.enabled),
      basePackage = value(_.basePackage, defaults.basePackage),
      models = value(_.models, defaults.models),
      framework = value(_.framework, defaults.framework),
      client = value(_.client, defaults.client),
      validation = value(_.validation, defaults.validation),
      tags = overrideConfig.flatMap(v => Option(v.tags)).getOrElse(defaults.tags).asScala.toSeq,
      input = Option(value(_.input, defaults.input)).filter(_.nonEmpty),
      targetDir = Option(value(_.targetDir, defaults.targetDir)).filter(_.nonEmpty),
      version = value(_.version, defaults.version)
    )

  private def optional(flag: String, value: String): Seq[String] =
    if value.isEmpty || value == "none" then Seq.empty else Seq(flag, value)

  private def moduleRoot(module: DederModule): os.Path =
    if module.root == "." || module.root.isEmpty then DederGlobals.projectRootDir
    else DederGlobals.projectRootDir / os.RelPath(module.root)

  private def defaultInput(module: DederModule): String =
    val resources = module.asInstanceOf[ScalaModule].resources.asScala.headOption.getOrElse("resources")
    s"$resources/openapi.json"

  private def defaultTarget(module: DederModule): String =
    module.sources.asScala.headOption.getOrElse("src")

  private def resolve(root: os.Path, value: String): os.Path =
    os.Path(value, root)
