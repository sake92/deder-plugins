package ba.sake.deder.squery

import ba.sake.deder.{*, given}
import ba.sake.deder.config.DederProject.ModuleType
import ba.sake.deder.deps.Dependency
import ba.sake.deder.plugins.Squery

import java.lang.reflect.InvocationTargetException
import java.net.URLClassLoader

object SqueryGenerationTask {
  private val supportedModuleTypes = Set(ModuleType.SCALA, ModuleType.SCALA_TEST)

  def make(config: Squery.SqueryPluginConfig): AbstractTask[DederPath] =
    TaskBuilder
      .make[DederPath](
        name = "squeryGenerate",
        supportedModuleTypes = supportedModuleTypes,
        category = "Squery",
        kind = TaskKind.SourceGenerator
      )
      .build { ctx =>
        val sourceOut = ctx.out / "sources"
        val resolved = SqueryConfigNormalizer.normalize(ctx.module.id, config)
        os.makeDir.all(sourceOut)

        if resolved.enabled then {
          require(resolved.jdbcUrl.nonEmpty, s"Squery jdbcUrl is required for module '${ctx.module.id}'")
          require(resolved.schemaMappings.nonEmpty, s"Squery schemaMappings are required for module '${ctx.module.id}'")

          val cli = Dependency.make(s"ba.sake:squery-cli_2.13:${resolved.squeryVersion}", "2.13.16")
          val jdbcDeps = resolved.jdbcDeps.map(Dependency.make(_, "2.13.16"))
          val classpath = ctx.dependencyResolver.fetchFiles(cli +: jdbcDeps, Some(ctx.notifications))
          ctx.notifications.add(ServerNotification.logInfo(s"Generating Squery sources for ${ctx.module.id}"))
          run(classpath, arguments(resolved, sourceOut))
        }

        DederPath(sourceOut)
      }

  def arguments(config: SqueryConfig, sourceOut: os.Path): Seq[String] = {
    def repeated(name: String, values: Seq[String]): Seq[String] =
      values.flatMap(value => Seq(name, value))

    Seq(
      "--jdbcUrl", config.jdbcUrl,
      "--baseFolder", sourceOut.toString,
      "--colNameIdentifierMapper", config.colNameIdentifierMapper,
      "--typeNameMapper", config.typeNameMapper,
      "--rowTypeSuffix", config.rowTypeSuffix,
      "--daoTypeSuffix", config.daoTypeSuffix
    ) ++
      repeated("--schemaMappings", config.schemaMappings.map { case (schema, pkg) => s"$schema:$pkg" }) ++
      repeated("--typeMappingRule", config.typeMappingRules) ++
      repeated("--includeTables", if config.includeTables.isEmpty then Seq(".*") else config.includeTables) ++
      repeated("--excludeTables", config.excludeTables)
  }

  private def run(classpath: Seq[os.Path], args: Seq[String]): Unit = {
    val thread = Thread.currentThread()
    val previousLoader = thread.getContextClassLoader
    val loader = new URLClassLoader(classpath.map(_.toNIO.toUri.toURL).toArray, ClassLoader.getPlatformClassLoader)
    try {
      thread.setContextClassLoader(loader)
      try loader.loadClass("ba.sake.squery.cli.SqueryMain").getMethod("main", classOf[Array[String]])
          .invoke(null, args.toArray)
      catch {
        case error: InvocationTargetException => throw error.getCause
      }
    } finally {
      thread.setContextClassLoader(previousLoader)
      loader.close()
    }
  }
}
