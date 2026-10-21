package ba.sake.deder.springboot

import ba.sake.deder.{*, given}
import ba.sake.deder.config.DederProject.{ModuleType, ScalaModule}
import ba.sake.deder.deps.Dependency
import ba.sake.deder.plugins.Springboot

object SpringBootTasks {

  private val supportedModuleTypes: Set[ModuleType] = Set(ModuleType.JAVA, ModuleType.SCALA)

  /** Builds an executable Spring Boot jar (layout understood by Spring Boot's `JarLauncher`). */
  def jarTask(
      config: Springboot.SpringBootPluginConfig,
      runClasspathTask: AbstractTask[Seq[os.Path]],
      finalMainClassTask: AbstractTask[Option[String]]
  ): AbstractTask[DederPath] =
    CachedTaskBuilder
      .make[DederPath](
        name = "springBootJar",
        supportedModuleTypes = supportedModuleTypes,
        category = "SpringBoot"
      )
      .dependsOn(runClasspathTask)
      .dependsOn(finalMainClassTask)
      .build { ctx =>
        val (classpath, moduleMainClass) = ctx.depResults
        val resolved = SpringBootConfigNormalizer.normalize(ctx.module.id, config)
        require(resolved.enabled, s"spring-boot plugin is disabled for module ${ctx.module.id}")
        val mainClass = resolved.mainClass
          .orElse(moduleMainClass)
          .getOrElse(
            throw new IllegalArgumentException(
              s"No main class found for module ${ctx.module.id}, set `mainClass` in the spring-boot plugin config or in the module"
            )
          )
        val scalaVersion = ctx.module match {
          case m: ScalaModule => m.scalaVersion
          case _              => "3"
        }
        val loaderJar = ctx.dependencyResolver.fetchFile(
          Dependency.make(s"org.springframework.boot:spring-boot-loader:${resolved.springBootVersion}", scalaVersion)
        )
        val target = ctx.out / s"${ctx.module.id}-spring-boot.jar"
        SpringBootPackager.pack(target, classpath, loaderJar, mainClass, resolved.springBootVersion)
        DederPath(target)
      }

  /** Runs the bundled jar with `java -jar`, i.e. through Spring Boot's launcher and classloader. */
  def runTask(
      config: Springboot.SpringBootPluginConfig,
      jarTask: AbstractTask[DederPath],
      javaHomeTask: AbstractTask[Option[os.Path]]
  ): AbstractTask[String] =
    TaskBuilder
      .make[String](
        name = "springBootRun",
        supportedModuleTypes = supportedModuleTypes,
        category = "SpringBoot"
      )
      .dependsOn(jarTask)
      .dependsOn(javaHomeTask)
      .build { ctx =>
        val (jar, javaHome) = ctx.depResults
        val resolved = SpringBootConfigNormalizer.normalize(ctx.module.id, config)
        val java = javaHome.map(home => (home / "bin" / "java").toString).getOrElse("java")
        val cmd = Seq(java) ++ resolved.jvmOptions ++ Seq("-jar", jar.absPath.toString) ++ ctx.args
        val result = os
          .proc(cmd)
          .call(
            cwd = DederGlobals.projectRootDir,
            stdin = os.Inherit,
            stdout = os.Inherit,
            stderr = os.Inherit,
            check = false
          )
        if result.exitCode != 0 then
          throw new RuntimeException(s"Spring Boot app exited with code ${result.exitCode}")
        s"Spring Boot app finished: ${jar.absPath}"
      }
}
