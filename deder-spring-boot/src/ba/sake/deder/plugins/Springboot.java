package ba.sake.deder.plugins;

import ba.sake.deder.config.DederPlugins;
import java.lang.Boolean;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.pkl.config.java.mapper.Named;
import org.pkl.config.java.mapper.NonNull;

public final class Springboot {
  public final @NonNull SpringBootPluginConfig config;

  public Springboot(@Named("config") @NonNull SpringBootPluginConfig config) {
    this.config = config;
  }

  public Springboot withConfig(@NonNull SpringBootPluginConfig config) {
    return new Springboot(config);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (this.getClass() != obj.getClass()) return false;
    Springboot other = (Springboot) obj;
    if (!Objects.equals(this.config, other.config)) return false;
    return true;
  }

  @Override
  public int hashCode() {
    int result = 1;
    result = 31 * result + Objects.hashCode(this.config);
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder(100);
    builder.append(Springboot.class.getSimpleName()).append(" {");
    appendProperty(builder, "config", this.config);
    builder.append("\n}");
    return builder.toString();
  }

  private static void appendProperty(StringBuilder builder, String name, Object value) {
    builder.append("\n  ").append(name).append(" = ");
    String[] lines = Objects.toString(value).split("\n");
    builder.append(lines[0]);
    for (int i = 1; i < lines.length; i++) {
      builder.append("\n  ").append(lines[i]);
    }
  }

  public static final class SpringBootPlugin extends DederPlugins.DederPlugin {
    public final @NonNull String version;

    public final @NonNull SpringBootPluginConfig config;

    public SpringBootPlugin(@Named("id") @NonNull String id,
        @Named("deps") @NonNull List<@NonNull String> deps,
        @Named("version") @NonNull String version,
        @Named("config") @NonNull SpringBootPluginConfig config) {
      super(id, deps);
      this.version = version;
      this.config = config;
    }

    public SpringBootPlugin withId(@NonNull String id) {
      return new SpringBootPlugin(id, deps, version, config);
    }

    public SpringBootPlugin withDeps(@NonNull List<@NonNull String> deps) {
      return new SpringBootPlugin(id, deps, version, config);
    }

    public SpringBootPlugin withVersion(@NonNull String version) {
      return new SpringBootPlugin(id, deps, version, config);
    }

    public SpringBootPlugin withConfig(@NonNull SpringBootPluginConfig config) {
      return new SpringBootPlugin(id, deps, version, config);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (this.getClass() != obj.getClass()) return false;
      SpringBootPlugin other = (SpringBootPlugin) obj;
      if (!Objects.equals(this.id, other.id)) return false;
      if (!Objects.equals(this.deps, other.deps)) return false;
      if (!Objects.equals(this.version, other.version)) return false;
      if (!Objects.equals(this.config, other.config)) return false;
      return true;
    }

    @Override
    public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.id);
      result = 31 * result + Objects.hashCode(this.deps);
      result = 31 * result + Objects.hashCode(this.version);
      result = 31 * result + Objects.hashCode(this.config);
      return result;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder(250);
      builder.append(SpringBootPlugin.class.getSimpleName()).append(" {");
      appendProperty(builder, "id", this.id);
      appendProperty(builder, "deps", this.deps);
      appendProperty(builder, "version", this.version);
      appendProperty(builder, "config", this.config);
      builder.append("\n}");
      return builder.toString();
    }
  }

  public static final class SpringBootPluginConfig {
    public final @NonNull ModuleDefaults defaults;

    public final @NonNull Map<@NonNull String, @NonNull ModuleOverride> modules;

    public SpringBootPluginConfig(@Named("defaults") @NonNull ModuleDefaults defaults,
        @Named("modules") @NonNull Map<@NonNull String, @NonNull ModuleOverride> modules) {
      this.defaults = defaults;
      this.modules = modules;
    }

    public SpringBootPluginConfig withDefaults(@NonNull ModuleDefaults defaults) {
      return new SpringBootPluginConfig(defaults, modules);
    }

    public SpringBootPluginConfig withModules(
        @NonNull Map<@NonNull String, @NonNull ModuleOverride> modules) {
      return new SpringBootPluginConfig(defaults, modules);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (this.getClass() != obj.getClass()) return false;
      SpringBootPluginConfig other = (SpringBootPluginConfig) obj;
      if (!Objects.equals(this.defaults, other.defaults)) return false;
      if (!Objects.equals(this.modules, other.modules)) return false;
      return true;
    }

    @Override
    public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.defaults);
      result = 31 * result + Objects.hashCode(this.modules);
      return result;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder(150);
      builder.append(SpringBootPluginConfig.class.getSimpleName()).append(" {");
      appendProperty(builder, "defaults", this.defaults);
      appendProperty(builder, "modules", this.modules);
      builder.append("\n}");
      return builder.toString();
    }
  }

  public static final class ModuleDefaults {
    public final boolean enabled;

    public final @NonNull String springBootVersion;

    public final String mainClass;

    public final @NonNull List<@NonNull String> jvmOptions;

    public ModuleDefaults(@Named("enabled") boolean enabled,
        @Named("springBootVersion") @NonNull String springBootVersion,
        @Named("mainClass") String mainClass,
        @Named("jvmOptions") @NonNull List<@NonNull String> jvmOptions) {
      this.enabled = enabled;
      this.springBootVersion = springBootVersion;
      this.mainClass = mainClass;
      this.jvmOptions = jvmOptions;
    }

    public ModuleDefaults withEnabled(boolean enabled) {
      return new ModuleDefaults(enabled, springBootVersion, mainClass, jvmOptions);
    }

    public ModuleDefaults withSpringBootVersion(@NonNull String springBootVersion) {
      return new ModuleDefaults(enabled, springBootVersion, mainClass, jvmOptions);
    }

    public ModuleDefaults withMainClass(String mainClass) {
      return new ModuleDefaults(enabled, springBootVersion, mainClass, jvmOptions);
    }

    public ModuleDefaults withJvmOptions(@NonNull List<@NonNull String> jvmOptions) {
      return new ModuleDefaults(enabled, springBootVersion, mainClass, jvmOptions);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (this.getClass() != obj.getClass()) return false;
      ModuleDefaults other = (ModuleDefaults) obj;
      if (!Objects.equals(this.enabled, other.enabled)) return false;
      if (!Objects.equals(this.springBootVersion, other.springBootVersion)) return false;
      if (!Objects.equals(this.mainClass, other.mainClass)) return false;
      if (!Objects.equals(this.jvmOptions, other.jvmOptions)) return false;
      return true;
    }

    @Override
    public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.enabled);
      result = 31 * result + Objects.hashCode(this.springBootVersion);
      result = 31 * result + Objects.hashCode(this.mainClass);
      result = 31 * result + Objects.hashCode(this.jvmOptions);
      return result;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder(250);
      builder.append(ModuleDefaults.class.getSimpleName()).append(" {");
      appendProperty(builder, "enabled", this.enabled);
      appendProperty(builder, "springBootVersion", this.springBootVersion);
      appendProperty(builder, "mainClass", this.mainClass);
      appendProperty(builder, "jvmOptions", this.jvmOptions);
      builder.append("\n}");
      return builder.toString();
    }
  }

  public static final class ModuleOverride {
    public final Boolean enabled;

    public final String springBootVersion;

    public final String mainClass;

    public final List<@NonNull String> jvmOptions;

    public ModuleOverride(@Named("enabled") Boolean enabled,
        @Named("springBootVersion") String springBootVersion, @Named("mainClass") String mainClass,
        @Named("jvmOptions") List<@NonNull String> jvmOptions) {
      this.enabled = enabled;
      this.springBootVersion = springBootVersion;
      this.mainClass = mainClass;
      this.jvmOptions = jvmOptions;
    }

    public ModuleOverride withEnabled(Boolean enabled) {
      return new ModuleOverride(enabled, springBootVersion, mainClass, jvmOptions);
    }

    public ModuleOverride withSpringBootVersion(String springBootVersion) {
      return new ModuleOverride(enabled, springBootVersion, mainClass, jvmOptions);
    }

    public ModuleOverride withMainClass(String mainClass) {
      return new ModuleOverride(enabled, springBootVersion, mainClass, jvmOptions);
    }

    public ModuleOverride withJvmOptions(List<@NonNull String> jvmOptions) {
      return new ModuleOverride(enabled, springBootVersion, mainClass, jvmOptions);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (this.getClass() != obj.getClass()) return false;
      ModuleOverride other = (ModuleOverride) obj;
      if (!Objects.equals(this.enabled, other.enabled)) return false;
      if (!Objects.equals(this.springBootVersion, other.springBootVersion)) return false;
      if (!Objects.equals(this.mainClass, other.mainClass)) return false;
      if (!Objects.equals(this.jvmOptions, other.jvmOptions)) return false;
      return true;
    }

    @Override
    public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.enabled);
      result = 31 * result + Objects.hashCode(this.springBootVersion);
      result = 31 * result + Objects.hashCode(this.mainClass);
      result = 31 * result + Objects.hashCode(this.jvmOptions);
      return result;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder(250);
      builder.append(ModuleOverride.class.getSimpleName()).append(" {");
      appendProperty(builder, "enabled", this.enabled);
      appendProperty(builder, "springBootVersion", this.springBootVersion);
      appendProperty(builder, "mainClass", this.mainClass);
      appendProperty(builder, "jvmOptions", this.jvmOptions);
      builder.append("\n}");
      return builder.toString();
    }
  }
}
