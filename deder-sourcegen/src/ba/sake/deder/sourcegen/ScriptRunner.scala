package ba.sake.deder.sourcegen

import ba.sake.deder.{ServerNotification, ServerNotificationsLogger}

object ScriptRunner:

  /** Writes the JSON config file and returns its absolute path. */
  def writeConfig(
      outDir: os.Path,
      config: ResolvedModuleConfig,
      moduleId: String,
      moduleRoot: String,
      scalaVersion: String,
      moduleType: String
  ): os.Path =
    val configFile = outDir / os.up / "sourcegen-config.json"
    val json = buildConfigJson(outDir, config, moduleId, moduleRoot, scalaVersion, moduleType)
    os.write(configFile, json)
    configFile

  /** Scans compiled classes for @main entry points and invokes each sequentially. */
  def runScripts(
      classesDir: os.Path,
      classpath: String,
      configPath: os.Path,
      notifications: ServerNotificationsLogger
  ): Unit =
    val mainMethods = discoverMainMethods(classesDir)
    if mainMethods.isEmpty then
      notifications.add(ServerNotification.logInfo("No @main entry points found in compiled scripts"))
    else
      mainMethods.foreach { case (className, method) =>
        notifications.add(ServerNotification.logInfo(s"Running sourcegen script: $className"))
        method.invoke(null, Array(configPath.toString))
      }

  /** Discovers all classes in classesDir that have a public static main(String[]) method. */
  private def discoverMainMethods(classesDir: os.Path): Seq[(String, java.lang.reflect.Method)] =
    val classFiles = os.walk(classesDir).filter(_.ext == "class")
    val urls = Array(classesDir.toNIO.toUri.toURL)
    val classLoader = new java.net.URLClassLoader(urls, getClass.getClassLoader)

    classFiles.flatMap { classFile =>
      val relativePath = classFile.relativeTo(classesDir).toString.stripSuffix(".class")
      val className = relativePath.replace("/", ".")
      try
        val clazz = classLoader.loadClass(className)
        val mainMethod = clazz.getMethod("main", classOf[Array[String]])
        val mods = mainMethod.getModifiers
        if java.lang.reflect.Modifier.isStatic(mods) && java.lang.reflect.Modifier.isPublic(mods) then
          Some((className, mainMethod))
        else None
      catch
        case _: ClassNotFoundException | _: NoSuchMethodException => None
    }

  /** Builds the JSON config string manually (no external JSON lib needed). */
  private def buildConfigJson(
      outDir: os.Path,
      config: ResolvedModuleConfig,
      moduleId: String,
      moduleRoot: String,
      scalaVersion: String,
      moduleType: String
  ): String =
    val absoluteOutDir = outDir.toString
    val absoluteModuleRoot = os.pwd / os.RelPath(moduleRoot)

    val extraJson = config.extra.map { case (k, v) =>
      s"""    "${escapeJson(k)}": "${escapeJson(v)}""""
    }.mkString(",\n")

    s"""|{
        |  "outDir": "${escapeJson(absoluteOutDir)}",
        |  "module": {
        |    "id": "${escapeJson(moduleId)}",
        |    "root": "${escapeJson(absoluteModuleRoot.toString)}",
        |    "scalaVersion": "${escapeJson(scalaVersion)}",
        |    "moduleType": "${escapeJson(moduleType)}"
        |  },
        |  "config": {
        |$extraJson
        |  }
        |}""".stripMargin

  private def escapeJson(s: String): String =
    s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
