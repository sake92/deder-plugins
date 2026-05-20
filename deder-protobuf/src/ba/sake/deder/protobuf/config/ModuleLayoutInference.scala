package ba.sake.deder.protobuf.config

enum ModuleLayout {
  case MavenLike, DefaultLike
}

enum SourceSetKind {
  case Main, Test
}

object ModuleLayoutInference {

  def infer(sources: Seq[String], resources: Seq[String]): ModuleLayout = {
    val candidates = sources ++ resources
    if candidates.exists(path => path.startsWith("src/main/") || path.startsWith("src/test/")) then ModuleLayout.MavenLike
    else ModuleLayout.DefaultLike
  }

  def defaultProtoSourceDirs(layout: ModuleLayout, sourceSet: SourceSetKind): Seq[String] =
    (layout, sourceSet) match {
      case (ModuleLayout.MavenLike, SourceSetKind.Main) => Seq("src/main/protobuf", "src/main/proto")
      case (ModuleLayout.MavenLike, SourceSetKind.Test) => Seq("src/test/protobuf", "src/test/proto")
      case (ModuleLayout.DefaultLike, SourceSetKind.Main) => Seq("protobuf", "proto")
      case (ModuleLayout.DefaultLike, SourceSetKind.Test) => Seq("test/protobuf", "test/proto")
    }
}
