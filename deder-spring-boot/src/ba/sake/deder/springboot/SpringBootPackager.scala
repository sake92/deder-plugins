package ba.sake.deder.springboot

import org.springframework.boot.loader.tools.{Libraries, Library, LibraryScope, Repackager}

import java.io.File

/** Thin wrapper over Spring Boot's `spring-boot-loader-tools` `Repackager`, which writes the executable jar layout
  * (`BOOT-INF/classes`, `BOOT-INF/lib`, `classpath.idx`, launcher classes, `JarLauncher` manifest).
  *
  * The launcher classes are bundled inside `spring-boot-loader-tools`, so its version determines the launcher version.
  */
object SpringBootPackager {

  def pack(target: os.Path, classpath: Seq[os.Path], workDir: os.Path, mainClass: String): Unit = {
    val classesDirs = classpath.filter(os.isDir(_)).distinct
    val libJars = classpath.filter(p => os.isFile(p) && p.last.endsWith(".jar")).distinct

    // Repackager works on an existing jar, so zip the application classes and resources first
    val appJar = workDir / "app-classes.jar"
    os.makeDir.all(workDir)
    os.remove.all(appJar)
    os.zip(appJar, classesDirs.map(dir => os.zip.ZipSource.fromPathTuple((dir, os.sub))))

    val repackager = new Repackager(appJar.toIO)
    repackager.setMainClass(mainClass)
    repackager.setBackupSource(false)

    val libraries: Libraries = callback => libJars.foreach(jar => callback.library(new Library(jar.toIO, LibraryScope.RUNTIME)))
    os.makeDir.all(target / os.up)
    os.remove.all(target)
    repackager.repackage(target.toIO, libraries)
  }
}
