package ba.sake.deder.buildinfo

object GitSupport {

  def gitHead(repoRoot: os.Path): String =
    try {
      val result = os.proc("git", "rev-parse", "HEAD")
        .call(cwd = repoRoot, stderr = os.Pipe)
      result.out.trim()
    } catch {
      case _: Exception => "unknown"
    }
}
