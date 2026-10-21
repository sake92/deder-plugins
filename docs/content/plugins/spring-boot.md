---
title: Spring Boot
description: Spring Boot plugin tasks and schema
pagination:
  enabled: false
layout: page.html
---

# Spring Boot

Bundles a Spring Boot 3.x application into an executable jar and runs it.
Only Java and Scala modules are supported (not test modules).

The jar uses Spring Boot's layout, so it is started by `JarLauncher` and its `LaunchedClassLoader`:

- `BOOT-INF/classes/` application classes and resources
- `BOOT-INF/lib/` dependency jars (stored uncompressed, nested)
- `BOOT-INF/classpath.idx` classpath index
- `org/springframework/boot/loader/` classes from `spring-boot-loader`
- `Main-Class: org.springframework.boot.loader.launch.JarLauncher`, `Start-Class: <your main class>`

Add it to your plugins list:
```pkl
import "https://sake92.github.io/deder-plugins/config/v0.4.0/SpringBootPlugin.pkl" as SB

plugins {
  new SB.SpringBootPlugin {
    config = new SB.SpringBootPluginConfig {
      defaults = new SB.ModuleDefaults {
        springBootVersion = "3.5.6"
      }
    }
  }
}
```

Dependencies are not managed by the plugin (no BOM support yet), declare the versions in your module.

Here are the defaults if you need to tweak them:
```pkl
plugins {
  new SB.SpringBootPlugin {
    config = new SB.SpringBootPluginConfig {
      defaults = new SB.ModuleDefaults {
        enabled = true
        springBootVersion = "3.5.6" // version of spring-boot-loader
        mainClass = null // falls back to the module's mainClass
        jvmOptions = new Listing {} // used by springBootRun
      }
      modules = new Mapping {}
    }
  }
}
```

## Schema

- [early-access](../config/deder-spring-boot/early-access/SpringBootPlugin.pkl)

## Tasks

- `springBootJar` builds `<module>-spring-boot.jar` in the task output dir
- `springBootRun` builds the jar and runs it with `java -jar`; extra args are passed to the app
