package ba.sake.deder.sourcegen

import ba.sake.deder.{*, given}
import ba.sake.deder.config.DederProject.{DederModule, ModuleType, ScalaModule, ScalaTestModule}
import ba.sake.deder.deps.Dependency
import ba.sake.deder.plugins.Sourcegen

object SourcegenGenerationTasks:

  // Set.empty means "all module types" (JAVA, SCALA, SCALA_JS, SCALA_NATIVE, and their test variants)
  private val allModuleTypes: Set[ModuleType] = Set.empty

  def scriptSourceFilesTask(config: Sourcegen.SourcegenPluginConfig): SourceFilesTask =
    SourceFilesTask(
      name = "sourcegenDiscoverScripts",
      execute = { ctx =>
        val resolved = SourcegenConfigNormalizer.normalize(ctx.module, config)
        if !resolved.enabled then Seq.empty
        else
          val moduleRoot = os.Path(ctx.module.root)
          val scriptsDir = moduleRoot / os.RelPath(resolved.scriptsDir)
          if os.exists(scriptsDir) then
            os.walk(scriptsDir)
              .filter(_.ext == "scala")
              .map(f => DederPath(f))
          else Seq.empty
      },
      supportedModuleTypes = allModuleTypes,
      description = "Discovered sourcegen .scala scripts for the module",
      category = "Sourcegen"
    )

  def sourceGeneratorTask(
      config: Sourcegen.SourcegenPluginConfig,
      scriptsTask: SourceFilesTask,
      allDependenciesTask: AbstractTask[Seq[Dependency]]
  ): AbstractTask[DederPath] =
    CachedTaskBuilder
      .make[DederPath](
        name = "sourcegenGenerate",
        supportedModuleTypes = allModuleTypes,
        category = "Sourcegen",
        kind = TaskKind.SourceGenerator
      )
      .dependsOn(scriptsTask)
      .dependsOn(allDependenciesTask)
      .build { ctx =>
        val (scriptPaths, allDeps) = ctx.depResults
        val resolved = SourcegenConfigNormalizer.normalize(ctx.module, config)

        if !resolved.enabled || scriptPaths.isEmpty then DederPath(ctx.out)
        else
          ctx.notifications.add(ServerNotification.logInfo(s"Sourcegen: discovered ${scriptPaths.size} script(s)"))

          // Resolve user-specified generator dependencies
          val scalaVer = resolved.scalaVersion.getOrElse {
            // Fall back to the module's own Scala version if not specified
            ctx.module match
              case m: ScalaTestModule => m.scalaVersion
              case m: ScalaModule => m.scalaVersion
              case _ => "3.7.4"
          }

          val userDeps = resolved.deps.map(d => Dependency.make(d, scalaVer))
          val generatorJars = ctx.dependencyResolver.fetchFiles(userDeps, Some(ctx.notifications))
          val compilerJars = ScriptCompiler.resolveCompilerClasspath(ctx.dependencyResolver, scalaVer, ctx.notifications)

          // Compile scripts
          val compiledOut = ctx.out / "classes"
          os.makeDir.all(compiledOut)

          val scriptFiles = scriptPaths.map(p => os.Path(p.path, os.pwd))
          val fullClasspath = (generatorJars ++ compilerJars).map(_.toString).mkString(":")

          ScriptCompiler.compile(scriptFiles, fullClasspath, compiledOut, ctx.notifications) match
            case Left(error) =>
              ctx.notifications.add(ServerNotification.logError(error))
              throw new RuntimeException(s"Sourcegen script compilation failed: $error")
            case Right(_) =>
              ctx.notifications.add(ServerNotification.logInfo("Sourcegen: compilation successful"))

          // Prepare output directory and write config
          val sourceOut = ctx.out / "sources"
          os.makeDir.all(sourceOut)

          val moduleRoot = ctx.module.root
          val moduleTypeStr = ctx.module.`type`.toString.toLowerCase
          val configFile = ScriptRunner.writeConfig(
            sourceOut, resolved, ctx.module.id, moduleRoot, scalaVer, moduleTypeStr
          )

          // Run scripts
          val runClasspath = fullClasspath + ":" + compiledOut.toString
          ScriptRunner.runScripts(compiledOut, runClasspath, configFile, ctx.notifications)

          DederPath(sourceOut)
      }
