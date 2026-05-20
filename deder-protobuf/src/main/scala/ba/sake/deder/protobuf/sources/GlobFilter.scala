package ba.sake.deder.protobuf.sources

import java.nio.file.{FileSystems, Path, Paths}

object GlobFilter {

  def filter(relativePaths: Seq[String], includeGlobs: Seq[String], excludeGlobs: Seq[String]): Seq[String] =
    relativePaths.filter(matches(_, includeGlobs, excludeGlobs))

  def matches(relativePath: String, includeGlobs: Seq[String], excludeGlobs: Seq[String]): Boolean = {
    val candidate = toPath(relativePath)
    val included = includeGlobs.isEmpty || includeGlobs.exists(glob => matchesGlob(candidate, glob))
    val excluded = excludeGlobs.exists(glob => matchesGlob(candidate, glob))
    included && !excluded
  }

  private def matcher(glob: String) =
    FileSystems.getDefault.getPathMatcher(s"glob:$glob")

  private def matchesGlob(candidate: Path, glob: String): Boolean =
    matcher(glob).matches(candidate) ||
      Option.when(glob.startsWith("**/"))(glob.stripPrefix("**/"))
        .exists(stripped => matcher(stripped).matches(candidate))

  private def toPath(relativePath: String): Path =
    Paths.get(relativePath.replace('/', java.io.File.separatorChar))
}
