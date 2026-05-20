package ba.sake.deder.protobuf.sources

import ba.sake.deder.DederGlobals
import ba.sake.deder.ServerNotificationsLogger
import ba.sake.deder.config.DederProject.{DederModule, ScalaModule}
import ba.sake.deder.deps.{Dependency, DependencyResolverApi}
import ba.sake.deder.protobuf.config.{ResolvedImportConfig, ResolvedSourceSetConfig}
import scala.jdk.CollectionConverters.*
import scala.util.Try

final case class ResolvedProtoInputs(
    localProtoFiles: Seq[os.Path],
    importRoots: Seq[os.Path]
)

object ProtoInputResolver {

  def moduleRoot(module: DederModule): os.Path =
    if module.root == "." then DederGlobals.projectRootDir
    else DederGlobals.projectRootDir / os.RelPath(module.root)

  def localSourceRoots(moduleRoot: os.Path, sourceSet: ResolvedSourceSetConfig): Seq[os.Path] =
    sourceSet.sourceDirs
      .map(resolvePath(moduleRoot, _))
      .filter(path => os.exists(path) && os.isDir(path))
      .distinct

  def localSourceFiles(moduleRoot: os.Path, sourceSet: ResolvedSourceSetConfig): Seq[os.Path] =
    localSourceRoots(moduleRoot, sourceSet).flatMap(root =>
      os.walk(root)
        .filter(os.isFile)
        .filter(_.ext == "proto")
        .filter { file =>
          val rel = file.subRelativeTo(root).toString
          GlobFilter.matches(rel, sourceSet.includeGlobs, sourceSet.excludeGlobs)
        }
    ).distinct.sortBy(_.toString)

  def declaredInputFiles(
      moduleRoot: os.Path,
      sourceSet: ResolvedSourceSetConfig,
      imports: ResolvedImportConfig
  ): Seq[os.Path] = {
    val localSources = localSourceFiles(moduleRoot, sourceSet)
    val importPathFiles = localImportPathFiles(moduleRoot, imports.paths ++ sourceSet.extraImportPaths)
    (localSources ++ importPathFiles).distinct.sortBy(_.toString)
  }

  def resolveGenerationInputs(
      module: DederModule,
      sourceSet: ResolvedSourceSetConfig,
      imports: ResolvedImportConfig,
      projectDependencies: Seq[Dependency],
      dependencyResolver: DependencyResolverApi,
      notifications: ServerNotificationsLogger,
      workspace: os.Path
  ): ResolvedProtoInputs = {
    val moduleRootPath = moduleRoot(module)
    val localRoots = localSourceRoots(moduleRootPath, sourceSet)
    val localFiles = localSourceFiles(moduleRootPath, sourceSet)
    val extraPathRoots = resolvePathImports(
      moduleRootPath,
      imports.paths ++ sourceSet.extraImportPaths,
      workspace / "path-imports"
    )
    val configuredDepFiles = fetchConfiguredDependencyFiles(
      imports.dependencyArtifacts ++ sourceSet.extraImportDeps,
      module,
      dependencyResolver,
      notifications
    )
    val projectDepFiles =
      if imports.includeProjectDependencies then dependencyResolver.fetchFiles(projectDependencies, Some(notifications))
      else Seq.empty
    val extractedConfiguredImports = ArchiveProtoExtractor.extractArchives(configuredDepFiles, workspace / "configured-dep-imports")
    val extractedProjectImports = ArchiveProtoExtractor.extractArchives(projectDepFiles, workspace / "project-dep-imports")

    ResolvedProtoInputs(
      localProtoFiles = localFiles,
      importRoots = (localRoots ++ extraPathRoots ++ extractedConfiguredImports ++ extractedProjectImports).distinct
    )
  }

  private def resolvePathImports(moduleRoot: os.Path, configuredPaths: Seq[String], workspace: os.Path): Seq[os.Path] =
    configuredPaths.flatMap { pathStr =>
      val resolved = resolvePath(moduleRoot, pathStr)
      if os.exists(resolved) && os.isDir(resolved) then Seq(resolved)
      else if os.exists(resolved) && os.isFile(resolved) && resolved.ext == "proto" then Seq(resolved / os.up)
      else if os.exists(resolved) && os.isFile(resolved) then ArchiveProtoExtractor.extractArchives(Seq(resolved), workspace)
      else Seq.empty
    }.distinct

  private def localImportPathFiles(moduleRoot: os.Path, configuredPaths: Seq[String]): Seq[os.Path] =
    configuredPaths.flatMap { pathStr =>
      val resolved = resolvePath(moduleRoot, pathStr)
      if os.exists(resolved) && os.isDir(resolved) then
        os.walk(resolved).filter(path => os.isFile(path) && path.ext == "proto")
      else if os.exists(resolved) && os.isFile(resolved) then
        Seq(resolved)
      else Seq.empty
    }

  private def fetchConfiguredDependencyFiles(
      declarations: Seq[String],
      module: DederModule,
      dependencyResolver: DependencyResolverApi,
      notifications: ServerNotificationsLogger
  ): Seq[os.Path] = {
    val scalaVersion = module match {
      case sm: ScalaModule => sm.scalaVersion
      case _               => "3.7.4"
    }
    val dependencies = declarations.distinct.map(declaration => Dependency.make(declaration, scalaVersion))
    if dependencies.isEmpty then Seq.empty
    else dependencyResolver.fetchFiles(dependencies, Some(notifications))
  }

  private def resolvePath(moduleRoot: os.Path, pathStr: String): os.Path =
    Try(moduleRoot / os.RelPath(pathStr))
      .orElse(Try(os.Path(pathStr)))
      .getOrElse(throw IllegalArgumentException(s"Invalid protobuf path '$pathStr'"))
}
