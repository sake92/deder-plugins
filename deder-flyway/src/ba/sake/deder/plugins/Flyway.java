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

public final class Flyway {
  public final @NonNull FlywayPluginConfig config;

  public Flyway(@Named("config") @NonNull FlywayPluginConfig config) {
    this.config = config;
  }

  public Flyway withConfig(@NonNull FlywayPluginConfig config) {
    return new Flyway(config);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (this.getClass() != obj.getClass()) return false;
    Flyway other = (Flyway) obj;
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
    builder.append(Flyway.class.getSimpleName()).append(" {");
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

  public static final class FlywayPlugin extends DederPlugins.DederPlugin {
    public final @NonNull String version;

    public final @NonNull FlywayPluginConfig config;

    public FlywayPlugin(@Named("id") @NonNull String id,
        @Named("deps") @NonNull List<@NonNull String> deps,
        @Named("version") @NonNull String version,
        @Named("config") @NonNull FlywayPluginConfig config) {
      super(id, deps);
      this.version = version;
      this.config = config;
    }

    public FlywayPlugin withId(@NonNull String id) {
      return new FlywayPlugin(id, deps, version, config);
    }

    public FlywayPlugin withDeps(@NonNull List<@NonNull String> deps) {
      return new FlywayPlugin(id, deps, version, config);
    }

    public FlywayPlugin withVersion(@NonNull String version) {
      return new FlywayPlugin(id, deps, version, config);
    }

    public FlywayPlugin withConfig(@NonNull FlywayPluginConfig config) {
      return new FlywayPlugin(id, deps, version, config);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (this.getClass() != obj.getClass()) return false;
      FlywayPlugin other = (FlywayPlugin) obj;
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
      builder.append(FlywayPlugin.class.getSimpleName()).append(" {");
      appendProperty(builder, "id", this.id);
      appendProperty(builder, "deps", this.deps);
      appendProperty(builder, "version", this.version);
      appendProperty(builder, "config", this.config);
      builder.append("\n}");
      return builder.toString();
    }
  }

  public static final class FlywayPluginConfig {
    public final @NonNull ModuleDefaults defaults;

    public final @NonNull Map<@NonNull String, @NonNull ModuleOverride> modules;

    public FlywayPluginConfig(@Named("defaults") @NonNull ModuleDefaults defaults,
        @Named("modules") @NonNull Map<@NonNull String, @NonNull ModuleOverride> modules) {
      this.defaults = defaults;
      this.modules = modules;
    }

    public FlywayPluginConfig withDefaults(@NonNull ModuleDefaults defaults) {
      return new FlywayPluginConfig(defaults, modules);
    }

    public FlywayPluginConfig withModules(
        @NonNull Map<@NonNull String, @NonNull ModuleOverride> modules) {
      return new FlywayPluginConfig(defaults, modules);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (this.getClass() != obj.getClass()) return false;
      FlywayPluginConfig other = (FlywayPluginConfig) obj;
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
      builder.append(FlywayPluginConfig.class.getSimpleName()).append(" {");
      appendProperty(builder, "defaults", this.defaults);
      appendProperty(builder, "modules", this.modules);
      builder.append("\n}");
      return builder.toString();
    }
  }

  public static final class ModuleDefaults {
    public final boolean enabled;

    public final @NonNull String url;

    public final @NonNull String user;

    public final @NonNull String password;

    public final @NonNull List<@NonNull String> locations;

    public final @NonNull List<@NonNull String> schemas;

    public final @NonNull List<@NonNull String> jdbcDeps;

    public final boolean cleanDisabled;

    public ModuleDefaults(@Named("enabled") boolean enabled,
        @Named("url") @NonNull String url,
        @Named("user") @NonNull String user,
        @Named("password") @NonNull String password,
        @Named("locations") @NonNull List<@NonNull String> locations,
        @Named("schemas") @NonNull List<@NonNull String> schemas,
        @Named("jdbcDeps") @NonNull List<@NonNull String> jdbcDeps,
        @Named("cleanDisabled") boolean cleanDisabled) {
      this.enabled = enabled;
      this.url = url;
      this.user = user;
      this.password = password;
      this.locations = locations;
      this.schemas = schemas;
      this.jdbcDeps = jdbcDeps;
      this.cleanDisabled = cleanDisabled;
    }

    public ModuleDefaults withEnabled(boolean enabled) {
      return new ModuleDefaults(enabled, url, user, password, locations, schemas, jdbcDeps, cleanDisabled);
    }

    public ModuleDefaults withUrl(@NonNull String url) {
      return new ModuleDefaults(enabled, url, user, password, locations, schemas, jdbcDeps, cleanDisabled);
    }

    public ModuleDefaults withUser(@NonNull String user) {
      return new ModuleDefaults(enabled, url, user, password, locations, schemas, jdbcDeps, cleanDisabled);
    }

    public ModuleDefaults withPassword(@NonNull String password) {
      return new ModuleDefaults(enabled, url, user, password, locations, schemas, jdbcDeps, cleanDisabled);
    }

    public ModuleDefaults withLocations(@NonNull List<@NonNull String> locations) {
      return new ModuleDefaults(enabled, url, user, password, locations, schemas, jdbcDeps, cleanDisabled);
    }

    public ModuleDefaults withSchemas(@NonNull List<@NonNull String> schemas) {
      return new ModuleDefaults(enabled, url, user, password, locations, schemas, jdbcDeps, cleanDisabled);
    }

    public ModuleDefaults withJdbcDeps(@NonNull List<@NonNull String> jdbcDeps) {
      return new ModuleDefaults(enabled, url, user, password, locations, schemas, jdbcDeps, cleanDisabled);
    }

    public ModuleDefaults withCleanDisabled(boolean cleanDisabled) {
      return new ModuleDefaults(enabled, url, user, password, locations, schemas, jdbcDeps, cleanDisabled);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (this.getClass() != obj.getClass()) return false;
      ModuleDefaults other = (ModuleDefaults) obj;
      if (!Objects.equals(this.enabled, other.enabled)) return false;
      if (!Objects.equals(this.url, other.url)) return false;
      if (!Objects.equals(this.user, other.user)) return false;
      if (!Objects.equals(this.password, other.password)) return false;
      if (!Objects.equals(this.locations, other.locations)) return false;
      if (!Objects.equals(this.schemas, other.schemas)) return false;
      if (!Objects.equals(this.jdbcDeps, other.jdbcDeps)) return false;
      if (!Objects.equals(this.cleanDisabled, other.cleanDisabled)) return false;
      return true;
    }

    @Override
    public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.enabled);
      result = 31 * result + Objects.hashCode(this.url);
      result = 31 * result + Objects.hashCode(this.user);
      result = 31 * result + Objects.hashCode(this.password);
      result = 31 * result + Objects.hashCode(this.locations);
      result = 31 * result + Objects.hashCode(this.schemas);
      result = 31 * result + Objects.hashCode(this.jdbcDeps);
      result = 31 * result + Objects.hashCode(this.cleanDisabled);
      return result;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder(400);
      builder.append(ModuleDefaults.class.getSimpleName()).append(" {");
      appendProperty(builder, "enabled", this.enabled);
      appendProperty(builder, "url", this.url);
      appendProperty(builder, "user", this.user);
      appendProperty(builder, "password", this.password);
      appendProperty(builder, "locations", this.locations);
      appendProperty(builder, "schemas", this.schemas);
      appendProperty(builder, "jdbcDeps", this.jdbcDeps);
      appendProperty(builder, "cleanDisabled", this.cleanDisabled);
      builder.append("\n}");
      return builder.toString();
    }
  }

  public static final class ModuleOverride {
    public final Boolean enabled;

    public final String url;

    public final String user;

    public final String password;

    public final List<@NonNull String> locations;

    public final List<@NonNull String> schemas;

    public final List<@NonNull String> jdbcDeps;

    public final Boolean cleanDisabled;

    public ModuleOverride(@Named("enabled") Boolean enabled,
        @Named("url") String url,
        @Named("user") String user,
        @Named("password") String password,
        @Named("locations") List<@NonNull String> locations,
        @Named("schemas") List<@NonNull String> schemas,
        @Named("jdbcDeps") List<@NonNull String> jdbcDeps,
        @Named("cleanDisabled") Boolean cleanDisabled) {
      this.enabled = enabled;
      this.url = url;
      this.user = user;
      this.password = password;
      this.locations = locations;
      this.schemas = schemas;
      this.jdbcDeps = jdbcDeps;
      this.cleanDisabled = cleanDisabled;
    }

    public ModuleOverride withEnabled(Boolean enabled) {
      return new ModuleOverride(enabled, url, user, password, locations, schemas, jdbcDeps, cleanDisabled);
    }

    public ModuleOverride withUrl(String url) {
      return new ModuleOverride(enabled, url, user, password, locations, schemas, jdbcDeps, cleanDisabled);
    }

    public ModuleOverride withUser(String user) {
      return new ModuleOverride(enabled, url, user, password, locations, schemas, jdbcDeps, cleanDisabled);
    }

    public ModuleOverride withPassword(String password) {
      return new ModuleOverride(enabled, url, user, password, locations, schemas, jdbcDeps, cleanDisabled);
    }

    public ModuleOverride withLocations(List<@NonNull String> locations) {
      return new ModuleOverride(enabled, url, user, password, locations, schemas, jdbcDeps, cleanDisabled);
    }

    public ModuleOverride withSchemas(List<@NonNull String> schemas) {
      return new ModuleOverride(enabled, url, user, password, locations, schemas, jdbcDeps, cleanDisabled);
    }

    public ModuleOverride withJdbcDeps(List<@NonNull String> jdbcDeps) {
      return new ModuleOverride(enabled, url, user, password, locations, schemas, jdbcDeps, cleanDisabled);
    }

    public ModuleOverride withCleanDisabled(Boolean cleanDisabled) {
      return new ModuleOverride(enabled, url, user, password, locations, schemas, jdbcDeps, cleanDisabled);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (this.getClass() != obj.getClass()) return false;
      ModuleOverride other = (ModuleOverride) obj;
      if (!Objects.equals(this.enabled, other.enabled)) return false;
      if (!Objects.equals(this.url, other.url)) return false;
      if (!Objects.equals(this.user, other.user)) return false;
      if (!Objects.equals(this.password, other.password)) return false;
      if (!Objects.equals(this.locations, other.locations)) return false;
      if (!Objects.equals(this.schemas, other.schemas)) return false;
      if (!Objects.equals(this.jdbcDeps, other.jdbcDeps)) return false;
      if (!Objects.equals(this.cleanDisabled, other.cleanDisabled)) return false;
      return true;
    }

    @Override
    public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.enabled);
      result = 31 * result + Objects.hashCode(this.url);
      result = 31 * result + Objects.hashCode(this.user);
      result = 31 * result + Objects.hashCode(this.password);
      result = 31 * result + Objects.hashCode(this.locations);
      result = 31 * result + Objects.hashCode(this.schemas);
      result = 31 * result + Objects.hashCode(this.jdbcDeps);
      result = 31 * result + Objects.hashCode(this.cleanDisabled);
      return result;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder(400);
      builder.append(ModuleOverride.class.getSimpleName()).append(" {");
      appendProperty(builder, "enabled", this.enabled);
      appendProperty(builder, "url", this.url);
      appendProperty(builder, "user", this.user);
      appendProperty(builder, "password", this.password);
      appendProperty(builder, "locations", this.locations);
      appendProperty(builder, "schemas", this.schemas);
      appendProperty(builder, "jdbcDeps", this.jdbcDeps);
      appendProperty(builder, "cleanDisabled", this.cleanDisabled);
      builder.append("\n}");
      return builder.toString();
    }
  }
}
