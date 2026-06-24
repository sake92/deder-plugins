@main def generate(configPath: String): Unit =
  val config = ujson.read(os.read(os.Path(configPath)))
  val outDir = os.Path(config("outDir").str)
  val pkg = config("config")("package").str

  val source =
    s"""package $pkg
       |
       |case class GeneratedMessage(text: String)
       |""".stripMargin

  os.write(outDir / "GeneratedMessage.scala", source)
  println(s"Generated: ${outDir / "GeneratedMessage.scala"}")
