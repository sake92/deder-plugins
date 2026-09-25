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

public final class Squery {
  public final @NonNull SqueryPluginConfig config;

  public Squery(@Named("config") @NonNull SqueryPluginConfig config) {
    this.config = config;
  }

  public Squery withConfig(@NonNull SqueryPluginConfig config) {
    return new Squery(config);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (this.getClass() != obj.getClass()) return false;
    Squery other = (Squery) obj;
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
    builder.append(Squery.class.getSimpleName()).append(" {");
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

  public static final class SqueryPlugin extends DederPlugins.DederPlugin {
    public final @NonNull String version;

    public final @NonNull SqueryPluginConfig config;

    public SqueryPlugin(@Named("id") @NonNull String id,
        @Named("deps") @NonNull List<@NonNull String> deps,
        @Named("version") @NonNull String version,
        @Named("config") @NonNull SqueryPluginConfig config) {
      super(id, deps);
      this.version = version;
      this.config = config;
    }

    public SqueryPlugin withId(@NonNull String id) {
      return new SqueryPlugin(id, deps, version, config);
    }

    public SqueryPlugin withDeps(@NonNull List<@NonNull String> deps) {
      return new SqueryPlugin(id, deps, version, config);
    }

    public SqueryPlugin withVersion(@NonNull String version) {
      return new SqueryPlugin(id, deps, version, config);
    }

    public SqueryPlugin withConfig(@NonNull SqueryPluginConfig config) {
      return new SqueryPlugin(id, deps, version, config);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (this.getClass() != obj.getClass()) return false;
      SqueryPlugin other = (SqueryPlugin) obj;
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
      builder.append(SqueryPlugin.class.getSimpleName()).append(" {");
      appendProperty(builder, "id", this.id);
      appendProperty(builder, "deps", this.deps);
      appendProperty(builder, "version", this.version);
      appendProperty(builder, "config", this.config);
      builder.append("\n}");
      return builder.toString();
    }
  }

  public static final class SqueryPluginConfig {
    public final @NonNull ModuleDefaults defaults;

    public final @NonNull Map<@NonNull String, @NonNull ModuleOverride> modules;

    public SqueryPluginConfig(@Named("defaults") @NonNull ModuleDefaults defaults,
        @Named("modules") @NonNull Map<@NonNull String, @NonNull ModuleOverride> modules) {
      this.defaults = defaults;
      this.modules = modules;
    }

    public SqueryPluginConfig withDefaults(@NonNull ModuleDefaults defaults) {
      return new SqueryPluginConfig(defaults, modules);
    }

    public SqueryPluginConfig withModules(
        @NonNull Map<@NonNull String, @NonNull ModuleOverride> modules) {
      return new SqueryPluginConfig(defaults, modules);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (this.getClass() != obj.getClass()) return false;
      SqueryPluginConfig other = (SqueryPluginConfig) obj;
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
      builder.append(SqueryPluginConfig.class.getSimpleName()).append(" {");
      appendProperty(builder, "defaults", this.defaults);
      appendProperty(builder, "modules", this.modules);
      builder.append("\n}");
      return builder.toString();
    }
  }

  public static final class ModuleDefaults {
    public final boolean enabled;

    public final @NonNull String jdbcUrl;

    public final @NonNull List<@NonNull String> jdbcDeps;

    public final @NonNull Map<@NonNull String, @NonNull String> schemaMappings;

    public final @NonNull String colNameIdentifierMapper;

    public final @NonNull String typeNameMapper;

    public final @NonNull String rowTypeSuffix;

    public final @NonNull String daoTypeSuffix;

    public final @NonNull List<@NonNull String> typeMappingRules;

    public final @NonNull List<@NonNull String> includeTables;

    public final @NonNull List<@NonNull String> excludeTables;

    public final @NonNull String squeryVersion;

    public ModuleDefaults(@Named("enabled") boolean enabled,
        @Named("jdbcUrl") @NonNull String jdbcUrl,
        @Named("jdbcDeps") @NonNull List<@NonNull String> jdbcDeps,
        @Named("schemaMappings") @NonNull Map<@NonNull String, @NonNull String> schemaMappings,
        @Named("colNameIdentifierMapper") @NonNull String colNameIdentifierMapper,
        @Named("typeNameMapper") @NonNull String typeNameMapper,
        @Named("rowTypeSuffix") @NonNull String rowTypeSuffix,
        @Named("daoTypeSuffix") @NonNull String daoTypeSuffix,
        @Named("typeMappingRules") @NonNull List<@NonNull String> typeMappingRules,
        @Named("includeTables") @NonNull List<@NonNull String> includeTables,
        @Named("excludeTables") @NonNull List<@NonNull String> excludeTables,
        @Named("squeryVersion") @NonNull String squeryVersion) {
      this.enabled = enabled;
      this.jdbcUrl = jdbcUrl;
      this.jdbcDeps = jdbcDeps;
      this.schemaMappings = schemaMappings;
      this.colNameIdentifierMapper = colNameIdentifierMapper;
      this.typeNameMapper = typeNameMapper;
      this.rowTypeSuffix = rowTypeSuffix;
      this.daoTypeSuffix = daoTypeSuffix;
      this.typeMappingRules = typeMappingRules;
      this.includeTables = includeTables;
      this.excludeTables = excludeTables;
      this.squeryVersion = squeryVersion;
    }

    public ModuleDefaults withEnabled(boolean enabled) {
      return new ModuleDefaults(enabled, jdbcUrl, jdbcDeps, schemaMappings, colNameIdentifierMapper, typeNameMapper, rowTypeSuffix, daoTypeSuffix, typeMappingRules, includeTables, excludeTables, squeryVersion);
    }

    public ModuleDefaults withJdbcUrl(@NonNull String jdbcUrl) {
      return new ModuleDefaults(enabled, jdbcUrl, jdbcDeps, schemaMappings, colNameIdentifierMapper, typeNameMapper, rowTypeSuffix, daoTypeSuffix, typeMappingRules, includeTables, excludeTables, squeryVersion);
    }

    public ModuleDefaults withJdbcDeps(@NonNull List<@NonNull String> jdbcDeps) {
      return new ModuleDefaults(enabled, jdbcUrl, jdbcDeps, schemaMappings, colNameIdentifierMapper, typeNameMapper, rowTypeSuffix, daoTypeSuffix, typeMappingRules, includeTables, excludeTables, squeryVersion);
    }

    public ModuleDefaults withSchemaMappings(
        @NonNull Map<@NonNull String, @NonNull String> schemaMappings) {
      return new ModuleDefaults(enabled, jdbcUrl, jdbcDeps, schemaMappings, colNameIdentifierMapper, typeNameMapper, rowTypeSuffix, daoTypeSuffix, typeMappingRules, includeTables, excludeTables, squeryVersion);
    }

    public ModuleDefaults withColNameIdentifierMapper(@NonNull String colNameIdentifierMapper) {
      return new ModuleDefaults(enabled, jdbcUrl, jdbcDeps, schemaMappings, colNameIdentifierMapper, typeNameMapper, rowTypeSuffix, daoTypeSuffix, typeMappingRules, includeTables, excludeTables, squeryVersion);
    }

    public ModuleDefaults withTypeNameMapper(@NonNull String typeNameMapper) {
      return new ModuleDefaults(enabled, jdbcUrl, jdbcDeps, schemaMappings, colNameIdentifierMapper, typeNameMapper, rowTypeSuffix, daoTypeSuffix, typeMappingRules, includeTables, excludeTables, squeryVersion);
    }

    public ModuleDefaults withRowTypeSuffix(@NonNull String rowTypeSuffix) {
      return new ModuleDefaults(enabled, jdbcUrl, jdbcDeps, schemaMappings, colNameIdentifierMapper, typeNameMapper, rowTypeSuffix, daoTypeSuffix, typeMappingRules, includeTables, excludeTables, squeryVersion);
    }

    public ModuleDefaults withDaoTypeSuffix(@NonNull String daoTypeSuffix) {
      return new ModuleDefaults(enabled, jdbcUrl, jdbcDeps, schemaMappings, colNameIdentifierMapper, typeNameMapper, rowTypeSuffix, daoTypeSuffix, typeMappingRules, includeTables, excludeTables, squeryVersion);
    }

    public ModuleDefaults withTypeMappingRules(@NonNull List<@NonNull String> typeMappingRules) {
      return new ModuleDefaults(enabled, jdbcUrl, jdbcDeps, schemaMappings, colNameIdentifierMapper, typeNameMapper, rowTypeSuffix, daoTypeSuffix, typeMappingRules, includeTables, excludeTables, squeryVersion);
    }

    public ModuleDefaults withIncludeTables(@NonNull List<@NonNull String> includeTables) {
      return new ModuleDefaults(enabled, jdbcUrl, jdbcDeps, schemaMappings, colNameIdentifierMapper, typeNameMapper, rowTypeSuffix, daoTypeSuffix, typeMappingRules, includeTables, excludeTables, squeryVersion);
    }

    public ModuleDefaults withExcludeTables(@NonNull List<@NonNull String> excludeTables) {
      return new ModuleDefaults(enabled, jdbcUrl, jdbcDeps, schemaMappings, colNameIdentifierMapper, typeNameMapper, rowTypeSuffix, daoTypeSuffix, typeMappingRules, includeTables, excludeTables, squeryVersion);
    }

    public ModuleDefaults withSqueryVersion(@NonNull String squeryVersion) {
      return new ModuleDefaults(enabled, jdbcUrl, jdbcDeps, schemaMappings, colNameIdentifierMapper, typeNameMapper, rowTypeSuffix, daoTypeSuffix, typeMappingRules, includeTables, excludeTables, squeryVersion);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (this.getClass() != obj.getClass()) return false;
      ModuleDefaults other = (ModuleDefaults) obj;
      if (!Objects.equals(this.enabled, other.enabled)) return false;
      if (!Objects.equals(this.jdbcUrl, other.jdbcUrl)) return false;
      if (!Objects.equals(this.jdbcDeps, other.jdbcDeps)) return false;
      if (!Objects.equals(this.schemaMappings, other.schemaMappings)) return false;
      if (!Objects.equals(this.colNameIdentifierMapper, other.colNameIdentifierMapper)) return false;
      if (!Objects.equals(this.typeNameMapper, other.typeNameMapper)) return false;
      if (!Objects.equals(this.rowTypeSuffix, other.rowTypeSuffix)) return false;
      if (!Objects.equals(this.daoTypeSuffix, other.daoTypeSuffix)) return false;
      if (!Objects.equals(this.typeMappingRules, other.typeMappingRules)) return false;
      if (!Objects.equals(this.includeTables, other.includeTables)) return false;
      if (!Objects.equals(this.excludeTables, other.excludeTables)) return false;
      if (!Objects.equals(this.squeryVersion, other.squeryVersion)) return false;
      return true;
    }

    @Override
    public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.enabled);
      result = 31 * result + Objects.hashCode(this.jdbcUrl);
      result = 31 * result + Objects.hashCode(this.jdbcDeps);
      result = 31 * result + Objects.hashCode(this.schemaMappings);
      result = 31 * result + Objects.hashCode(this.colNameIdentifierMapper);
      result = 31 * result + Objects.hashCode(this.typeNameMapper);
      result = 31 * result + Objects.hashCode(this.rowTypeSuffix);
      result = 31 * result + Objects.hashCode(this.daoTypeSuffix);
      result = 31 * result + Objects.hashCode(this.typeMappingRules);
      result = 31 * result + Objects.hashCode(this.includeTables);
      result = 31 * result + Objects.hashCode(this.excludeTables);
      result = 31 * result + Objects.hashCode(this.squeryVersion);
      return result;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder(650);
      builder.append(ModuleDefaults.class.getSimpleName()).append(" {");
      appendProperty(builder, "enabled", this.enabled);
      appendProperty(builder, "jdbcUrl", this.jdbcUrl);
      appendProperty(builder, "jdbcDeps", this.jdbcDeps);
      appendProperty(builder, "schemaMappings", this.schemaMappings);
      appendProperty(builder, "colNameIdentifierMapper", this.colNameIdentifierMapper);
      appendProperty(builder, "typeNameMapper", this.typeNameMapper);
      appendProperty(builder, "rowTypeSuffix", this.rowTypeSuffix);
      appendProperty(builder, "daoTypeSuffix", this.daoTypeSuffix);
      appendProperty(builder, "typeMappingRules", this.typeMappingRules);
      appendProperty(builder, "includeTables", this.includeTables);
      appendProperty(builder, "excludeTables", this.excludeTables);
      appendProperty(builder, "squeryVersion", this.squeryVersion);
      builder.append("\n}");
      return builder.toString();
    }
  }

  public static final class ModuleOverride {
    public final Boolean enabled;

    public final String jdbcUrl;

    public final List<@NonNull String> jdbcDeps;

    public final Map<@NonNull String, @NonNull String> schemaMappings;

    public final String colNameIdentifierMapper;

    public final String typeNameMapper;

    public final String rowTypeSuffix;

    public final String daoTypeSuffix;

    public final List<@NonNull String> typeMappingRules;

    public final List<@NonNull String> includeTables;

    public final List<@NonNull String> excludeTables;

    public final String squeryVersion;

    public ModuleOverride(@Named("enabled") Boolean enabled, @Named("jdbcUrl") String jdbcUrl,
        @Named("jdbcDeps") List<@NonNull String> jdbcDeps,
        @Named("schemaMappings") Map<@NonNull String, @NonNull String> schemaMappings,
        @Named("colNameIdentifierMapper") String colNameIdentifierMapper,
        @Named("typeNameMapper") String typeNameMapper,
        @Named("rowTypeSuffix") String rowTypeSuffix, @Named("daoTypeSuffix") String daoTypeSuffix,
        @Named("typeMappingRules") List<@NonNull String> typeMappingRules,
        @Named("includeTables") List<@NonNull String> includeTables,
        @Named("excludeTables") List<@NonNull String> excludeTables,
        @Named("squeryVersion") String squeryVersion) {
      this.enabled = enabled;
      this.jdbcUrl = jdbcUrl;
      this.jdbcDeps = jdbcDeps;
      this.schemaMappings = schemaMappings;
      this.colNameIdentifierMapper = colNameIdentifierMapper;
      this.typeNameMapper = typeNameMapper;
      this.rowTypeSuffix = rowTypeSuffix;
      this.daoTypeSuffix = daoTypeSuffix;
      this.typeMappingRules = typeMappingRules;
      this.includeTables = includeTables;
      this.excludeTables = excludeTables;
      this.squeryVersion = squeryVersion;
    }

    public ModuleOverride withEnabled(Boolean enabled) {
      return new ModuleOverride(enabled, jdbcUrl, jdbcDeps, schemaMappings, colNameIdentifierMapper, typeNameMapper, rowTypeSuffix, daoTypeSuffix, typeMappingRules, includeTables, excludeTables, squeryVersion);
    }

    public ModuleOverride withJdbcUrl(String jdbcUrl) {
      return new ModuleOverride(enabled, jdbcUrl, jdbcDeps, schemaMappings, colNameIdentifierMapper, typeNameMapper, rowTypeSuffix, daoTypeSuffix, typeMappingRules, includeTables, excludeTables, squeryVersion);
    }

    public ModuleOverride withJdbcDeps(List<@NonNull String> jdbcDeps) {
      return new ModuleOverride(enabled, jdbcUrl, jdbcDeps, schemaMappings, colNameIdentifierMapper, typeNameMapper, rowTypeSuffix, daoTypeSuffix, typeMappingRules, includeTables, excludeTables, squeryVersion);
    }

    public ModuleOverride withSchemaMappings(Map<@NonNull String, @NonNull String> schemaMappings) {
      return new ModuleOverride(enabled, jdbcUrl, jdbcDeps, schemaMappings, colNameIdentifierMapper, typeNameMapper, rowTypeSuffix, daoTypeSuffix, typeMappingRules, includeTables, excludeTables, squeryVersion);
    }

    public ModuleOverride withColNameIdentifierMapper(String colNameIdentifierMapper) {
      return new ModuleOverride(enabled, jdbcUrl, jdbcDeps, schemaMappings, colNameIdentifierMapper, typeNameMapper, rowTypeSuffix, daoTypeSuffix, typeMappingRules, includeTables, excludeTables, squeryVersion);
    }

    public ModuleOverride withTypeNameMapper(String typeNameMapper) {
      return new ModuleOverride(enabled, jdbcUrl, jdbcDeps, schemaMappings, colNameIdentifierMapper, typeNameMapper, rowTypeSuffix, daoTypeSuffix, typeMappingRules, includeTables, excludeTables, squeryVersion);
    }

    public ModuleOverride withRowTypeSuffix(String rowTypeSuffix) {
      return new ModuleOverride(enabled, jdbcUrl, jdbcDeps, schemaMappings, colNameIdentifierMapper, typeNameMapper, rowTypeSuffix, daoTypeSuffix, typeMappingRules, includeTables, excludeTables, squeryVersion);
    }

    public ModuleOverride withDaoTypeSuffix(String daoTypeSuffix) {
      return new ModuleOverride(enabled, jdbcUrl, jdbcDeps, schemaMappings, colNameIdentifierMapper, typeNameMapper, rowTypeSuffix, daoTypeSuffix, typeMappingRules, includeTables, excludeTables, squeryVersion);
    }

    public ModuleOverride withTypeMappingRules(List<@NonNull String> typeMappingRules) {
      return new ModuleOverride(enabled, jdbcUrl, jdbcDeps, schemaMappings, colNameIdentifierMapper, typeNameMapper, rowTypeSuffix, daoTypeSuffix, typeMappingRules, includeTables, excludeTables, squeryVersion);
    }

    public ModuleOverride withIncludeTables(List<@NonNull String> includeTables) {
      return new ModuleOverride(enabled, jdbcUrl, jdbcDeps, schemaMappings, colNameIdentifierMapper, typeNameMapper, rowTypeSuffix, daoTypeSuffix, typeMappingRules, includeTables, excludeTables, squeryVersion);
    }

    public ModuleOverride withExcludeTables(List<@NonNull String> excludeTables) {
      return new ModuleOverride(enabled, jdbcUrl, jdbcDeps, schemaMappings, colNameIdentifierMapper, typeNameMapper, rowTypeSuffix, daoTypeSuffix, typeMappingRules, includeTables, excludeTables, squeryVersion);
    }

    public ModuleOverride withSqueryVersion(String squeryVersion) {
      return new ModuleOverride(enabled, jdbcUrl, jdbcDeps, schemaMappings, colNameIdentifierMapper, typeNameMapper, rowTypeSuffix, daoTypeSuffix, typeMappingRules, includeTables, excludeTables, squeryVersion);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (this.getClass() != obj.getClass()) return false;
      ModuleOverride other = (ModuleOverride) obj;
      if (!Objects.equals(this.enabled, other.enabled)) return false;
      if (!Objects.equals(this.jdbcUrl, other.jdbcUrl)) return false;
      if (!Objects.equals(this.jdbcDeps, other.jdbcDeps)) return false;
      if (!Objects.equals(this.schemaMappings, other.schemaMappings)) return false;
      if (!Objects.equals(this.colNameIdentifierMapper, other.colNameIdentifierMapper)) return false;
      if (!Objects.equals(this.typeNameMapper, other.typeNameMapper)) return false;
      if (!Objects.equals(this.rowTypeSuffix, other.rowTypeSuffix)) return false;
      if (!Objects.equals(this.daoTypeSuffix, other.daoTypeSuffix)) return false;
      if (!Objects.equals(this.typeMappingRules, other.typeMappingRules)) return false;
      if (!Objects.equals(this.includeTables, other.includeTables)) return false;
      if (!Objects.equals(this.excludeTables, other.excludeTables)) return false;
      if (!Objects.equals(this.squeryVersion, other.squeryVersion)) return false;
      return true;
    }

    @Override
    public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.enabled);
      result = 31 * result + Objects.hashCode(this.jdbcUrl);
      result = 31 * result + Objects.hashCode(this.jdbcDeps);
      result = 31 * result + Objects.hashCode(this.schemaMappings);
      result = 31 * result + Objects.hashCode(this.colNameIdentifierMapper);
      result = 31 * result + Objects.hashCode(this.typeNameMapper);
      result = 31 * result + Objects.hashCode(this.rowTypeSuffix);
      result = 31 * result + Objects.hashCode(this.daoTypeSuffix);
      result = 31 * result + Objects.hashCode(this.typeMappingRules);
      result = 31 * result + Objects.hashCode(this.includeTables);
      result = 31 * result + Objects.hashCode(this.excludeTables);
      result = 31 * result + Objects.hashCode(this.squeryVersion);
      return result;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder(650);
      builder.append(ModuleOverride.class.getSimpleName()).append(" {");
      appendProperty(builder, "enabled", this.enabled);
      appendProperty(builder, "jdbcUrl", this.jdbcUrl);
      appendProperty(builder, "jdbcDeps", this.jdbcDeps);
      appendProperty(builder, "schemaMappings", this.schemaMappings);
      appendProperty(builder, "colNameIdentifierMapper", this.colNameIdentifierMapper);
      appendProperty(builder, "typeNameMapper", this.typeNameMapper);
      appendProperty(builder, "rowTypeSuffix", this.rowTypeSuffix);
      appendProperty(builder, "daoTypeSuffix", this.daoTypeSuffix);
      appendProperty(builder, "typeMappingRules", this.typeMappingRules);
      appendProperty(builder, "includeTables", this.includeTables);
      appendProperty(builder, "excludeTables", this.excludeTables);
      appendProperty(builder, "squeryVersion", this.squeryVersion);
      builder.append("\n}");
      return builder.toString();
    }
  }
}
