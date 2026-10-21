package ba.sake.deder.flyway

import ba.sake.deder.{*, given}
import ba.sake.deder.config.DederProject.ModuleType
import ba.sake.deder.deps.Dependency
import ba.sake.deder.plugins.Flyway

import java.lang.reflect.InvocationTargetException
import java.net.URLClassLoader

object FlywayTasks {
  private val supportedModuleTypes: Set[ModuleType] = Set(ModuleType.JAVA, ModuleType.SCALA)

  /** Flyway commands exposed as tasks: task name -> `org.flywaydb.core.Flyway` method. */
  val commands: Seq[(String, String)] = Seq(
    "flywayMigrate" -> "migrate",
    "flywayClean" -> "clean",
    "flywayInfo" -> "info",
    "flywayValidate" -> "validate",
    "flywayBaseline" -> "baseline",
    "flywayRepair" -> "repair"
  )

  def all(config: Flyway.FlywayPluginConfig, runClasspathTask: AbstractTask[Seq[os.Path]]): Seq[AbstractTask[?]] =
    commands.map { case (taskName, method) => task(config, runClasspathTask, taskName, method) }

  private def task(
      config: Flyway.FlywayPluginConfig,
      runClasspathTask: AbstractTask[Seq[os.Path]],
      taskName: String,
      method: String
  ): AbstractTask[String] =
    TaskBuilder
      .make[String](
        name = taskName,
        supportedModuleTypes = supportedModuleTypes,
        category = "Flyway"
      )
      .dependsOn(runClasspathTask)
      .build { ctx =>
        val moduleClasspath = ctx.depResults
        val resolved = FlywayConfigNormalizer.normalize(ctx.module.id, config)
        require(resolved.enabled, s"flyway plugin is disabled for module ${ctx.module.id}")
        validate(resolved, ctx.module.id, method)
        val flywayDeps = Dependency.make(s"org.flywaydb:flyway-core:${resolved.flywayVersion}", "2.13.16") +:
          resolved.jdbcDeps.map(Dependency.make(_, "2.13.16"))
        val flywayClasspath = ctx.dependencyResolver.fetchFiles(flywayDeps, Some(ctx.notifications))
        ctx.notifications.add(ServerNotification.logInfo(s"Running $taskName for ${ctx.module.id}"))
        val output = run(flywayClasspath ++ moduleClasspath, resolved, method)
        if output.nonEmpty then ctx.notifications.add(ServerNotification.logInfo(output))
        output
      }

  def validate(config: ResolvedFlywayConfig, moduleId: String, method: String): Unit = {
    require(config.url.nonEmpty, s"Flyway url is required for module '$moduleId'")
    require(
      !(method == "clean" && config.cleanDisabled),
      s"flywayClean is disabled for module '$moduleId', set `cleanDisabled = false` to allow it"
    )
  }

  /** Relative `filesystem:` locations are resolved against the project root, not the server's working dir. */
  def resolveLocation(location: String, root: os.Path): String = {
    val prefix = "filesystem:"
    if location.startsWith(prefix) then {
      val path = location.stripPrefix(prefix)
      prefix + os.FilePath(path).resolveFrom(root).toString
    } else location
  }

  private def run(classpath: Seq[os.Path], config: ResolvedFlywayConfig, method: String): String = {
    val thread = Thread.currentThread()
    val previousLoader = thread.getContextClassLoader
    val loader = new URLClassLoader(classpath.map(_.toNIO.toUri.toURL).toArray, ClassLoader.getPlatformClassLoader)
    try {
      thread.setContextClassLoader(loader)
      try {
        val flywayClass = loader.loadClass("org.flywaydb.core.Flyway")
        var conf = flywayClass.getMethod("configure").invoke(null)
        def call(name: String, argTypes: Seq[Class[?]], args: Seq[AnyRef]): AnyRef = {
          val m = conf.getClass.getMethod(name, argTypes*)
          conf = m.invoke(conf, args*)
          conf
        }
        val strings = classOf[Array[String]]
        call("classLoader", Seq(classOf[ClassLoader]), Seq(loader))
        call(
          "dataSource",
          Seq(classOf[String], classOf[String], classOf[String]),
          Seq(config.url, config.user, config.password)
        )
        val locations = config.locations.map(resolveLocation(_, DederGlobals.projectRootDir)).toArray
        call("locations", Seq(strings), Seq(locations))
        if config.schemas.nonEmpty then call("schemas", Seq(strings), Seq(config.schemas.toArray))
        call("cleanDisabled", Seq(classOf[Boolean]), Seq(Boolean.box(config.cleanDisabled)))
        val flyway = conf.getClass.getMethod("load").invoke(conf)
        val result = flyway.getClass.getMethod(method).invoke(flyway)
        if method == "info" then formatInfo(result) else describe(method, result)
      } catch {
        case error: InvocationTargetException => throw error.getCause
      }
    } finally {
      thread.setContextClassLoader(previousLoader)
      loader.close()
    }
  }

  private def describe(method: String, result: AnyRef): String =
    method match {
      case "migrate" =>
        val count = member(result, "migrationsExecuted")
        val target = member(result, "targetSchemaVersion")
        s"Flyway migrate: $count migration(s) executed, schema version: ${Option(target).getOrElse("<none>")}"
      case "validate" => "Flyway validate: OK"
      case other      => s"Flyway $other finished"
    }

  private def formatInfo(infoService: AnyRef): String = {
    val all = invoke(infoService, "all").asInstanceOf[Array[AnyRef]]
    val rows = all.toSeq.map { info =>
      val state = invoke(invoke(info, "getState"), "getDisplayName")
      Seq(
        String.valueOf(Option(invoke(info, "getVersion")).getOrElse("")),
        String.valueOf(invoke(info, "getDescription")),
        String.valueOf(state)
      )
    }
    val header = Seq("Version", "Description", "State")
    val table = header +: rows
    val widths = header.indices.map(i => table.map(_(i).length).max)
    table
      .map(row => row.zip(widths).map { case (cell, w) => cell.padTo(w, ' ') }.mkString(" | ").trim)
      .mkString("\n")
  }

  private def invoke(target: AnyRef, name: String): AnyRef =
    if target == null then null
    else {
      val method = target.getClass.getMethod(name)
      method.setAccessible(true)
      method.invoke(target)
    }

  /** Flyway result objects expose public fields, older/newer versions may expose getters instead. */
  private def member(target: AnyRef, name: String): AnyRef =
    if target == null then null
    else
      try target.getClass.getField(name).get(target)
      catch {
        case _: NoSuchFieldException => invoke(target, "get" + name.capitalize)
      }
}
