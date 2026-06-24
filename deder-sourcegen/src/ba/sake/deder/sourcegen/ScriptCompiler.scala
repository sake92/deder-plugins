package ba.sake.deder.sourcegen

import ba.sake.deder.ServerNotificationsLogger
import ba.sake.deder.deps.{Dependency, DependencyResolverApi}

object ScriptCompiler:

  /** Compiles the given Scala source files using the provided classpath.
    * Returns Left(errorMessage) on compilation failure, Right(()) on success.
    */
  def compile(
      sourceFiles: Seq[os.Path],
      classpath: String,
      outputDir: os.Path,
      notifications: ServerNotificationsLogger
  ): Either[String, Unit] =
    if sourceFiles.isEmpty then Right(())
    else
      val sourceArgs = sourceFiles.map(_.toString)
      val allArgs = Array(
        "-classpath", classpath,
        "-d", outputDir.toString,
        "-usejavacp"
      ) ++ sourceArgs

      val driver = new dotty.tools.dotc.Driver
      val reporter = driver.process(allArgs)

      if reporter.hasErrors then
        val errors = reporter.allErrors.map(e => s"${e.pos}: ${e.message}").mkString("\n")
        Left(s"Compilation failed:\n$errors")
      else Right(())

  /** Resolves scala3-compiler and scala3-library JARs for the given Scala version. */
  def resolveCompilerClasspath(
      resolver: DependencyResolverApi,
      scalaVersion: String,
      notifications: ServerNotificationsLogger
  ): Seq[os.Path] =
    val compilerDep = Dependency.make(s"org.scala-lang::scala3-compiler:$scalaVersion", scalaVersion)
    resolver.fetchFiles(Seq(compilerDep), Some(notifications))
