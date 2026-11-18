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

public final class Jmh {
  public final @NonNull JmhPluginConfig config;

  public Jmh(@Named("config") @NonNull JmhPluginConfig config) {
    this.config = config;
  }

  public Jmh withConfig(@NonNull JmhPluginConfig config) {
    return new Jmh(config);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (this.getClass() != obj.getClass()) return false;
    Jmh other = (Jmh) obj;
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
    builder.append(Jmh.class.getSimpleName()).append(" {");
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

  public static final class JmhPlugin extends DederPlugins.DederPlugin {
    public final @NonNull String version;

    public final @NonNull JmhPluginConfig config;

    public JmhPlugin(@Named("id") @NonNull String id,
        @Named("deps") @NonNull List<@NonNull String> deps,
        @Named("version") @NonNull String version,
        @Named("config") @NonNull JmhPluginConfig config) {
      super(id, deps);
      this.version = version;
      this.config = config;
    }

    public JmhPlugin withId(@NonNull String id) {
      return new JmhPlugin(id, deps, version, config);
    }

    public JmhPlugin withDeps(@NonNull List<@NonNull String> deps) {
      return new JmhPlugin(id, deps, version, config);
    }

    public JmhPlugin withVersion(@NonNull String version) {
      return new JmhPlugin(id, deps, version, config);
    }

    public JmhPlugin withConfig(@NonNull JmhPluginConfig config) {
      return new JmhPlugin(id, deps, version, config);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (this.getClass() != obj.getClass()) return false;
      JmhPlugin other = (JmhPlugin) obj;
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
      builder.append(JmhPlugin.class.getSimpleName()).append(" {");
      appendProperty(builder, "id", this.id);
      appendProperty(builder, "deps", this.deps);
      appendProperty(builder, "version", this.version);
      appendProperty(builder, "config", this.config);
      builder.append("\n}");
      return builder.toString();
    }
  }

  public static final class JmhPluginConfig {
    public final @NonNull ModuleDefaults defaults;

    public final @NonNull Map<@NonNull String, @NonNull ModuleOverride> modules;

    public JmhPluginConfig(@Named("defaults") @NonNull ModuleDefaults defaults,
        @Named("modules") @NonNull Map<@NonNull String, @NonNull ModuleOverride> modules) {
      this.defaults = defaults;
      this.modules = modules;
    }

    public JmhPluginConfig withDefaults(@NonNull ModuleDefaults defaults) {
      return new JmhPluginConfig(defaults, modules);
    }

    public JmhPluginConfig withModules(
        @NonNull Map<@NonNull String, @NonNull ModuleOverride> modules) {
      return new JmhPluginConfig(defaults, modules);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (this.getClass() != obj.getClass()) return false;
      JmhPluginConfig other = (JmhPluginConfig) obj;
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
      builder.append(JmhPluginConfig.class.getSimpleName()).append(" {");
      appendProperty(builder, "defaults", this.defaults);
      appendProperty(builder, "modules", this.modules);
      builder.append("\n}");
      return builder.toString();
    }
  }

  public static final class ModuleDefaults {
    public final boolean enabled;

    public final @NonNull String jmhVersion;

    public final @NonNull List<@NonNull String> jvmOptions;

    public ModuleDefaults(@Named("enabled") boolean enabled,
        @Named("jmhVersion") @NonNull String jmhVersion,
        @Named("jvmOptions") @NonNull List<@NonNull String> jvmOptions) {
      this.enabled = enabled;
      this.jmhVersion = jmhVersion;
      this.jvmOptions = jvmOptions;
    }

    public ModuleDefaults withEnabled(boolean enabled) {
      return new ModuleDefaults(enabled, jmhVersion, jvmOptions);
    }

    public ModuleDefaults withJmhVersion(@NonNull String jmhVersion) {
      return new ModuleDefaults(enabled, jmhVersion, jvmOptions);
    }

    public ModuleDefaults withJvmOptions(@NonNull List<@NonNull String> jvmOptions) {
      return new ModuleDefaults(enabled, jmhVersion, jvmOptions);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (this.getClass() != obj.getClass()) return false;
      ModuleDefaults other = (ModuleDefaults) obj;
      if (!Objects.equals(this.enabled, other.enabled)) return false;
      if (!Objects.equals(this.jmhVersion, other.jmhVersion)) return false;
      if (!Objects.equals(this.jvmOptions, other.jvmOptions)) return false;
      return true;
    }

    @Override
    public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.enabled);
      result = 31 * result + Objects.hashCode(this.jmhVersion);
      result = 31 * result + Objects.hashCode(this.jvmOptions);
      return result;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder(200);
      builder.append(ModuleDefaults.class.getSimpleName()).append(" {");
      appendProperty(builder, "enabled", this.enabled);
      appendProperty(builder, "jmhVersion", this.jmhVersion);
      appendProperty(builder, "jvmOptions", this.jvmOptions);
      builder.append("\n}");
      return builder.toString();
    }
  }

  public static final class ModuleOverride {
    public final Boolean enabled;

    public final String jmhVersion;

    public final List<@NonNull String> jvmOptions;

    public ModuleOverride(@Named("enabled") Boolean enabled, @Named("jmhVersion") String jmhVersion,
        @Named("jvmOptions") List<@NonNull String> jvmOptions) {
      this.enabled = enabled;
      this.jmhVersion = jmhVersion;
      this.jvmOptions = jvmOptions;
    }

    public ModuleOverride withEnabled(Boolean enabled) {
      return new ModuleOverride(enabled, jmhVersion, jvmOptions);
    }

    public ModuleOverride withJmhVersion(String jmhVersion) {
      return new ModuleOverride(enabled, jmhVersion, jvmOptions);
    }

    public ModuleOverride withJvmOptions(List<@NonNull String> jvmOptions) {
      return new ModuleOverride(enabled, jmhVersion, jvmOptions);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (this.getClass() != obj.getClass()) return false;
      ModuleOverride other = (ModuleOverride) obj;
      if (!Objects.equals(this.enabled, other.enabled)) return false;
      if (!Objects.equals(this.jmhVersion, other.jmhVersion)) return false;
      if (!Objects.equals(this.jvmOptions, other.jvmOptions)) return false;
      return true;
    }

    @Override
    public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.enabled);
      result = 31 * result + Objects.hashCode(this.jmhVersion);
      result = 31 * result + Objects.hashCode(this.jvmOptions);
      return result;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder(200);
      builder.append(ModuleOverride.class.getSimpleName()).append(" {");
      appendProperty(builder, "enabled", this.enabled);
      appendProperty(builder, "jmhVersion", this.jmhVersion);
      appendProperty(builder, "jvmOptions", this.jvmOptions);
      builder.append("\n}");
      return builder.toString();
    }
  }
}
