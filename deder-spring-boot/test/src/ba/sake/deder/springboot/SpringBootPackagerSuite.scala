package ba.sake.deder.springboot

import munit.FunSuite

import java.util.jar.JarFile
import java.util.zip.{ZipEntry, ZipOutputStream}
import scala.jdk.CollectionConverters.*

class SpringBootPackagerSuite extends FunSuite {

  private def makeJar(path: os.Path, entries: Map[String, String]): Unit = {
    os.makeDir.all(path / os.up)
    val out = new ZipOutputStream(os.write.outputStream(path))
    try entries.foreach { case (name, content) =>
      out.putNextEntry(new ZipEntry(name))
      out.write(content.getBytes("UTF-8"))
      out.closeEntry()
    } finally out.close()
  }

  test("repackages classes and dependency jars in the Spring Boot layout") {
    val tmp = os.temp.dir()
    val classes = tmp / "classes"
    os.write(classes / "com" / "example" / "App.class", "app", createFolders = true)
    os.write(classes / "application.properties", "a=b")
    val lib = tmp / "libs" / "lib.jar"
    makeJar(lib, Map("A.class" -> "a"))
    val target = tmp / "out" / "app.jar"

    SpringBootPackager.pack(target, Seq(classes, lib), tmp / "work", "com.example.App")

    val jar = new JarFile(target.toIO)
    try {
      val names = jar.entries().asScala.map(_.getName).toList
      Seq(
        "BOOT-INF/classes/com/example/App.class",
        "BOOT-INF/classes/application.properties",
        "BOOT-INF/lib/lib.jar",
        "BOOT-INF/classpath.idx",
        "org/springframework/boot/loader/launch/JarLauncher.class"
      ).foreach(n => assert(names.contains(n), s"missing $n in $names"))

      val attrs = jar.getManifest.getMainAttributes
      assertEquals(attrs.getValue("Main-Class"), "org.springframework.boot.loader.launch.JarLauncher")
      assertEquals(attrs.getValue("Start-Class"), "com.example.App")
    } finally jar.close()
  }
}
