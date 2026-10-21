package ba.sake.deder.springboot

import java.io.{BufferedInputStream, InputStream, OutputStream}
import java.util.jar.{Attributes, Manifest}
import java.util.zip.{CRC32, ZipEntry, ZipFile, ZipOutputStream}
import scala.collection.mutable
import scala.jdk.CollectionConverters.*

/** Writes an executable Spring Boot (3.2+) jar:
  *   - `BOOT-INF/classes/` : application classes and resources
  *   - `BOOT-INF/lib/`     : dependency jars, stored uncompressed (they are read as nested jars by the launcher)
  *   - `BOOT-INF/classpath.idx`
  *   - `org/springframework/boot/loader/` : classes of `spring-boot-loader`
  *   - `Main-Class` is `JarLauncher`, `Start-Class` is the application main class
  */
object SpringBootPackager {

  val LauncherClass = "org.springframework.boot.loader.launch.JarLauncher"
  val ClassesDir = "BOOT-INF/classes/"
  val LibDir = "BOOT-INF/lib/"
  val ClasspathIndex = "BOOT-INF/classpath.idx"

  def pack(
      target: os.Path,
      classpath: Seq[os.Path],
      loaderJar: os.Path,
      mainClass: String,
      springBootVersion: String
  ): Unit = {
    val classesDirs = classpath.filter(os.isDir(_)).distinct
    val libJars = classpath.filter(p => os.isFile(p) && p.last.endsWith(".jar")).distinct
    val libNames = uniqueNames(libJars)

    os.makeDir.all(target / os.up)
    os.remove.all(target)
    val out = new ZipOutputStream(os.write.outputStream(target))
    val written = mutable.Set.empty[String]
    try {
      def startEntry(name: String, method: Int = ZipEntry.DEFLATED): ZipEntry | Null =
        if !written.add(name) then null
        else {
          val e = new ZipEntry(name)
          e.setMethod(method)
          e
        }

      def addDir(name: String): Unit =
        Option(startEntry(name)).foreach { e =>
          out.putNextEntry(e)
          out.closeEntry()
        }

      def addBytes(name: String, bytes: Array[Byte]): Unit =
        Option(startEntry(name)).foreach { e =>
          out.putNextEntry(e)
          out.write(bytes)
          out.closeEntry()
        }

      def addStream(name: String, in: => InputStream): Unit =
        Option(startEntry(name)).foreach { e =>
          out.putNextEntry(e)
          val is = in
          try is.transferTo(out)
          finally is.close()
          out.closeEntry()
        }

      // manifest has to be the first entry
      written += "META-INF/MANIFEST.MF"
      out.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"))
      manifest(mainClass, springBootVersion).write(out)
      out.closeEntry()

      addDir("META-INF/")
      addDir("BOOT-INF/")
      addDir(ClassesDir)
      addDir(LibDir)

      // application classes and resources, first on the classpath wins
      classesDirs.foreach { dir =>
        os.walk(dir).sortBy(_.toString).foreach { p =>
          val rel = p.relativeTo(dir).toString.replace('\\', '/')
          if os.isDir(p) then addDir(s"$ClassesDir$rel/")
          else addStream(s"$ClassesDir$rel", os.read.inputStream(p))
        }
      }

      // nested dependency jars have to be STORED
      libJars.zip(libNames).foreach { case (jar, name) =>
        Option(startEntry(LibDir + name, ZipEntry.STORED)).foreach { e =>
          val crc = new CRC32
          val size = crc32(jar, crc)
          e.setSize(size)
          e.setCompressedSize(size)
          e.setCrc(crc.getValue)
          out.putNextEntry(e)
          val is = os.read.inputStream(jar)
          try is.transferTo(out)
          finally is.close()
          out.closeEntry()
        }
      }

      val index = libNames.map(name => s"""- "$LibDir$name"""").mkString("", "\n", "\n")
      addBytes(ClasspathIndex, index.getBytes("UTF-8"))

      // launcher classes at the root
      val loader = new ZipFile(loaderJar.toIO)
      try
        loader.entries().asScala.filterNot(e => skipLoaderEntry(e.getName)).foreach { e =>
          if e.isDirectory then addDir(e.getName)
          else addStream(e.getName, loader.getInputStream(e))
        }
      finally loader.close()
    } finally out.close()
  }

  private[springboot] def manifest(mainClass: String, springBootVersion: String): Manifest = {
    val manifest = new Manifest()
    val attrs = manifest.getMainAttributes
    attrs.put(Attributes.Name.MANIFEST_VERSION, "1.0")
    attrs.putValue("Created-By", "deder-spring-boot")
    attrs.put(Attributes.Name.MAIN_CLASS, LauncherClass)
    attrs.putValue("Start-Class", mainClass)
    attrs.putValue("Spring-Boot-Version", springBootVersion)
    attrs.putValue("Spring-Boot-Classes", ClassesDir)
    attrs.putValue("Spring-Boot-Lib", LibDir)
    attrs.putValue("Spring-Boot-Classpath-Index", ClasspathIndex)
    manifest
  }

  /** File names for the nested jars, made unique so that two jars with the same name do not collide. */
  private[springboot] def uniqueNames(jars: Seq[os.Path]): Seq[String] = {
    val seen = mutable.Map.empty[String, Int]
    jars.map { jar =>
      val name = jar.last
      val count = seen.getOrElse(name, 0)
      seen(name) = count + 1
      if count == 0 then name else s"${name.stripSuffix(".jar")}-$count.jar"
    }
  }

  private def skipLoaderEntry(name: String): Boolean = {
    val upper = name.toUpperCase
    upper == "META-INF/MANIFEST.MF" ||
    (upper.startsWith("META-INF/") && Seq(".SF", ".RSA", ".DSA").exists(upper.endsWith))
  }

  private def crc32(path: os.Path, crc: CRC32): Long = {
    val in = new BufferedInputStream(os.read.inputStream(path))
    try {
      val buf = new Array[Byte](8192)
      var total = 0L
      var n = in.read(buf)
      while n >= 0 do {
        crc.update(buf, 0, n)
        total += n
        n = in.read(buf)
      }
      total
    } finally in.close()
  }
}
