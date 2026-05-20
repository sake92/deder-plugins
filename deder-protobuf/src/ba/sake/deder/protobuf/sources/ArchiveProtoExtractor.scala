package ba.sake.deder.protobuf.sources

import java.util.zip.ZipFile

object ArchiveProtoExtractor {

  def extractArchives(archives: Seq[os.Path], destinationRoot: os.Path): Seq[os.Path] =
    archives.distinct.flatMap(archive => extractArchive(archive, destinationRoot))

  private def extractArchive(archive: os.Path, destinationRoot: os.Path): Option[os.Path] = {
    if !os.exists(archive) || !os.isFile(archive) then None
    else if !Set("jar", "zip").contains(archive.ext.toLowerCase) then None
    else {
      val outDir = destinationRoot / s"${archive.baseName}-${Math.abs(archive.toString.hashCode())}"
      if os.exists(outDir) then os.remove.all(outDir)
      os.makeDir.all(outDir)
      var extracted = false
      val zip = ZipFile(archive.toIO)
      try {
        val entries = zip.entries()
        while entries.hasMoreElements() do
          val entry = entries.nextElement()
          if !entry.isDirectory && entry.getName.endsWith(".proto") then
            val outputPath = outDir / os.RelPath(entry.getName)
            os.makeDir.all(outputPath / os.up)
            val stream = zip.getInputStream(entry)
            try os.write.over(outputPath, stream.readAllBytes(), createFolders = true)
            finally stream.close()
            extracted = true
      } finally zip.close()
      Option.when(extracted)(outDir)
    }
  }
}
