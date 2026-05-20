package ba.sake.deder.protobuf.protoc

object ProtocCommandBuilder {

  def build(request: ProtocInvocationRequest): Seq[String] =
    Seq(request.executable.command) ++
      request.importRoots.distinct.map(root => s"-I=$root") ++
      request.builtins.flatMap(target => Seq(outFlag(target.name, target.options, target.outputDir.getOrElse(request.sourceOutDir)))) ++
      request.plugins.flatMap(target =>
        Seq(
          s"--plugin=protoc-gen-${target.name}=${target.executable.command}",
          outFlag(target.name, target.options, target.outputDir.getOrElse(request.sourceOutDir))
        )
      ) ++
      request.descriptor.toSeq.flatMap(descriptor =>
        Seq(s"--descriptor_set_out=${descriptor.path}") ++
          (if descriptor.includeImports then Seq("--include_imports") else Seq.empty) ++
          (if descriptor.includeSourceInfo then Seq("--include_source_info") else Seq.empty)
      ) ++
      request.extraArgs ++
      request.protoFiles

  private def outFlag(name: String, options: Option[String], outputDir: String): String = {
    val renderedOut = options.filter(_.nonEmpty) match {
      case Some(value) => s"$value:$outputDir"
      case None        => outputDir
    }
    s"--${name}_out=$renderedOut"
  }
}
