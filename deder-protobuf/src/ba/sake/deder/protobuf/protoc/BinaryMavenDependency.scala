package ba.sake.deder.protobuf.protoc

import ba.sake.deder.{ServerNotificationsLogger, ServerNotification}
import ba.sake.deder.config.DederProject
import ba.sake.deder.deps.Dependency
import coursierapi.Fetch
import coursierapi.{MavenRepository as CsMavenRepository, Repository as CsRepository}
import dependency.api.ops.*

import scala.jdk.CollectionConverters.*

object BinaryMavenDependency {

  def toCoursierDependency(
      declaration: String,
      scalaVersion: String,
      classifier: PlatformClassifier
  ): coursierapi.Dependency =
    Dependency
      .make(declaration, scalaVersion)
      .applied
      .toCs
      .withClassifier(classifier.classifier)
      .withType(classifier.binaryExtension)
      .withTransitive(false)

  def fetchFile(
      project: DederProject,
      dependency: coursierapi.Dependency,
      notifications: ServerNotificationsLogger
  ): os.Path = {
    val cache = coursierapi.Cache.create().withLogger(new SimpleCoursierLogger(notifications))
    val classifier = PlatformClassifier(dependency.getClassifier, dependency.getType)
    val fetch = newFetch(dependency, classifier).withCache(cache)
    assembleRepositories(project).foreach(repo => fetch.addRepositories(repo))
    val files = fetch.fetchResult().getFiles.asScala.toSeq.map(file => os.Path(file.toPath))
    files.headOption.getOrElse {
      throw IllegalStateException(s"Failed to fetch binary artifact for ${dependency.getModule}:${dependency.getVersion}")
    }
  }

  def newFetch(
      dependency: coursierapi.Dependency,
      classifier: PlatformClassifier
  ): Fetch =
    Fetch
      .create()
      .withDependencies(dependency)
      .addClassifiers(classifier.classifier)
      .addArtifactTypes(classifier.binaryExtension)

  private def assembleRepositories(project: DederProject): Seq[CsRepository] = {
    val userRepos = project.repositories.asScala.toSeq.map(repo => CsMavenRepository.of(repo.url))
    if project.includeDefaultRepos then
      val m2local = CsMavenRepository.of(s"file://${os.home}/.m2/repository")
      userRepos ++ Seq(m2local) ++ CsRepository.defaults().asScala.toSeq
    else userRepos
  }

  private final class SimpleCoursierLogger(notifications: ServerNotificationsLogger) extends coursierapi.SimpleLogger {
    override def starting(url: String): Unit =
      notifications.add(ServerNotification.logInfo(s"Download started: $url"))
    override def progress(url: String, downloaded: Long): Unit = ()
    override def done(url: String, success: Boolean): Unit =
      notifications.add(ServerNotification.logInfo(s"Download ${if success then "completed" else "failed"}: $url"))
  }
}
