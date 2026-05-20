package ba.sake.deder.protobuf.protoc

final case class PlatformClassifier(
    classifier: String,
    binaryExtension: String
)

object PlatformClassifier {

  def current(): PlatformClassifier =
    detect(System.getProperty("os.name"), System.getProperty("os.arch"))

  def detect(osName: String, arch: String): PlatformClassifier = {
    val normalizedOs = osName.toLowerCase
    val normalizedArch = arch.toLowerCase match {
      case "amd64" | "x86_64" => "x86_64"
      case "aarch64" | "arm64" => "aarch_64"
      case other => other
    }
    val prefix =
      if normalizedOs.contains("mac") then "osx"
      else if normalizedOs.contains("win") then "windows"
      else if normalizedOs.contains("linux") then "linux"
      else throw IllegalArgumentException(s"Unsupported OS for protoc resolution: $osName")
    PlatformClassifier(s"$prefix-$normalizedArch", "exe")
  }
}
