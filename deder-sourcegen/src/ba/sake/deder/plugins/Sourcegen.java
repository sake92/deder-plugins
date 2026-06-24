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

public final class Sourcegen {
  public final @NonNull SourcegenPluginConfig config;

  public Sourcegen(@Named("config") @NonNull SourcegenPluginConfig config) {
    this.config = config;
  }

  public Sourcegen withConfig(@NonNull SourcegenPluginConfig config) {
    return new Sourcegen(config);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (this.getClass() != obj.getClass()) return false;
    Sourcegen other = (Sourcegen) obj;
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
    builder.append(Sourcegen.class.getSimpleName()).append(" {");
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

  public static final class SourcegenPlugin extends DederPlugins.DederPlugin {
    public final @NonNull String version;

    public final @NonNull SourcegenPluginConfig config;

    public SourcegenPlugin(@Named("id") @NonNull String id,
        @Named("deps") @NonNull List<@NonNull String> deps,
        @Named("version") @NonNull String version,
        @Named("config") @NonNull SourcegenPluginConfig config) {
      super(id, deps);
      this.version = version;
      this.config = config;
    }

    public SourcegenPlugin withId(@NonNull String id) {
      return new SourcegenPlugin(id, deps, version, config);
    }

    public SourcegenPlugin withDeps(@NonNull List<@NonNull String> deps) {
      return new SourcegenPlugin(id, deps, version, config);
    }

    public SourcegenPlugin withVersion(@NonNull String version) {
      return new SourcegenPlugin(id, deps, version, config);
    }

    public SourcegenPlugin withConfig(@NonNull SourcegenPluginConfig config) {
      return new SourcegenPlugin(id, deps, version, config);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (this.getClass() != obj.getClass()) return false;
      SourcegenPlugin other = (SourcegenPlugin) obj;
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
      builder.append(SourcegenPlugin.class.getSimpleName()).append(" {");
      appendProperty(builder, "id", this.id);
      appendProperty(builder, "deps", this.deps);
      appendProperty(builder, "version", this.version);
      appendProperty(builder, "config", this.config);
      builder.append("\n}");
      return builder.toString();
    }
  }

  public static final class SourcegenPluginConfig {
    public final @NonNull ModuleDefaults defaults;

    public final @NonNull Map<@NonNull String, @NonNull ModuleOverride> modules;

    public SourcegenPluginConfig(@Named("defaults") @NonNull ModuleDefaults defaults,
        @Named("modules") @NonNull Map<@NonNull String, @NonNull ModuleOverride> modules) {
      this.defaults = defaults;
      this.modules = modules;
    }

    public SourcegenPluginConfig withDefaults(@NonNull ModuleDefaults defaults) {
      return new SourcegenPluginConfig(defaults, modules);
    }

    public SourcegenPluginConfig withModules(
        @NonNull Map<@NonNull String, @NonNull ModuleOverride> modules) {
      return new SourcegenPluginConfig(defaults, modules);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (this.getClass() != obj.getClass()) return false;
      SourcegenPluginConfig other = (SourcegenPluginConfig) obj;
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
      builder.append(SourcegenPluginConfig.class.getSimpleName()).append(" {");
      appendProperty(builder, "defaults", this.defaults);
      appendProperty(builder, "modules", this.modules);
      builder.append("\n}");
      return builder.toString();
    }
  }

  public static final class ModuleDefaults {
    public final boolean enabled;

    public final @NonNull List<@NonNull String> deps;

    public final @NonNull String scriptsDir;

    public final String scalaVersion;

    public final @NonNull String outputSourceSet;

    public final @NonNull Map<@NonNull String, @NonNull String> extra;

    public ModuleDefaults(@Named("enabled") boolean enabled,
        @Named("deps") @NonNull List<@NonNull String> deps,
        @Named("scriptsDir") @NonNull String scriptsDir, @Named("scalaVersion") String scalaVersion,
        @Named("outputSourceSet") @NonNull String outputSourceSet,
        @Named("extra") @NonNull Map<@NonNull String, @NonNull String> extra) {
      this.enabled = enabled;
      this.deps = deps;
      this.scriptsDir = scriptsDir;
      this.scalaVersion = scalaVersion;
      this.outputSourceSet = outputSourceSet;
      this.extra = extra;
    }

    public ModuleDefaults withEnabled(boolean enabled) {
      return new ModuleDefaults(enabled, deps, scriptsDir, scalaVersion, outputSourceSet, extra);
    }

    public ModuleDefaults withDeps(@NonNull List<@NonNull String> deps) {
      return new ModuleDefaults(enabled, deps, scriptsDir, scalaVersion, outputSourceSet, extra);
    }

    public ModuleDefaults withScriptsDir(@NonNull String scriptsDir) {
      return new ModuleDefaults(enabled, deps, scriptsDir, scalaVersion, outputSourceSet, extra);
    }

    public ModuleDefaults withScalaVersion(String scalaVersion) {
      return new ModuleDefaults(enabled, deps, scriptsDir, scalaVersion, outputSourceSet, extra);
    }

    public ModuleDefaults withOutputSourceSet(@NonNull String outputSourceSet) {
      return new ModuleDefaults(enabled, deps, scriptsDir, scalaVersion, outputSourceSet, extra);
    }

    public ModuleDefaults withExtra(@NonNull Map<@NonNull String, @NonNull String> extra) {
      return new ModuleDefaults(enabled, deps, scriptsDir, scalaVersion, outputSourceSet, extra);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (this.getClass() != obj.getClass()) return false;
      ModuleDefaults other = (ModuleDefaults) obj;
      if (!Objects.equals(this.enabled, other.enabled)) return false;
      if (!Objects.equals(this.deps, other.deps)) return false;
      if (!Objects.equals(this.scriptsDir, other.scriptsDir)) return false;
      if (!Objects.equals(this.scalaVersion, other.scalaVersion)) return false;
      if (!Objects.equals(this.outputSourceSet, other.outputSourceSet)) return false;
      if (!Objects.equals(this.extra, other.extra)) return false;
      return true;
    }

    @Override
    public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.enabled);
      result = 31 * result + Objects.hashCode(this.deps);
      result = 31 * result + Objects.hashCode(this.scriptsDir);
      result = 31 * result + Objects.hashCode(this.scalaVersion);
      result = 31 * result + Objects.hashCode(this.outputSourceSet);
      result = 31 * result + Objects.hashCode(this.extra);
      return result;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder(350);
      builder.append(ModuleDefaults.class.getSimpleName()).append(" {");
      appendProperty(builder, "enabled", this.enabled);
      appendProperty(builder, "deps", this.deps);
      appendProperty(builder, "scriptsDir", this.scriptsDir);
      appendProperty(builder, "scalaVersion", this.scalaVersion);
      appendProperty(builder, "outputSourceSet", this.outputSourceSet);
      appendProperty(builder, "extra", this.extra);
      builder.append("\n}");
      return builder.toString();
    }
  }

  public static final class ModuleOverride {
    public final Boolean enabled;

    public final List<@NonNull String> deps;

    public final String scriptsDir;

    public final String scalaVersion;

    public final String outputSourceSet;

    public final Map<@NonNull String, @NonNull String> extra;

    public ModuleOverride(@Named("enabled") Boolean enabled,
        @Named("deps") List<@NonNull String> deps, @Named("scriptsDir") String scriptsDir,
        @Named("scalaVersion") String scalaVersion,
        @Named("outputSourceSet") String outputSourceSet,
        @Named("extra") Map<@NonNull String, @NonNull String> extra) {
      this.enabled = enabled;
      this.deps = deps;
      this.scriptsDir = scriptsDir;
      this.scalaVersion = scalaVersion;
      this.outputSourceSet = outputSourceSet;
      this.extra = extra;
    }

    public ModuleOverride withEnabled(Boolean enabled) {
      return new ModuleOverride(enabled, deps, scriptsDir, scalaVersion, outputSourceSet, extra);
    }

    public ModuleOverride withDeps(List<@NonNull String> deps) {
      return new ModuleOverride(enabled, deps, scriptsDir, scalaVersion, outputSourceSet, extra);
    }

    public ModuleOverride withScriptsDir(String scriptsDir) {
      return new ModuleOverride(enabled, deps, scriptsDir, scalaVersion, outputSourceSet, extra);
    }

    public ModuleOverride withScalaVersion(String scalaVersion) {
      return new ModuleOverride(enabled, deps, scriptsDir, scalaVersion, outputSourceSet, extra);
    }

    public ModuleOverride withOutputSourceSet(String outputSourceSet) {
      return new ModuleOverride(enabled, deps, scriptsDir, scalaVersion, outputSourceSet, extra);
    }

    public ModuleOverride withExtra(Map<@NonNull String, @NonNull String> extra) {
      return new ModuleOverride(enabled, deps, scriptsDir, scalaVersion, outputSourceSet, extra);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (this.getClass() != obj.getClass()) return false;
      ModuleOverride other = (ModuleOverride) obj;
      if (!Objects.equals(this.enabled, other.enabled)) return false;
      if (!Objects.equals(this.deps, other.deps)) return false;
      if (!Objects.equals(this.scriptsDir, other.scriptsDir)) return false;
      if (!Objects.equals(this.scalaVersion, other.scalaVersion)) return false;
      if (!Objects.equals(this.outputSourceSet, other.outputSourceSet)) return false;
      if (!Objects.equals(this.extra, other.extra)) return false;
      return true;
    }

    @Override
    public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.enabled);
      result = 31 * result + Objects.hashCode(this.deps);
      result = 31 * result + Objects.hashCode(this.scriptsDir);
      result = 31 * result + Objects.hashCode(this.scalaVersion);
      result = 31 * result + Objects.hashCode(this.outputSourceSet);
      result = 31 * result + Objects.hashCode(this.extra);
      return result;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder(350);
      builder.append(ModuleOverride.class.getSimpleName()).append(" {");
      appendProperty(builder, "enabled", this.enabled);
      appendProperty(builder, "deps", this.deps);
      appendProperty(builder, "scriptsDir", this.scriptsDir);
      appendProperty(builder, "scalaVersion", this.scalaVersion);
      appendProperty(builder, "outputSourceSet", this.outputSourceSet);
      appendProperty(builder, "extra", this.extra);
      builder.append("\n}");
      return builder.toString();
    }
  }
}
