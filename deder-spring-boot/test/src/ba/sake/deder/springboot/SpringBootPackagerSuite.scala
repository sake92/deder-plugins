package ba.sake.deder.springboot

import munit.FunSuite

import java.util.zip.{ZipEntry, ZipFile, ZipOutputStream}
import scala.jdk.CollectionConverters.*

class SpringBootPackagerSuite extends FunSuite {

  private def makeJar(path: os.Path, entries: Map[String, String]): Unit = {
    val out = new ZipOutputStream(os.write.outputStream(path))
    try entries.foreach { case (name, content) =>
      out.putNextEntry(new ZipEntry(name))
      out.write(content.getBytes("UTF-8"))
      out.closeEntry()
    } finally out.close()
  }

  test("packs classes, nested libs and loader in the Spring Boot layout") {
    val tmp = os.temp.dir()
    val classes = tmp / "classes"
    os.write(classes / "com" / "example" / "App.class", "app", createFolders = true)
    os.write(classes / "application.properties", "a=b")
    val libA = tmp / "a" / "lib.jar"
    val libB = tmp / "b" / "lib.jar"
    os.makeDir.all(libA / os.up)
    os.makeDir.all(libB / os.up)
    makeJar(libA, Map("A.class" -> "a"))
    makeJar(libB, Map("B.class" -> "b"))
    val loader = tmp / "loader.jar"
    makeJar(
      loader,
      Map(
        "META-INF/MANIFEST.MF" -> "Manifest-Version: 1.0\n",
        "org/springframework/boot/loader/launch/JarLauncher.class" -> "launcher"
      )
    )
    val target = tmp / "out" / "app.jar"

    SpringBootPackager.pack(target, Seq(classes, libA, libB), loader, "com.example.App", "3.5.6")

    val zip = new ZipFile(target.toIO)
    try {
      val names = zip.entries().asScala.map(_.getName).toList
      assertEquals(names.head, "META-INF/MANIFEST.MF")
      assertEquals(names.distinct, names)
      Seq(
        "BOOT-INF/classes/com/example/App.class",
        "BOOT-INF/classes/application.properties",
        "BOOT-INF/lib/lib.jar",
        "BOOT-INF/lib/lib-1.jar",
        "BOOT-INF/classpath.idx",
        "org/springframework/boot/loader/launch/JarLauncher.class"
      ).foreach(n => assert(names.contains(n), s"missing $n in $names"))

      assertEquals(zip.getEntry("BOOT-INF/lib/lib.jar").getMethod, ZipEntry.STORED)
      assertEquals(zip.getEntry("BOOT-INF/classes/application.properties").getMethod, ZipEntry.DEFLATED)

      val idx = new String(zip.getInputStream(zip.getEntry("BOOT-INF/classpath.idx")).readAllBytes(), "UTF-8")
      assertEquals(idx, "- \"BOOT-INF/lib/lib.jar\"\n- \"BOOT-INF/lib/lib-1.jar\"\n")

      // nested jar content is preserved byte for byte
      val nested = zip.getInputStream(zip.getEntry("BOOT-INF/lib/lib-1.jar")).readAllBytes()
      assertEquals(nested.toSeq, os.read.bytes(libB).toSeq)

      val mf = new java.util.jar.Manifest(zip.getInputStream(zip.getEntry("META-INF/MANIFEST.MF")))
      val attrs = mf.getMainAttributes
      assertEquals(attrs.getValue("Main-Class"), SpringBootPackager.LauncherClass)
      assertEquals(attrs.getValue("Start-Class"), "com.example.App")
      assertEquals(attrs.getValue("Spring-Boot-Classes"), "BOOT-INF/classes/")
      assertEquals(attrs.getValue("Spring-Boot-Lib"), "BOOT-INF/lib/")
      assertEquals(attrs.getValue("Spring-Boot-Classpath-Index"), "BOOT-INF/classpath.idx")
    } finally zip.close()
  }

  test("uniqueNames disambiguates duplicate jar names") {
    val names = SpringBootPackager.uniqueNames(Seq(os.pwd / "a" / "x.jar", os.pwd / "b" / "x.jar", os.pwd / "y.jar"))
    assertEquals(names, Seq("x.jar", "x-1.jar", "y.jar"))
  }
}
