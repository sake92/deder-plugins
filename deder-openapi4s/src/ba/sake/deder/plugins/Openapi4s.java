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

public final class Openapi4s {
  public final @NonNull Openapi4sPluginConfig config;

  public Openapi4s(@Named("config") @NonNull Openapi4sPluginConfig config) {
    this.config = config;
  }

  public Openapi4s withConfig(@NonNull Openapi4sPluginConfig config) {
    return new Openapi4s(config);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (this.getClass() != obj.getClass()) return false;
    Openapi4s other = (Openapi4s) obj;
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
    builder.append(Openapi4s.class.getSimpleName()).append(" {");
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

  public static final class Openapi4sPlugin extends DederPlugins.DederPlugin {
    public final @NonNull String version;

    public final @NonNull Openapi4sPluginConfig config;

    public Openapi4sPlugin(@Named("id") @NonNull String id,
        @Named("deps") @NonNull List<@NonNull String> deps,
        @Named("version") @NonNull String version,
        @Named("config") @NonNull Openapi4sPluginConfig config) {
      super(id, deps);
      this.version = version;
      this.config = config;
    }

    public Openapi4sPlugin withId(@NonNull String id) {
      return new Openapi4sPlugin(id, deps, version, config);
    }

    public Openapi4sPlugin withDeps(@NonNull List<@NonNull String> deps) {
      return new Openapi4sPlugin(id, deps, version, config);
    }

    public Openapi4sPlugin withVersion(@NonNull String version) {
      return new Openapi4sPlugin(id, deps, version, config);
    }

    public Openapi4sPlugin withConfig(@NonNull Openapi4sPluginConfig config) {
      return new Openapi4sPlugin(id, deps, version, config);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (this.getClass() != obj.getClass()) return false;
      Openapi4sPlugin other = (Openapi4sPlugin) obj;
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
      builder.append(Openapi4sPlugin.class.getSimpleName()).append(" {");
      appendProperty(builder, "id", this.id);
      appendProperty(builder, "deps", this.deps);
      appendProperty(builder, "version", this.version);
      appendProperty(builder, "config", this.config);
      builder.append("\n}");
      return builder.toString();
    }
  }

  public static final class Openapi4sPluginConfig {
    public final @NonNull ModuleDefaults defaults;

    public final @NonNull Map<@NonNull String, @NonNull ModuleOverride> modules;

    public Openapi4sPluginConfig(@Named("defaults") @NonNull ModuleDefaults defaults,
        @Named("modules") @NonNull Map<@NonNull String, @NonNull ModuleOverride> modules) {
      this.defaults = defaults;
      this.modules = modules;
    }

    public Openapi4sPluginConfig withDefaults(@NonNull ModuleDefaults defaults) {
      return new Openapi4sPluginConfig(defaults, modules);
    }

    public Openapi4sPluginConfig withModules(
        @NonNull Map<@NonNull String, @NonNull ModuleOverride> modules) {
      return new Openapi4sPluginConfig(defaults, modules);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (this.getClass() != obj.getClass()) return false;
      Openapi4sPluginConfig other = (Openapi4sPluginConfig) obj;
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
      builder.append(Openapi4sPluginConfig.class.getSimpleName()).append(" {");
      appendProperty(builder, "defaults", this.defaults);
      appendProperty(builder, "modules", this.modules);
      builder.append("\n}");
      return builder.toString();
    }
  }

  public static final class ModuleDefaults {
    public final boolean enabled;

    public final @NonNull String basePackage;

    public final @NonNull String models;

    public final @NonNull String framework;

    public final @NonNull String client;

    public final @NonNull String validation;

    public final @NonNull List<@NonNull String> tags;

    public final String input;

    public final String targetDir;

    public final @NonNull String version;

    public ModuleDefaults(@Named("enabled") boolean enabled,
        @Named("basePackage") @NonNull String basePackage, @Named("models") @NonNull String models,
        @Named("framework") @NonNull String framework, @Named("client") @NonNull String client,
        @Named("validation") @NonNull String validation,
        @Named("tags") @NonNull List<@NonNull String> tags, @Named("input") String input,
        @Named("targetDir") String targetDir, @Named("version") @NonNull String version) {
      this.enabled = enabled;
      this.basePackage = basePackage;
      this.models = models;
      this.framework = framework;
      this.client = client;
      this.validation = validation;
      this.tags = tags;
      this.input = input;
      this.targetDir = targetDir;
      this.version = version;
    }

    public ModuleDefaults withEnabled(boolean enabled) {
      return new ModuleDefaults(enabled, basePackage, models, framework, client, validation, tags, input, targetDir, version);
    }

    public ModuleDefaults withBasePackage(@NonNull String basePackage) {
      return new ModuleDefaults(enabled, basePackage, models, framework, client, validation, tags, input, targetDir, version);
    }

    public ModuleDefaults withModels(@NonNull String models) {
      return new ModuleDefaults(enabled, basePackage, models, framework, client, validation, tags, input, targetDir, version);
    }

    public ModuleDefaults withFramework(@NonNull String framework) {
      return new ModuleDefaults(enabled, basePackage, models, framework, client, validation, tags, input, targetDir, version);
    }

    public ModuleDefaults withClient(@NonNull String client) {
      return new ModuleDefaults(enabled, basePackage, models, framework, client, validation, tags, input, targetDir, version);
    }

    public ModuleDefaults withValidation(@NonNull String validation) {
      return new ModuleDefaults(enabled, basePackage, models, framework, client, validation, tags, input, targetDir, version);
    }

    public ModuleDefaults withTags(@NonNull List<@NonNull String> tags) {
      return new ModuleDefaults(enabled, basePackage, models, framework, client, validation, tags, input, targetDir, version);
    }

    public ModuleDefaults withInput(String input) {
      return new ModuleDefaults(enabled, basePackage, models, framework, client, validation, tags, input, targetDir, version);
    }

    public ModuleDefaults withTargetDir(String targetDir) {
      return new ModuleDefaults(enabled, basePackage, models, framework, client, validation, tags, input, targetDir, version);
    }

    public ModuleDefaults withVersion(@NonNull String version) {
      return new ModuleDefaults(enabled, basePackage, models, framework, client, validation, tags, input, targetDir, version);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (this.getClass() != obj.getClass()) return false;
      ModuleDefaults other = (ModuleDefaults) obj;
      if (!Objects.equals(this.enabled, other.enabled)) return false;
      if (!Objects.equals(this.basePackage, other.basePackage)) return false;
      if (!Objects.equals(this.models, other.models)) return false;
      if (!Objects.equals(this.framework, other.framework)) return false;
      if (!Objects.equals(this.client, other.client)) return false;
      if (!Objects.equals(this.validation, other.validation)) return false;
      if (!Objects.equals(this.tags, other.tags)) return false;
      if (!Objects.equals(this.input, other.input)) return false;
      if (!Objects.equals(this.targetDir, other.targetDir)) return false;
      if (!Objects.equals(this.version, other.version)) return false;
      return true;
    }

    @Override
    public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.enabled);
      result = 31 * result + Objects.hashCode(this.basePackage);
      result = 31 * result + Objects.hashCode(this.models);
      result = 31 * result + Objects.hashCode(this.framework);
      result = 31 * result + Objects.hashCode(this.client);
      result = 31 * result + Objects.hashCode(this.validation);
      result = 31 * result + Objects.hashCode(this.tags);
      result = 31 * result + Objects.hashCode(this.input);
      result = 31 * result + Objects.hashCode(this.targetDir);
      result = 31 * result + Objects.hashCode(this.version);
      return result;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder(550);
      builder.append(ModuleDefaults.class.getSimpleName()).append(" {");
      appendProperty(builder, "enabled", this.enabled);
      appendProperty(builder, "basePackage", this.basePackage);
      appendProperty(builder, "models", this.models);
      appendProperty(builder, "framework", this.framework);
      appendProperty(builder, "client", this.client);
      appendProperty(builder, "validation", this.validation);
      appendProperty(builder, "tags", this.tags);
      appendProperty(builder, "input", this.input);
      appendProperty(builder, "targetDir", this.targetDir);
      appendProperty(builder, "version", this.version);
      builder.append("\n}");
      return builder.toString();
    }
  }

  public static final class ModuleOverride {
    public final Boolean enabled;

    public final String basePackage;

    public final String models;

    public final String framework;

    public final String client;

    public final String validation;

    public final List<@NonNull String> tags;

    public final String input;

    public final String targetDir;

    public final String version;

    public ModuleOverride(@Named("enabled") Boolean enabled,
        @Named("basePackage") String basePackage, @Named("models") String models,
        @Named("framework") String framework, @Named("client") String client,
        @Named("validation") String validation, @Named("tags") List<@NonNull String> tags,
        @Named("input") String input, @Named("targetDir") String targetDir,
        @Named("version") String version) {
      this.enabled = enabled;
      this.basePackage = basePackage;
      this.models = models;
      this.framework = framework;
      this.client = client;
      this.validation = validation;
      this.tags = tags;
      this.input = input;
      this.targetDir = targetDir;
      this.version = version;
    }

    public ModuleOverride withEnabled(Boolean enabled) {
      return new ModuleOverride(enabled, basePackage, models, framework, client, validation, tags, input, targetDir, version);
    }

    public ModuleOverride withBasePackage(String basePackage) {
      return new ModuleOverride(enabled, basePackage, models, framework, client, validation, tags, input, targetDir, version);
    }

    public ModuleOverride withModels(String models) {
      return new ModuleOverride(enabled, basePackage, models, framework, client, validation, tags, input, targetDir, version);
    }

    public ModuleOverride withFramework(String framework) {
      return new ModuleOverride(enabled, basePackage, models, framework, client, validation, tags, input, targetDir, version);
    }

    public ModuleOverride withClient(String client) {
      return new ModuleOverride(enabled, basePackage, models, framework, client, validation, tags, input, targetDir, version);
    }

    public ModuleOverride withValidation(String validation) {
      return new ModuleOverride(enabled, basePackage, models, framework, client, validation, tags, input, targetDir, version);
    }

    public ModuleOverride withTags(List<@NonNull String> tags) {
      return new ModuleOverride(enabled, basePackage, models, framework, client, validation, tags, input, targetDir, version);
    }

    public ModuleOverride withInput(String input) {
      return new ModuleOverride(enabled, basePackage, models, framework, client, validation, tags, input, targetDir, version);
    }

    public ModuleOverride withTargetDir(String targetDir) {
      return new ModuleOverride(enabled, basePackage, models, framework, client, validation, tags, input, targetDir, version);
    }

    public ModuleOverride withVersion(String version) {
      return new ModuleOverride(enabled, basePackage, models, framework, client, validation, tags, input, targetDir, version);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (this.getClass() != obj.getClass()) return false;
      ModuleOverride other = (ModuleOverride) obj;
      if (!Objects.equals(this.enabled, other.enabled)) return false;
      if (!Objects.equals(this.basePackage, other.basePackage)) return false;
      if (!Objects.equals(this.models, other.models)) return false;
      if (!Objects.equals(this.framework, other.framework)) return false;
      if (!Objects.equals(this.client, other.client)) return false;
      if (!Objects.equals(this.validation, other.validation)) return false;
      if (!Objects.equals(this.tags, other.tags)) return false;
      if (!Objects.equals(this.input, other.input)) return false;
      if (!Objects.equals(this.targetDir, other.targetDir)) return false;
      if (!Objects.equals(this.version, other.version)) return false;
      return true;
    }

    @Override
    public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.enabled);
      result = 31 * result + Objects.hashCode(this.basePackage);
      result = 31 * result + Objects.hashCode(this.models);
      result = 31 * result + Objects.hashCode(this.framework);
      result = 31 * result + Objects.hashCode(this.client);
      result = 31 * result + Objects.hashCode(this.validation);
      result = 31 * result + Objects.hashCode(this.tags);
      result = 31 * result + Objects.hashCode(this.input);
      result = 31 * result + Objects.hashCode(this.targetDir);
      result = 31 * result + Objects.hashCode(this.version);
      return result;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder(550);
      builder.append(ModuleOverride.class.getSimpleName()).append(" {");
      appendProperty(builder, "enabled", this.enabled);
      appendProperty(builder, "basePackage", this.basePackage);
      appendProperty(builder, "models", this.models);
      appendProperty(builder, "framework", this.framework);
      appendProperty(builder, "client", this.client);
      appendProperty(builder, "validation", this.validation);
      appendProperty(builder, "tags", this.tags);
      appendProperty(builder, "input", this.input);
      appendProperty(builder, "targetDir", this.targetDir);
      appendProperty(builder, "version", this.version);
      builder.append("\n}");
      return builder.toString();
    }
  }
}
