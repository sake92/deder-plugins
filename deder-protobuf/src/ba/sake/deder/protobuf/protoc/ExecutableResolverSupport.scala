package ba.sake.deder.protobuf.protoc

import ba.sake.deder.TaskExecContext
import ba.sake.deder.config.DederProject.{DederModule, ScalaModule}
import ba.sake.deder.deps.Dependency
import ba.sake.deder.protobuf.config.ResolverSpec
import scala.util.Try

private[protoc] object ExecutableResolverSupport {

  def resolveExecutable[T, Deps <: Tuple](
      ctx: TaskExecContext[T, Deps],
      spec: ResolverSpec,
      moduleRoot: os.Path,
      workspace: os.Path,
      defaultCommand: String
  ): ResolvedExecutable =
    spec.kind match {
      case "system-path" =>
        ResolvedExecutable.command(spec.command)
      case "path" =>
        ResolvedExecutable.command(resolveProjectPath(moduleRoot, spec.path.getOrElse(
          throw IllegalArgumentException(s"Resolver kind 'path' requires a path for $defaultCommand")
        )).toString)
      case "binary-maven" =>
        val classifier = PlatformClassifier.current()
        val dependency = spec.dependency.getOrElse(
          throw IllegalArgumentException(s"Resolver kind 'binary-maven' requires a dependency for $defaultCommand")
        )
        val fetched = BinaryMavenDependency.fetchFile(
          project = ctx.project,
          dependency = BinaryMavenDependency.toCoursierDependency(dependency, scalaVersion(ctx.module), classifier),
          notifications = ctx.notifications
        )
        val executable = materializeExecutable(fetched, spec.executableSubpath, workspace / defaultCommand)
        ensureExecutable(executable)
        ResolvedExecutable.command(executable.toString)
      case "jvm-maven" =>
        val dependency = spec.dependency.getOrElse(
          throw IllegalArgumentException(s"Resolver kind 'jvm-maven' requires a dependency for $defaultCommand")
        )
        val mainClass = spec.mainClass.getOrElse(
          throw IllegalArgumentException(s"Resolver kind 'jvm-maven' requires a mainClass for $defaultCommand")
        )
        val jars = ctx.dependencyResolver.fetchFiles(Seq(Dependency.make(dependency, scalaVersion(ctx.module))), Some(ctx.notifications))
        val wrapper = createJvmWrapper(
          workspace = workspace,
          wrapperName = defaultCommand,
          classpath = jars,
          mainClass = mainClass,
          jvmArgs = spec.jvmArgs
        )
        ResolvedExecutable.command(wrapper.toString)
      case other =>
        throw IllegalArgumentException(s"Unsupported resolver kind '$other'")
    }

  private def scalaVersion(module: DederModule): String =
    module match {
      case m: ScalaModule => m.scalaVersion
      case _              => "3.7.4"
    }

  private def materializeExecutable(
      fetched: os.Path,
      executableSubpath: Option[String],
      destination: os.Path
  ): os.Path =
    executableSubpath match {
      case None => fetched
      case Some(entryPath) =>
        if !Set("jar", "zip").contains(fetched.ext.toLowerCase) then
          throw IllegalArgumentException(s"Configured executableSubpath '$entryPath' but fetched file is not an archive: $fetched")
        val zip = java.util.zip.ZipFile(fetched.toIO)
        try {
          val entry = Option(zip.getEntry(entryPath)).getOrElse(
            throw IllegalArgumentException(s"Archive '$fetched' does not contain '$entryPath'")
          )
          os.makeDir.all(destination / os.up)
          val stream = zip.getInputStream(entry)
          try os.write.over(destination, stream.readAllBytes(), createFolders = true)
          finally stream.close()
          destination
        } finally zip.close()
    }

  private def createJvmWrapper(
      workspace: os.Path,
      wrapperName: String,
      classpath: Seq[os.Path],
      mainClass: String,
      jvmArgs: Seq[String]
  ): os.Path = {
    val wrapper = workspace / wrapperName
    val classpathText = classpath.map(_.toString).mkString(java.io.File.pathSeparator)
    val command = Seq("java") ++ jvmArgs ++ Seq("-cp", classpathText, mainClass, "\"$@\"")
    val script =
      s"""#!/usr/bin/env bash
         |set -euo pipefail
         |exec ${command.mkString(" ")}
         |""".stripMargin
    os.write.over(wrapper, script, createFolders = true)
    ensureExecutable(wrapper)
    wrapper
  }

  private def ensureExecutable(path: os.Path): Unit =
    if os.exists(path) then
      Try(path.toIO.setExecutable(true, false)).getOrElse(false)

  private def resolveProjectPath(moduleRoot: os.Path, pathStr: String): os.Path =
    Try(moduleRoot / os.RelPath(pathStr))
      .orElse(Try(os.Path(pathStr)))
      .getOrElse(throw IllegalArgumentException(s"Invalid executable path '$pathStr'"))
}
