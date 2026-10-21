package ba.sake.deder.flyway

import ba.sake.deder.{*, given}
import ba.sake.deder.config.DederProject.ModuleType
import ba.sake.deder.deps.Dependency
import ba.sake.deder.plugins.Flyway

import org.flywaydb.core.api.MigrationInfo
import org.flywaydb.core.{Flyway => FlywayCore}

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
        val jdbcDeps = resolved.jdbcDeps.map(Dependency.make(_, "2.13.16"))
        val jdbcClasspath =
          if jdbcDeps.isEmpty then Seq.empty
          else ctx.dependencyResolver.fetchFiles(jdbcDeps, Some(ctx.notifications))
        ctx.notifications.add(ServerNotification.logInfo(s"Running $taskName for ${ctx.module.id}"))
        val output = run(jdbcClasspath ++ moduleClasspath, resolved, method)
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
    // JDBC drivers, Flyway database modules and classpath migrations are found through this loader
    val loader = new URLClassLoader(classpath.map(_.toNIO.toUri.toURL).toArray, getClass.getClassLoader)
    try {
      val builder = FlywayCore
        .configure(loader)
        .dataSource(config.url, config.user, config.password)
        .locations(config.locations.map(resolveLocation(_, DederGlobals.projectRootDir))*)
        .cleanDisabled(config.cleanDisabled)
      if config.schemas.nonEmpty then builder.schemas(config.schemas*)
      val flyway = builder.load()
      method match {
        case "migrate" =>
          val result = flyway.migrate()
          s"Flyway migrate: ${result.migrationsExecuted} migration(s) executed, schema version: ${Option(result.targetSchemaVersion).getOrElse("<none>")}"
        case "clean"    => flyway.clean(); "Flyway clean finished"
        case "info"     => formatInfo(flyway.info().all().toSeq)
        case "validate" => flyway.validate(); "Flyway validate: OK"
        case "baseline" => flyway.baseline(); "Flyway baseline finished"
        case "repair"   => flyway.repair(); "Flyway repair finished"
        case other      => throw new IllegalArgumentException(s"Unknown Flyway command: $other")
      }
    } finally loader.close()
  }

  private def formatInfo(infos: Seq[MigrationInfo]): String = {
    val header = Seq("Version", "Description", "State")
    val rows = infos.map { info =>
      Seq(Option(info.getVersion).map(_.toString).getOrElse(""), info.getDescription, info.getState.getDisplayName)
    }
    val table = header +: rows
    val widths = header.indices.map(i => table.map(_(i).length).max)
    table
      .map(row => row.zip(widths).map { case (cell, w) => cell.padTo(w, ' ') }.mkString(" | ").trim)
      .mkString("\n")
  }
}
