package ba.sake.deder.plugins;

import ba.sake.deder.config.DederPlugins;
import java.lang.Boolean;
import java.lang.Double;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.pkl.config.java.mapper.Named;
import org.pkl.config.java.mapper.NonNull;

public final class Owasp {
  public final @NonNull OwaspPluginConfig config;

  public Owasp(@Named("config") @NonNull OwaspPluginConfig config) {
    this.config = config;
  }

  public Owasp withConfig(@NonNull OwaspPluginConfig config) {
    return new Owasp(config);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (this.getClass() != obj.getClass()) return false;
    Owasp other = (Owasp) obj;
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
    builder.append(Owasp.class.getSimpleName()).append(" {");
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

  public static final class OwaspPlugin extends DederPlugins.DederPlugin {
    public final @NonNull String version;

    public final @NonNull OwaspPluginConfig config;

    public OwaspPlugin(@Named("id") @NonNull String id,
        @Named("deps") @NonNull List<@NonNull String> deps,
        @Named("version") @NonNull String version,
        @Named("config") @NonNull OwaspPluginConfig config) {
      super(id, deps);
      this.version = version;
      this.config = config;
    }

    public OwaspPlugin withId(@NonNull String id) {
      return new OwaspPlugin(id, deps, version, config);
    }

    public OwaspPlugin withDeps(@NonNull List<@NonNull String> deps) {
      return new OwaspPlugin(id, deps, version, config);
    }

    public OwaspPlugin withVersion(@NonNull String version) {
      return new OwaspPlugin(id, deps, version, config);
    }

    public OwaspPlugin withConfig(@NonNull OwaspPluginConfig config) {
      return new OwaspPlugin(id, deps, version, config);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (this.getClass() != obj.getClass()) return false;
      OwaspPlugin other = (OwaspPlugin) obj;
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
      builder.append(OwaspPlugin.class.getSimpleName()).append(" {");
      appendProperty(builder, "id", this.id);
      appendProperty(builder, "deps", this.deps);
      appendProperty(builder, "version", this.version);
      appendProperty(builder, "config", this.config);
      builder.append("\n}");
      return builder.toString();
    }
  }

  public static final class OwaspPluginConfig {
    public final @NonNull ModuleDefaults defaults;

    public final @NonNull Map<@NonNull String, @NonNull ModuleOverride> modules;

    public OwaspPluginConfig(@Named("defaults") @NonNull ModuleDefaults defaults,
        @Named("modules") @NonNull Map<@NonNull String, @NonNull ModuleOverride> modules) {
      this.defaults = defaults;
      this.modules = modules;
    }

    public OwaspPluginConfig withDefaults(@NonNull ModuleDefaults defaults) {
      return new OwaspPluginConfig(defaults, modules);
    }

    public OwaspPluginConfig withModules(
        @NonNull Map<@NonNull String, @NonNull ModuleOverride> modules) {
      return new OwaspPluginConfig(defaults, modules);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (this.getClass() != obj.getClass()) return false;
      OwaspPluginConfig other = (OwaspPluginConfig) obj;
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
      builder.append(OwaspPluginConfig.class.getSimpleName()).append(" {");
      appendProperty(builder, "defaults", this.defaults);
      appendProperty(builder, "modules", this.modules);
      builder.append("\n}");
      return builder.toString();
    }
  }

  public static final class ModuleDefaults {
    public final boolean enabled;

    public final @NonNull String dependencyCheckVersion;

    public final @NonNull List<@NonNull String> formats;

    public final Double failOnCvss;

    public final @NonNull List<@NonNull String> suppressionFiles;

    public final @NonNull String dataDirectory;

    public final @NonNull List<@NonNull String> extraArgs;

    public ModuleDefaults(@Named("enabled") boolean enabled,
        @Named("dependencyCheckVersion") @NonNull String dependencyCheckVersion,
        @Named("formats") @NonNull List<@NonNull String> formats,
        @Named("failOnCvss") Double failOnCvss,
        @Named("suppressionFiles") @NonNull List<@NonNull String> suppressionFiles,
        @Named("dataDirectory") @NonNull String dataDirectory,
        @Named("extraArgs") @NonNull List<@NonNull String> extraArgs) {
      this.enabled = enabled;
      this.dependencyCheckVersion = dependencyCheckVersion;
      this.formats = formats;
      this.failOnCvss = failOnCvss;
      this.suppressionFiles = suppressionFiles;
      this.dataDirectory = dataDirectory;
      this.extraArgs = extraArgs;
    }

    public ModuleDefaults withEnabled(boolean enabled) {
      return new ModuleDefaults(enabled, dependencyCheckVersion, formats, failOnCvss, suppressionFiles, dataDirectory, extraArgs);
    }

    public ModuleDefaults withDependencyCheckVersion(@NonNull String dependencyCheckVersion) {
      return new ModuleDefaults(enabled, dependencyCheckVersion, formats, failOnCvss, suppressionFiles, dataDirectory, extraArgs);
    }

    public ModuleDefaults withFormats(@NonNull List<@NonNull String> formats) {
      return new ModuleDefaults(enabled, dependencyCheckVersion, formats, failOnCvss, suppressionFiles, dataDirectory, extraArgs);
    }

    public ModuleDefaults withFailOnCvss(Double failOnCvss) {
      return new ModuleDefaults(enabled, dependencyCheckVersion, formats, failOnCvss, suppressionFiles, dataDirectory, extraArgs);
    }

    public ModuleDefaults withSuppressionFiles(@NonNull List<@NonNull String> suppressionFiles) {
      return new ModuleDefaults(enabled, dependencyCheckVersion, formats, failOnCvss, suppressionFiles, dataDirectory, extraArgs);
    }

    public ModuleDefaults withDataDirectory(@NonNull String dataDirectory) {
      return new ModuleDefaults(enabled, dependencyCheckVersion, formats, failOnCvss, suppressionFiles, dataDirectory, extraArgs);
    }

    public ModuleDefaults withExtraArgs(@NonNull List<@NonNull String> extraArgs) {
      return new ModuleDefaults(enabled, dependencyCheckVersion, formats, failOnCvss, suppressionFiles, dataDirectory, extraArgs);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (this.getClass() != obj.getClass()) return false;
      ModuleDefaults other = (ModuleDefaults) obj;
      if (!Objects.equals(this.enabled, other.enabled)) return false;
      if (!Objects.equals(this.dependencyCheckVersion, other.dependencyCheckVersion)) return false;
      if (!Objects.equals(this.formats, other.formats)) return false;
      if (!Objects.equals(this.failOnCvss, other.failOnCvss)) return false;
      if (!Objects.equals(this.suppressionFiles, other.suppressionFiles)) return false;
      if (!Objects.equals(this.dataDirectory, other.dataDirectory)) return false;
      if (!Objects.equals(this.extraArgs, other.extraArgs)) return false;
      return true;
    }

    @Override
    public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.enabled);
      result = 31 * result + Objects.hashCode(this.dependencyCheckVersion);
      result = 31 * result + Objects.hashCode(this.formats);
      result = 31 * result + Objects.hashCode(this.failOnCvss);
      result = 31 * result + Objects.hashCode(this.suppressionFiles);
      result = 31 * result + Objects.hashCode(this.dataDirectory);
      result = 31 * result + Objects.hashCode(this.extraArgs);
      return result;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder(400);
      builder.append(ModuleDefaults.class.getSimpleName()).append(" {");
      appendProperty(builder, "enabled", this.enabled);
      appendProperty(builder, "dependencyCheckVersion", this.dependencyCheckVersion);
      appendProperty(builder, "formats", this.formats);
      appendProperty(builder, "failOnCvss", this.failOnCvss);
      appendProperty(builder, "suppressionFiles", this.suppressionFiles);
      appendProperty(builder, "dataDirectory", this.dataDirectory);
      appendProperty(builder, "extraArgs", this.extraArgs);
      builder.append("\n}");
      return builder.toString();
    }
  }

  public static final class ModuleOverride {
    public final Boolean enabled;

    public final String dependencyCheckVersion;

    public final List<@NonNull String> formats;

    public final Double failOnCvss;

    public final List<@NonNull String> suppressionFiles;

    public final String dataDirectory;

    public final List<@NonNull String> extraArgs;

    public ModuleOverride(@Named("enabled") Boolean enabled,
        @Named("dependencyCheckVersion") String dependencyCheckVersion,
        @Named("formats") List<@NonNull String> formats, @Named("failOnCvss") Double failOnCvss,
        @Named("suppressionFiles") List<@NonNull String> suppressionFiles,
        @Named("dataDirectory") String dataDirectory,
        @Named("extraArgs") List<@NonNull String> extraArgs) {
      this.enabled = enabled;
      this.dependencyCheckVersion = dependencyCheckVersion;
      this.formats = formats;
      this.failOnCvss = failOnCvss;
      this.suppressionFiles = suppressionFiles;
      this.dataDirectory = dataDirectory;
      this.extraArgs = extraArgs;
    }

    public ModuleOverride withEnabled(Boolean enabled) {
      return new ModuleOverride(enabled, dependencyCheckVersion, formats, failOnCvss, suppressionFiles, dataDirectory, extraArgs);
    }

    public ModuleOverride withDependencyCheckVersion(String dependencyCheckVersion) {
      return new ModuleOverride(enabled, dependencyCheckVersion, formats, failOnCvss, suppressionFiles, dataDirectory, extraArgs);
    }

    public ModuleOverride withFormats(List<@NonNull String> formats) {
      return new ModuleOverride(enabled, dependencyCheckVersion, formats, failOnCvss, suppressionFiles, dataDirectory, extraArgs);
    }

    public ModuleOverride withFailOnCvss(Double failOnCvss) {
      return new ModuleOverride(enabled, dependencyCheckVersion, formats, failOnCvss, suppressionFiles, dataDirectory, extraArgs);
    }

    public ModuleOverride withSuppressionFiles(List<@NonNull String> suppressionFiles) {
      return new ModuleOverride(enabled, dependencyCheckVersion, formats, failOnCvss, suppressionFiles, dataDirectory, extraArgs);
    }

    public ModuleOverride withDataDirectory(String dataDirectory) {
      return new ModuleOverride(enabled, dependencyCheckVersion, formats, failOnCvss, suppressionFiles, dataDirectory, extraArgs);
    }

    public ModuleOverride withExtraArgs(List<@NonNull String> extraArgs) {
      return new ModuleOverride(enabled, dependencyCheckVersion, formats, failOnCvss, suppressionFiles, dataDirectory, extraArgs);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (this.getClass() != obj.getClass()) return false;
      ModuleOverride other = (ModuleOverride) obj;
      if (!Objects.equals(this.enabled, other.enabled)) return false;
      if (!Objects.equals(this.dependencyCheckVersion, other.dependencyCheckVersion)) return false;
      if (!Objects.equals(this.formats, other.formats)) return false;
      if (!Objects.equals(this.failOnCvss, other.failOnCvss)) return false;
      if (!Objects.equals(this.suppressionFiles, other.suppressionFiles)) return false;
      if (!Objects.equals(this.dataDirectory, other.dataDirectory)) return false;
      if (!Objects.equals(this.extraArgs, other.extraArgs)) return false;
      return true;
    }

    @Override
    public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.enabled);
      result = 31 * result + Objects.hashCode(this.dependencyCheckVersion);
      result = 31 * result + Objects.hashCode(this.formats);
      result = 31 * result + Objects.hashCode(this.failOnCvss);
      result = 31 * result + Objects.hashCode(this.suppressionFiles);
      result = 31 * result + Objects.hashCode(this.dataDirectory);
      result = 31 * result + Objects.hashCode(this.extraArgs);
      return result;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder(400);
      builder.append(ModuleOverride.class.getSimpleName()).append(" {");
      appendProperty(builder, "enabled", this.enabled);
      appendProperty(builder, "dependencyCheckVersion", this.dependencyCheckVersion);
      appendProperty(builder, "formats", this.formats);
      appendProperty(builder, "failOnCvss", this.failOnCvss);
      appendProperty(builder, "suppressionFiles", this.suppressionFiles);
      appendProperty(builder, "dataDirectory", this.dataDirectory);
      appendProperty(builder, "extraArgs", this.extraArgs);
      builder.append("\n}");
      return builder.toString();
    }
  }
}
