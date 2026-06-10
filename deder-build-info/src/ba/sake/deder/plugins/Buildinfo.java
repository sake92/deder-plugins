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

public final class Buildinfo {
  public final @NonNull BuildInfoPluginConfig config;

  public Buildinfo(@Named("config") @NonNull BuildInfoPluginConfig config) {
    this.config = config;
  }

  public Buildinfo withConfig(@NonNull BuildInfoPluginConfig config) {
    return new Buildinfo(config);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (this.getClass() != obj.getClass()) return false;
    Buildinfo other = (Buildinfo) obj;
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
    builder.append(Buildinfo.class.getSimpleName()).append(" {");
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

  public static final class BuildInfoPlugin extends DederPlugins.DederPlugin {
    public final @NonNull String version;

    public final @NonNull BuildInfoPluginConfig config;

    public BuildInfoPlugin(@Named("id") @NonNull String id,
        @Named("deps") @NonNull List<@NonNull String> deps,
        @Named("version") @NonNull String version,
        @Named("config") @NonNull BuildInfoPluginConfig config) {
      super(id, deps);
      this.version = version;
      this.config = config;
    }

    public BuildInfoPlugin withId(@NonNull String id) {
      return new BuildInfoPlugin(id, deps, version, config);
    }

    public BuildInfoPlugin withDeps(@NonNull List<@NonNull String> deps) {
      return new BuildInfoPlugin(id, deps, version, config);
    }

    public BuildInfoPlugin withVersion(@NonNull String version) {
      return new BuildInfoPlugin(id, deps, version, config);
    }

    public BuildInfoPlugin withConfig(@NonNull BuildInfoPluginConfig config) {
      return new BuildInfoPlugin(id, deps, version, config);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (this.getClass() != obj.getClass()) return false;
      BuildInfoPlugin other = (BuildInfoPlugin) obj;
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
      builder.append(BuildInfoPlugin.class.getSimpleName()).append(" {");
      appendProperty(builder, "id", this.id);
      appendProperty(builder, "deps", this.deps);
      appendProperty(builder, "version", this.version);
      appendProperty(builder, "config", this.config);
      builder.append("\n}");
      return builder.toString();
    }
  }

  public static final class BuildInfoPluginConfig {
    public final @NonNull ModuleDefaults defaults;

    public final @NonNull Map<@NonNull String, @NonNull ModuleOverride> modules;

    public BuildInfoPluginConfig(@Named("defaults") @NonNull ModuleDefaults defaults,
        @Named("modules") @NonNull Map<@NonNull String, @NonNull ModuleOverride> modules) {
      this.defaults = defaults;
      this.modules = modules;
    }

    public BuildInfoPluginConfig withDefaults(@NonNull ModuleDefaults defaults) {
      return new BuildInfoPluginConfig(defaults, modules);
    }

    public BuildInfoPluginConfig withModules(
        @NonNull Map<@NonNull String, @NonNull ModuleOverride> modules) {
      return new BuildInfoPluginConfig(defaults, modules);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (this.getClass() != obj.getClass()) return false;
      BuildInfoPluginConfig other = (BuildInfoPluginConfig) obj;
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
      builder.append(BuildInfoPluginConfig.class.getSimpleName()).append(" {");
      appendProperty(builder, "defaults", this.defaults);
      appendProperty(builder, "modules", this.modules);
      builder.append("\n}");
      return builder.toString();
    }
  }

  public static final class ModuleDefaults {
    public final boolean enabled;

    public final String packageName;

    public final @NonNull String objectName;

    public final boolean includeGitHash;

    public final boolean includeTimestamp;

    public final @NonNull Map<@NonNull String, @NonNull String> extra;

    public ModuleDefaults(@Named("enabled") boolean enabled,
        @Named("packageName") String packageName, @Named("objectName") @NonNull String objectName,
        @Named("includeGitHash") boolean includeGitHash,
        @Named("includeTimestamp") boolean includeTimestamp,
        @Named("extra") @NonNull Map<@NonNull String, @NonNull String> extra) {
      this.enabled = enabled;
      this.packageName = packageName;
      this.objectName = objectName;
      this.includeGitHash = includeGitHash;
      this.includeTimestamp = includeTimestamp;
      this.extra = extra;
    }

    public ModuleDefaults withEnabled(boolean enabled) {
      return new ModuleDefaults(enabled, packageName, objectName, includeGitHash, includeTimestamp, extra);
    }

    public ModuleDefaults withPackageName(String packageName) {
      return new ModuleDefaults(enabled, packageName, objectName, includeGitHash, includeTimestamp, extra);
    }

    public ModuleDefaults withObjectName(@NonNull String objectName) {
      return new ModuleDefaults(enabled, packageName, objectName, includeGitHash, includeTimestamp, extra);
    }

    public ModuleDefaults withIncludeGitHash(boolean includeGitHash) {
      return new ModuleDefaults(enabled, packageName, objectName, includeGitHash, includeTimestamp, extra);
    }

    public ModuleDefaults withIncludeTimestamp(boolean includeTimestamp) {
      return new ModuleDefaults(enabled, packageName, objectName, includeGitHash, includeTimestamp, extra);
    }

    public ModuleDefaults withExtra(@NonNull Map<@NonNull String, @NonNull String> extra) {
      return new ModuleDefaults(enabled, packageName, objectName, includeGitHash, includeTimestamp, extra);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (this.getClass() != obj.getClass()) return false;
      ModuleDefaults other = (ModuleDefaults) obj;
      if (!Objects.equals(this.enabled, other.enabled)) return false;
      if (!Objects.equals(this.packageName, other.packageName)) return false;
      if (!Objects.equals(this.objectName, other.objectName)) return false;
      if (!Objects.equals(this.includeGitHash, other.includeGitHash)) return false;
      if (!Objects.equals(this.includeTimestamp, other.includeTimestamp)) return false;
      if (!Objects.equals(this.extra, other.extra)) return false;
      return true;
    }

    @Override
    public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.enabled);
      result = 31 * result + Objects.hashCode(this.packageName);
      result = 31 * result + Objects.hashCode(this.objectName);
      result = 31 * result + Objects.hashCode(this.includeGitHash);
      result = 31 * result + Objects.hashCode(this.includeTimestamp);
      result = 31 * result + Objects.hashCode(this.extra);
      return result;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder(350);
      builder.append(ModuleDefaults.class.getSimpleName()).append(" {");
      appendProperty(builder, "enabled", this.enabled);
      appendProperty(builder, "packageName", this.packageName);
      appendProperty(builder, "objectName", this.objectName);
      appendProperty(builder, "includeGitHash", this.includeGitHash);
      appendProperty(builder, "includeTimestamp", this.includeTimestamp);
      appendProperty(builder, "extra", this.extra);
      builder.append("\n}");
      return builder.toString();
    }
  }

  public static final class ModuleOverride {
    public final Boolean enabled;

    public final String packageName;

    public final String objectName;

    public final Boolean includeGitHash;

    public final Boolean includeTimestamp;

    public final Map<@NonNull String, @NonNull String> extra;

    public ModuleOverride(@Named("enabled") Boolean enabled,
        @Named("packageName") String packageName, @Named("objectName") String objectName,
        @Named("includeGitHash") Boolean includeGitHash,
        @Named("includeTimestamp") Boolean includeTimestamp,
        @Named("extra") Map<@NonNull String, @NonNull String> extra) {
      this.enabled = enabled;
      this.packageName = packageName;
      this.objectName = objectName;
      this.includeGitHash = includeGitHash;
      this.includeTimestamp = includeTimestamp;
      this.extra = extra;
    }

    public ModuleOverride withEnabled(Boolean enabled) {
      return new ModuleOverride(enabled, packageName, objectName, includeGitHash, includeTimestamp, extra);
    }

    public ModuleOverride withPackageName(String packageName) {
      return new ModuleOverride(enabled, packageName, objectName, includeGitHash, includeTimestamp, extra);
    }

    public ModuleOverride withObjectName(String objectName) {
      return new ModuleOverride(enabled, packageName, objectName, includeGitHash, includeTimestamp, extra);
    }

    public ModuleOverride withIncludeGitHash(Boolean includeGitHash) {
      return new ModuleOverride(enabled, packageName, objectName, includeGitHash, includeTimestamp, extra);
    }

    public ModuleOverride withIncludeTimestamp(Boolean includeTimestamp) {
      return new ModuleOverride(enabled, packageName, objectName, includeGitHash, includeTimestamp, extra);
    }

    public ModuleOverride withExtra(Map<@NonNull String, @NonNull String> extra) {
      return new ModuleOverride(enabled, packageName, objectName, includeGitHash, includeTimestamp, extra);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (this.getClass() != obj.getClass()) return false;
      ModuleOverride other = (ModuleOverride) obj;
      if (!Objects.equals(this.enabled, other.enabled)) return false;
      if (!Objects.equals(this.packageName, other.packageName)) return false;
      if (!Objects.equals(this.objectName, other.objectName)) return false;
      if (!Objects.equals(this.includeGitHash, other.includeGitHash)) return false;
      if (!Objects.equals(this.includeTimestamp, other.includeTimestamp)) return false;
      if (!Objects.equals(this.extra, other.extra)) return false;
      return true;
    }

    @Override
    public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.enabled);
      result = 31 * result + Objects.hashCode(this.packageName);
      result = 31 * result + Objects.hashCode(this.objectName);
      result = 31 * result + Objects.hashCode(this.includeGitHash);
      result = 31 * result + Objects.hashCode(this.includeTimestamp);
      result = 31 * result + Objects.hashCode(this.extra);
      return result;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder(350);
      builder.append(ModuleOverride.class.getSimpleName()).append(" {");
      appendProperty(builder, "enabled", this.enabled);
      appendProperty(builder, "packageName", this.packageName);
      appendProperty(builder, "objectName", this.objectName);
      appendProperty(builder, "includeGitHash", this.includeGitHash);
      appendProperty(builder, "includeTimestamp", this.includeTimestamp);
      appendProperty(builder, "extra", this.extra);
      builder.append("\n}");
      return builder.toString();
    }
  }
}
