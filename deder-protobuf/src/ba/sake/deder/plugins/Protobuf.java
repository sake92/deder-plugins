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

public final class Protobuf {
  public final @NonNull ProtobufPluginConfig config;

  public Protobuf(@Named("config") @NonNull ProtobufPluginConfig config) {
    this.config = config;
  }

  public Protobuf withConfig(@NonNull ProtobufPluginConfig config) {
    return new Protobuf(config);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (this.getClass() != obj.getClass()) return false;
    Protobuf other = (Protobuf) obj;
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
    builder.append(Protobuf.class.getSimpleName()).append(" {");
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

  public static final class ProtobufPlugin extends DederPlugins.DederPlugin {
    public final @NonNull String version;

    public final @NonNull ProtobufPluginConfig config;

    public ProtobufPlugin(@Named("id") @NonNull String id,
        @Named("deps") @NonNull List<@NonNull String> deps,
        @Named("version") @NonNull String version,
        @Named("config") @NonNull ProtobufPluginConfig config) {
      super(id, deps);
      this.version = version;
      this.config = config;
    }

    public ProtobufPlugin withId(@NonNull String id) {
      return new ProtobufPlugin(id, deps, version, config);
    }

    public ProtobufPlugin withDeps(@NonNull List<@NonNull String> deps) {
      return new ProtobufPlugin(id, deps, version, config);
    }

    public ProtobufPlugin withVersion(@NonNull String version) {
      return new ProtobufPlugin(id, deps, version, config);
    }

    public ProtobufPlugin withConfig(@NonNull ProtobufPluginConfig config) {
      return new ProtobufPlugin(id, deps, version, config);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (this.getClass() != obj.getClass()) return false;
      ProtobufPlugin other = (ProtobufPlugin) obj;
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
      builder.append(ProtobufPlugin.class.getSimpleName()).append(" {");
      appendProperty(builder, "id", this.id);
      appendProperty(builder, "deps", this.deps);
      appendProperty(builder, "version", this.version);
      appendProperty(builder, "config", this.config);
      builder.append("\n}");
      return builder.toString();
    }
  }

  public static final class ProtobufPluginConfig {
    public final @NonNull ModuleDefaults defaults;

    public final @NonNull Map<@NonNull String, @NonNull ModuleOverride> modules;

    public ProtobufPluginConfig(@Named("defaults") @NonNull ModuleDefaults defaults,
        @Named("modules") @NonNull Map<@NonNull String, @NonNull ModuleOverride> modules) {
      this.defaults = defaults;
      this.modules = modules;
    }

    public ProtobufPluginConfig withDefaults(@NonNull ModuleDefaults defaults) {
      return new ProtobufPluginConfig(defaults, modules);
    }

    public ProtobufPluginConfig withModules(
        @NonNull Map<@NonNull String, @NonNull ModuleOverride> modules) {
      return new ProtobufPluginConfig(defaults, modules);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (this.getClass() != obj.getClass()) return false;
      ProtobufPluginConfig other = (ProtobufPluginConfig) obj;
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
      builder.append(ProtobufPluginConfig.class.getSimpleName()).append(" {");
      appendProperty(builder, "defaults", this.defaults);
      appendProperty(builder, "modules", this.modules);
      builder.append("\n}");
      return builder.toString();
    }
  }

  public static final class ModuleDefaults {
    public final boolean enabled;

    public final @NonNull ResolverConfig protoc;

    public final @NonNull List<@NonNull BuiltinLanguageTarget> builtins;

    public final @NonNull List<@NonNull PluginTarget> plugins;

    public final @NonNull ImportConfig imports;

    public final DescriptorConfig descriptor;

    public final @NonNull SourceSetConfig main;

    public final @NonNull SourceSetConfig test;

    public ModuleDefaults(@Named("enabled") boolean enabled,
        @Named("protoc") @NonNull ResolverConfig protoc,
        @Named("builtins") @NonNull List<@NonNull BuiltinLanguageTarget> builtins,
        @Named("plugins") @NonNull List<@NonNull PluginTarget> plugins,
        @Named("imports") @NonNull ImportConfig imports,
        @Named("descriptor") DescriptorConfig descriptor,
        @Named("main") @NonNull SourceSetConfig main,
        @Named("test") @NonNull SourceSetConfig test) {
      this.enabled = enabled;
      this.protoc = protoc;
      this.builtins = builtins;
      this.plugins = plugins;
      this.imports = imports;
      this.descriptor = descriptor;
      this.main = main;
      this.test = test;
    }

    public ModuleDefaults withEnabled(boolean enabled) {
      return new ModuleDefaults(enabled, protoc, builtins, plugins, imports, descriptor, main, test);
    }

    public ModuleDefaults withProtoc(@NonNull ResolverConfig protoc) {
      return new ModuleDefaults(enabled, protoc, builtins, plugins, imports, descriptor, main, test);
    }

    public ModuleDefaults withBuiltins(@NonNull List<@NonNull BuiltinLanguageTarget> builtins) {
      return new ModuleDefaults(enabled, protoc, builtins, plugins, imports, descriptor, main, test);
    }

    public ModuleDefaults withPlugins(@NonNull List<@NonNull PluginTarget> plugins) {
      return new ModuleDefaults(enabled, protoc, builtins, plugins, imports, descriptor, main, test);
    }

    public ModuleDefaults withImports(@NonNull ImportConfig imports) {
      return new ModuleDefaults(enabled, protoc, builtins, plugins, imports, descriptor, main, test);
    }

    public ModuleDefaults withDescriptor(DescriptorConfig descriptor) {
      return new ModuleDefaults(enabled, protoc, builtins, plugins, imports, descriptor, main, test);
    }

    public ModuleDefaults withMain(@NonNull SourceSetConfig main) {
      return new ModuleDefaults(enabled, protoc, builtins, plugins, imports, descriptor, main, test);
    }

    public ModuleDefaults withTest(@NonNull SourceSetConfig test) {
      return new ModuleDefaults(enabled, protoc, builtins, plugins, imports, descriptor, main, test);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (this.getClass() != obj.getClass()) return false;
      ModuleDefaults other = (ModuleDefaults) obj;
      if (!Objects.equals(this.enabled, other.enabled)) return false;
      if (!Objects.equals(this.protoc, other.protoc)) return false;
      if (!Objects.equals(this.builtins, other.builtins)) return false;
      if (!Objects.equals(this.plugins, other.plugins)) return false;
      if (!Objects.equals(this.imports, other.imports)) return false;
      if (!Objects.equals(this.descriptor, other.descriptor)) return false;
      if (!Objects.equals(this.main, other.main)) return false;
      if (!Objects.equals(this.test, other.test)) return false;
      return true;
    }

    @Override
    public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.enabled);
      result = 31 * result + Objects.hashCode(this.protoc);
      result = 31 * result + Objects.hashCode(this.builtins);
      result = 31 * result + Objects.hashCode(this.plugins);
      result = 31 * result + Objects.hashCode(this.imports);
      result = 31 * result + Objects.hashCode(this.descriptor);
      result = 31 * result + Objects.hashCode(this.main);
      result = 31 * result + Objects.hashCode(this.test);
      return result;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder(450);
      builder.append(ModuleDefaults.class.getSimpleName()).append(" {");
      appendProperty(builder, "enabled", this.enabled);
      appendProperty(builder, "protoc", this.protoc);
      appendProperty(builder, "builtins", this.builtins);
      appendProperty(builder, "plugins", this.plugins);
      appendProperty(builder, "imports", this.imports);
      appendProperty(builder, "descriptor", this.descriptor);
      appendProperty(builder, "main", this.main);
      appendProperty(builder, "test", this.test);
      builder.append("\n}");
      return builder.toString();
    }
  }

  public static final class ModuleOverride {
    public final Boolean enabled;

    public final ResolverConfig protoc;

    public final List<@NonNull BuiltinLanguageTarget> builtins;

    public final List<@NonNull PluginTarget> plugins;

    public final ImportConfig imports;

    public final DescriptorConfig descriptor;

    public final SourceSetOverride main;

    public final SourceSetOverride test;

    public ModuleOverride(@Named("enabled") Boolean enabled, @Named("protoc") ResolverConfig protoc,
        @Named("builtins") List<@NonNull BuiltinLanguageTarget> builtins,
        @Named("plugins") List<@NonNull PluginTarget> plugins,
        @Named("imports") ImportConfig imports, @Named("descriptor") DescriptorConfig descriptor,
        @Named("main") SourceSetOverride main, @Named("test") SourceSetOverride test) {
      this.enabled = enabled;
      this.protoc = protoc;
      this.builtins = builtins;
      this.plugins = plugins;
      this.imports = imports;
      this.descriptor = descriptor;
      this.main = main;
      this.test = test;
    }

    public ModuleOverride withEnabled(Boolean enabled) {
      return new ModuleOverride(enabled, protoc, builtins, plugins, imports, descriptor, main, test);
    }

    public ModuleOverride withProtoc(ResolverConfig protoc) {
      return new ModuleOverride(enabled, protoc, builtins, plugins, imports, descriptor, main, test);
    }

    public ModuleOverride withBuiltins(List<@NonNull BuiltinLanguageTarget> builtins) {
      return new ModuleOverride(enabled, protoc, builtins, plugins, imports, descriptor, main, test);
    }

    public ModuleOverride withPlugins(List<@NonNull PluginTarget> plugins) {
      return new ModuleOverride(enabled, protoc, builtins, plugins, imports, descriptor, main, test);
    }

    public ModuleOverride withImports(ImportConfig imports) {
      return new ModuleOverride(enabled, protoc, builtins, plugins, imports, descriptor, main, test);
    }

    public ModuleOverride withDescriptor(DescriptorConfig descriptor) {
      return new ModuleOverride(enabled, protoc, builtins, plugins, imports, descriptor, main, test);
    }

    public ModuleOverride withMain(SourceSetOverride main) {
      return new ModuleOverride(enabled, protoc, builtins, plugins, imports, descriptor, main, test);
    }

    public ModuleOverride withTest(SourceSetOverride test) {
      return new ModuleOverride(enabled, protoc, builtins, plugins, imports, descriptor, main, test);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (this.getClass() != obj.getClass()) return false;
      ModuleOverride other = (ModuleOverride) obj;
      if (!Objects.equals(this.enabled, other.enabled)) return false;
      if (!Objects.equals(this.protoc, other.protoc)) return false;
      if (!Objects.equals(this.builtins, other.builtins)) return false;
      if (!Objects.equals(this.plugins, other.plugins)) return false;
      if (!Objects.equals(this.imports, other.imports)) return false;
      if (!Objects.equals(this.descriptor, other.descriptor)) return false;
      if (!Objects.equals(this.main, other.main)) return false;
      if (!Objects.equals(this.test, other.test)) return false;
      return true;
    }

    @Override
    public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.enabled);
      result = 31 * result + Objects.hashCode(this.protoc);
      result = 31 * result + Objects.hashCode(this.builtins);
      result = 31 * result + Objects.hashCode(this.plugins);
      result = 31 * result + Objects.hashCode(this.imports);
      result = 31 * result + Objects.hashCode(this.descriptor);
      result = 31 * result + Objects.hashCode(this.main);
      result = 31 * result + Objects.hashCode(this.test);
      return result;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder(450);
      builder.append(ModuleOverride.class.getSimpleName()).append(" {");
      appendProperty(builder, "enabled", this.enabled);
      appendProperty(builder, "protoc", this.protoc);
      appendProperty(builder, "builtins", this.builtins);
      appendProperty(builder, "plugins", this.plugins);
      appendProperty(builder, "imports", this.imports);
      appendProperty(builder, "descriptor", this.descriptor);
      appendProperty(builder, "main", this.main);
      appendProperty(builder, "test", this.test);
      builder.append("\n}");
      return builder.toString();
    }
  }

  public static final class SourceSetConfig {
    public final boolean enabled;

    public final @NonNull List<@NonNull String> sourceDirs;

    public final @NonNull List<@NonNull String> includeGlobs;

    public final @NonNull List<@NonNull String> excludeGlobs;

    public final @NonNull List<@NonNull String> extraImportPaths;

    public final @NonNull List<@NonNull String> extraImportDeps;

    public final @NonNull List<@NonNull String> extraArgs;

    public final @NonNull Map<@NonNull String, @NonNull String> env;

    public SourceSetConfig(@Named("enabled") boolean enabled,
        @Named("sourceDirs") @NonNull List<@NonNull String> sourceDirs,
        @Named("includeGlobs") @NonNull List<@NonNull String> includeGlobs,
        @Named("excludeGlobs") @NonNull List<@NonNull String> excludeGlobs,
        @Named("extraImportPaths") @NonNull List<@NonNull String> extraImportPaths,
        @Named("extraImportDeps") @NonNull List<@NonNull String> extraImportDeps,
        @Named("extraArgs") @NonNull List<@NonNull String> extraArgs,
        @Named("env") @NonNull Map<@NonNull String, @NonNull String> env) {
      this.enabled = enabled;
      this.sourceDirs = sourceDirs;
      this.includeGlobs = includeGlobs;
      this.excludeGlobs = excludeGlobs;
      this.extraImportPaths = extraImportPaths;
      this.extraImportDeps = extraImportDeps;
      this.extraArgs = extraArgs;
      this.env = env;
    }

    public SourceSetConfig withEnabled(boolean enabled) {
      return new SourceSetConfig(enabled, sourceDirs, includeGlobs, excludeGlobs, extraImportPaths, extraImportDeps, extraArgs, env);
    }

    public SourceSetConfig withSourceDirs(@NonNull List<@NonNull String> sourceDirs) {
      return new SourceSetConfig(enabled, sourceDirs, includeGlobs, excludeGlobs, extraImportPaths, extraImportDeps, extraArgs, env);
    }

    public SourceSetConfig withIncludeGlobs(@NonNull List<@NonNull String> includeGlobs) {
      return new SourceSetConfig(enabled, sourceDirs, includeGlobs, excludeGlobs, extraImportPaths, extraImportDeps, extraArgs, env);
    }

    public SourceSetConfig withExcludeGlobs(@NonNull List<@NonNull String> excludeGlobs) {
      return new SourceSetConfig(enabled, sourceDirs, includeGlobs, excludeGlobs, extraImportPaths, extraImportDeps, extraArgs, env);
    }

    public SourceSetConfig withExtraImportPaths(@NonNull List<@NonNull String> extraImportPaths) {
      return new SourceSetConfig(enabled, sourceDirs, includeGlobs, excludeGlobs, extraImportPaths, extraImportDeps, extraArgs, env);
    }

    public SourceSetConfig withExtraImportDeps(@NonNull List<@NonNull String> extraImportDeps) {
      return new SourceSetConfig(enabled, sourceDirs, includeGlobs, excludeGlobs, extraImportPaths, extraImportDeps, extraArgs, env);
    }

    public SourceSetConfig withExtraArgs(@NonNull List<@NonNull String> extraArgs) {
      return new SourceSetConfig(enabled, sourceDirs, includeGlobs, excludeGlobs, extraImportPaths, extraImportDeps, extraArgs, env);
    }

    public SourceSetConfig withEnv(@NonNull Map<@NonNull String, @NonNull String> env) {
      return new SourceSetConfig(enabled, sourceDirs, includeGlobs, excludeGlobs, extraImportPaths, extraImportDeps, extraArgs, env);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (this.getClass() != obj.getClass()) return false;
      SourceSetConfig other = (SourceSetConfig) obj;
      if (!Objects.equals(this.enabled, other.enabled)) return false;
      if (!Objects.equals(this.sourceDirs, other.sourceDirs)) return false;
      if (!Objects.equals(this.includeGlobs, other.includeGlobs)) return false;
      if (!Objects.equals(this.excludeGlobs, other.excludeGlobs)) return false;
      if (!Objects.equals(this.extraImportPaths, other.extraImportPaths)) return false;
      if (!Objects.equals(this.extraImportDeps, other.extraImportDeps)) return false;
      if (!Objects.equals(this.extraArgs, other.extraArgs)) return false;
      if (!Objects.equals(this.env, other.env)) return false;
      return true;
    }

    @Override
    public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.enabled);
      result = 31 * result + Objects.hashCode(this.sourceDirs);
      result = 31 * result + Objects.hashCode(this.includeGlobs);
      result = 31 * result + Objects.hashCode(this.excludeGlobs);
      result = 31 * result + Objects.hashCode(this.extraImportPaths);
      result = 31 * result + Objects.hashCode(this.extraImportDeps);
      result = 31 * result + Objects.hashCode(this.extraArgs);
      result = 31 * result + Objects.hashCode(this.env);
      return result;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder(450);
      builder.append(SourceSetConfig.class.getSimpleName()).append(" {");
      appendProperty(builder, "enabled", this.enabled);
      appendProperty(builder, "sourceDirs", this.sourceDirs);
      appendProperty(builder, "includeGlobs", this.includeGlobs);
      appendProperty(builder, "excludeGlobs", this.excludeGlobs);
      appendProperty(builder, "extraImportPaths", this.extraImportPaths);
      appendProperty(builder, "extraImportDeps", this.extraImportDeps);
      appendProperty(builder, "extraArgs", this.extraArgs);
      appendProperty(builder, "env", this.env);
      builder.append("\n}");
      return builder.toString();
    }
  }

  public static final class SourceSetOverride {
    public final Boolean enabled;

    public final List<@NonNull String> sourceDirs;

    public final List<@NonNull String> includeGlobs;

    public final List<@NonNull String> excludeGlobs;

    public final List<@NonNull String> extraImportPaths;

    public final List<@NonNull String> extraImportDeps;

    public final List<@NonNull String> extraArgs;

    public final Map<@NonNull String, @NonNull String> env;

    public SourceSetOverride(@Named("enabled") Boolean enabled,
        @Named("sourceDirs") List<@NonNull String> sourceDirs,
        @Named("includeGlobs") List<@NonNull String> includeGlobs,
        @Named("excludeGlobs") List<@NonNull String> excludeGlobs,
        @Named("extraImportPaths") List<@NonNull String> extraImportPaths,
        @Named("extraImportDeps") List<@NonNull String> extraImportDeps,
        @Named("extraArgs") List<@NonNull String> extraArgs,
        @Named("env") Map<@NonNull String, @NonNull String> env) {
      this.enabled = enabled;
      this.sourceDirs = sourceDirs;
      this.includeGlobs = includeGlobs;
      this.excludeGlobs = excludeGlobs;
      this.extraImportPaths = extraImportPaths;
      this.extraImportDeps = extraImportDeps;
      this.extraArgs = extraArgs;
      this.env = env;
    }

    public SourceSetOverride withEnabled(Boolean enabled) {
      return new SourceSetOverride(enabled, sourceDirs, includeGlobs, excludeGlobs, extraImportPaths, extraImportDeps, extraArgs, env);
    }

    public SourceSetOverride withSourceDirs(List<@NonNull String> sourceDirs) {
      return new SourceSetOverride(enabled, sourceDirs, includeGlobs, excludeGlobs, extraImportPaths, extraImportDeps, extraArgs, env);
    }

    public SourceSetOverride withIncludeGlobs(List<@NonNull String> includeGlobs) {
      return new SourceSetOverride(enabled, sourceDirs, includeGlobs, excludeGlobs, extraImportPaths, extraImportDeps, extraArgs, env);
    }

    public SourceSetOverride withExcludeGlobs(List<@NonNull String> excludeGlobs) {
      return new SourceSetOverride(enabled, sourceDirs, includeGlobs, excludeGlobs, extraImportPaths, extraImportDeps, extraArgs, env);
    }

    public SourceSetOverride withExtraImportPaths(List<@NonNull String> extraImportPaths) {
      return new SourceSetOverride(enabled, sourceDirs, includeGlobs, excludeGlobs, extraImportPaths, extraImportDeps, extraArgs, env);
    }

    public SourceSetOverride withExtraImportDeps(List<@NonNull String> extraImportDeps) {
      return new SourceSetOverride(enabled, sourceDirs, includeGlobs, excludeGlobs, extraImportPaths, extraImportDeps, extraArgs, env);
    }

    public SourceSetOverride withExtraArgs(List<@NonNull String> extraArgs) {
      return new SourceSetOverride(enabled, sourceDirs, includeGlobs, excludeGlobs, extraImportPaths, extraImportDeps, extraArgs, env);
    }

    public SourceSetOverride withEnv(Map<@NonNull String, @NonNull String> env) {
      return new SourceSetOverride(enabled, sourceDirs, includeGlobs, excludeGlobs, extraImportPaths, extraImportDeps, extraArgs, env);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (this.getClass() != obj.getClass()) return false;
      SourceSetOverride other = (SourceSetOverride) obj;
      if (!Objects.equals(this.enabled, other.enabled)) return false;
      if (!Objects.equals(this.sourceDirs, other.sourceDirs)) return false;
      if (!Objects.equals(this.includeGlobs, other.includeGlobs)) return false;
      if (!Objects.equals(this.excludeGlobs, other.excludeGlobs)) return false;
      if (!Objects.equals(this.extraImportPaths, other.extraImportPaths)) return false;
      if (!Objects.equals(this.extraImportDeps, other.extraImportDeps)) return false;
      if (!Objects.equals(this.extraArgs, other.extraArgs)) return false;
      if (!Objects.equals(this.env, other.env)) return false;
      return true;
    }

    @Override
    public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.enabled);
      result = 31 * result + Objects.hashCode(this.sourceDirs);
      result = 31 * result + Objects.hashCode(this.includeGlobs);
      result = 31 * result + Objects.hashCode(this.excludeGlobs);
      result = 31 * result + Objects.hashCode(this.extraImportPaths);
      result = 31 * result + Objects.hashCode(this.extraImportDeps);
      result = 31 * result + Objects.hashCode(this.extraArgs);
      result = 31 * result + Objects.hashCode(this.env);
      return result;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder(450);
      builder.append(SourceSetOverride.class.getSimpleName()).append(" {");
      appendProperty(builder, "enabled", this.enabled);
      appendProperty(builder, "sourceDirs", this.sourceDirs);
      appendProperty(builder, "includeGlobs", this.includeGlobs);
      appendProperty(builder, "excludeGlobs", this.excludeGlobs);
      appendProperty(builder, "extraImportPaths", this.extraImportPaths);
      appendProperty(builder, "extraImportDeps", this.extraImportDeps);
      appendProperty(builder, "extraArgs", this.extraArgs);
      appendProperty(builder, "env", this.env);
      builder.append("\n}");
      return builder.toString();
    }
  }

  public static final class ResolverConfig {
    public final @NonNull ResolverKind kind;

    public final String command;

    public final String path;

    public final String dependency;

    public final String mainClass;

    public final @NonNull List<@NonNull String> jvmArgs;

    public final String executableSubpath;

    public final String url;

    public final String digest;

    public final String sanctionedExecutablePath;

    public final boolean skipOnUnsupportedPlatform;

    public ResolverConfig(@Named("kind") @NonNull ResolverKind kind,
        @Named("command") String command, @Named("path") String path,
        @Named("dependency") String dependency, @Named("mainClass") String mainClass,
        @Named("jvmArgs") @NonNull List<@NonNull String> jvmArgs,
        @Named("executableSubpath") String executableSubpath, @Named("url") String url,
        @Named("digest") String digest,
        @Named("sanctionedExecutablePath") String sanctionedExecutablePath,
        @Named("skipOnUnsupportedPlatform") boolean skipOnUnsupportedPlatform) {
      this.kind = kind;
      this.command = command;
      this.path = path;
      this.dependency = dependency;
      this.mainClass = mainClass;
      this.jvmArgs = jvmArgs;
      this.executableSubpath = executableSubpath;
      this.url = url;
      this.digest = digest;
      this.sanctionedExecutablePath = sanctionedExecutablePath;
      this.skipOnUnsupportedPlatform = skipOnUnsupportedPlatform;
    }

    public ResolverConfig withKind(@NonNull ResolverKind kind) {
      return new ResolverConfig(kind, command, path, dependency, mainClass, jvmArgs, executableSubpath, url, digest, sanctionedExecutablePath, skipOnUnsupportedPlatform);
    }

    public ResolverConfig withCommand(String command) {
      return new ResolverConfig(kind, command, path, dependency, mainClass, jvmArgs, executableSubpath, url, digest, sanctionedExecutablePath, skipOnUnsupportedPlatform);
    }

    public ResolverConfig withPath(String path) {
      return new ResolverConfig(kind, command, path, dependency, mainClass, jvmArgs, executableSubpath, url, digest, sanctionedExecutablePath, skipOnUnsupportedPlatform);
    }

    public ResolverConfig withDependency(String dependency) {
      return new ResolverConfig(kind, command, path, dependency, mainClass, jvmArgs, executableSubpath, url, digest, sanctionedExecutablePath, skipOnUnsupportedPlatform);
    }

    public ResolverConfig withMainClass(String mainClass) {
      return new ResolverConfig(kind, command, path, dependency, mainClass, jvmArgs, executableSubpath, url, digest, sanctionedExecutablePath, skipOnUnsupportedPlatform);
    }

    public ResolverConfig withJvmArgs(@NonNull List<@NonNull String> jvmArgs) {
      return new ResolverConfig(kind, command, path, dependency, mainClass, jvmArgs, executableSubpath, url, digest, sanctionedExecutablePath, skipOnUnsupportedPlatform);
    }

    public ResolverConfig withExecutableSubpath(String executableSubpath) {
      return new ResolverConfig(kind, command, path, dependency, mainClass, jvmArgs, executableSubpath, url, digest, sanctionedExecutablePath, skipOnUnsupportedPlatform);
    }

    public ResolverConfig withUrl(String url) {
      return new ResolverConfig(kind, command, path, dependency, mainClass, jvmArgs, executableSubpath, url, digest, sanctionedExecutablePath, skipOnUnsupportedPlatform);
    }

    public ResolverConfig withDigest(String digest) {
      return new ResolverConfig(kind, command, path, dependency, mainClass, jvmArgs, executableSubpath, url, digest, sanctionedExecutablePath, skipOnUnsupportedPlatform);
    }

    public ResolverConfig withSanctionedExecutablePath(String sanctionedExecutablePath) {
      return new ResolverConfig(kind, command, path, dependency, mainClass, jvmArgs, executableSubpath, url, digest, sanctionedExecutablePath, skipOnUnsupportedPlatform);
    }

    public ResolverConfig withSkipOnUnsupportedPlatform(boolean skipOnUnsupportedPlatform) {
      return new ResolverConfig(kind, command, path, dependency, mainClass, jvmArgs, executableSubpath, url, digest, sanctionedExecutablePath, skipOnUnsupportedPlatform);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (this.getClass() != obj.getClass()) return false;
      ResolverConfig other = (ResolverConfig) obj;
      if (!Objects.equals(this.kind, other.kind)) return false;
      if (!Objects.equals(this.command, other.command)) return false;
      if (!Objects.equals(this.path, other.path)) return false;
      if (!Objects.equals(this.dependency, other.dependency)) return false;
      if (!Objects.equals(this.mainClass, other.mainClass)) return false;
      if (!Objects.equals(this.jvmArgs, other.jvmArgs)) return false;
      if (!Objects.equals(this.executableSubpath, other.executableSubpath)) return false;
      if (!Objects.equals(this.url, other.url)) return false;
      if (!Objects.equals(this.digest, other.digest)) return false;
      if (!Objects.equals(this.sanctionedExecutablePath, other.sanctionedExecutablePath)) return false;
      if (!Objects.equals(this.skipOnUnsupportedPlatform, other.skipOnUnsupportedPlatform)) return false;
      return true;
    }

    @Override
    public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.kind);
      result = 31 * result + Objects.hashCode(this.command);
      result = 31 * result + Objects.hashCode(this.path);
      result = 31 * result + Objects.hashCode(this.dependency);
      result = 31 * result + Objects.hashCode(this.mainClass);
      result = 31 * result + Objects.hashCode(this.jvmArgs);
      result = 31 * result + Objects.hashCode(this.executableSubpath);
      result = 31 * result + Objects.hashCode(this.url);
      result = 31 * result + Objects.hashCode(this.digest);
      result = 31 * result + Objects.hashCode(this.sanctionedExecutablePath);
      result = 31 * result + Objects.hashCode(this.skipOnUnsupportedPlatform);
      return result;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder(600);
      builder.append(ResolverConfig.class.getSimpleName()).append(" {");
      appendProperty(builder, "kind", this.kind);
      appendProperty(builder, "command", this.command);
      appendProperty(builder, "path", this.path);
      appendProperty(builder, "dependency", this.dependency);
      appendProperty(builder, "mainClass", this.mainClass);
      appendProperty(builder, "jvmArgs", this.jvmArgs);
      appendProperty(builder, "executableSubpath", this.executableSubpath);
      appendProperty(builder, "url", this.url);
      appendProperty(builder, "digest", this.digest);
      appendProperty(builder, "sanctionedExecutablePath", this.sanctionedExecutablePath);
      appendProperty(builder, "skipOnUnsupportedPlatform", this.skipOnUnsupportedPlatform);
      builder.append("\n}");
      return builder.toString();
    }
  }

  public static final class BuiltinLanguageTarget {
    public final @NonNull String name;

    public final String options;

    public final String outputDir;

    public BuiltinLanguageTarget(@Named("name") @NonNull String name,
        @Named("options") String options, @Named("outputDir") String outputDir) {
      this.name = name;
      this.options = options;
      this.outputDir = outputDir;
    }

    public BuiltinLanguageTarget withName(@NonNull String name) {
      return new BuiltinLanguageTarget(name, options, outputDir);
    }

    public BuiltinLanguageTarget withOptions(String options) {
      return new BuiltinLanguageTarget(name, options, outputDir);
    }

    public BuiltinLanguageTarget withOutputDir(String outputDir) {
      return new BuiltinLanguageTarget(name, options, outputDir);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (this.getClass() != obj.getClass()) return false;
      BuiltinLanguageTarget other = (BuiltinLanguageTarget) obj;
      if (!Objects.equals(this.name, other.name)) return false;
      if (!Objects.equals(this.options, other.options)) return false;
      if (!Objects.equals(this.outputDir, other.outputDir)) return false;
      return true;
    }

    @Override
    public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.name);
      result = 31 * result + Objects.hashCode(this.options);
      result = 31 * result + Objects.hashCode(this.outputDir);
      return result;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder(200);
      builder.append(BuiltinLanguageTarget.class.getSimpleName()).append(" {");
      appendProperty(builder, "name", this.name);
      appendProperty(builder, "options", this.options);
      appendProperty(builder, "outputDir", this.outputDir);
      builder.append("\n}");
      return builder.toString();
    }
  }

  public static final class PluginTarget {
    public final @NonNull String name;

    public final String options;

    public final String outputDir;

    public final @NonNull ResolverConfig resolver;

    public PluginTarget(@Named("name") @NonNull String name, @Named("options") String options,
        @Named("outputDir") String outputDir, @Named("resolver") @NonNull ResolverConfig resolver) {
      this.name = name;
      this.options = options;
      this.outputDir = outputDir;
      this.resolver = resolver;
    }

    public PluginTarget withName(@NonNull String name) {
      return new PluginTarget(name, options, outputDir, resolver);
    }

    public PluginTarget withOptions(String options) {
      return new PluginTarget(name, options, outputDir, resolver);
    }

    public PluginTarget withOutputDir(String outputDir) {
      return new PluginTarget(name, options, outputDir, resolver);
    }

    public PluginTarget withResolver(@NonNull ResolverConfig resolver) {
      return new PluginTarget(name, options, outputDir, resolver);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (this.getClass() != obj.getClass()) return false;
      PluginTarget other = (PluginTarget) obj;
      if (!Objects.equals(this.name, other.name)) return false;
      if (!Objects.equals(this.options, other.options)) return false;
      if (!Objects.equals(this.outputDir, other.outputDir)) return false;
      if (!Objects.equals(this.resolver, other.resolver)) return false;
      return true;
    }

    @Override
    public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.name);
      result = 31 * result + Objects.hashCode(this.options);
      result = 31 * result + Objects.hashCode(this.outputDir);
      result = 31 * result + Objects.hashCode(this.resolver);
      return result;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder(250);
      builder.append(PluginTarget.class.getSimpleName()).append(" {");
      appendProperty(builder, "name", this.name);
      appendProperty(builder, "options", this.options);
      appendProperty(builder, "outputDir", this.outputDir);
      appendProperty(builder, "resolver", this.resolver);
      builder.append("\n}");
      return builder.toString();
    }
  }

  public static final class ImportConfig {
    public final boolean includeProjectDependencies;

    public final @NonNull List<@NonNull String> dependencyArtifacts;

    public final @NonNull List<@NonNull String> paths;

    public ImportConfig(@Named("includeProjectDependencies") boolean includeProjectDependencies,
        @Named("dependencyArtifacts") @NonNull List<@NonNull String> dependencyArtifacts,
        @Named("paths") @NonNull List<@NonNull String> paths) {
      this.includeProjectDependencies = includeProjectDependencies;
      this.dependencyArtifacts = dependencyArtifacts;
      this.paths = paths;
    }

    public ImportConfig withIncludeProjectDependencies(boolean includeProjectDependencies) {
      return new ImportConfig(includeProjectDependencies, dependencyArtifacts, paths);
    }

    public ImportConfig withDependencyArtifacts(
        @NonNull List<@NonNull String> dependencyArtifacts) {
      return new ImportConfig(includeProjectDependencies, dependencyArtifacts, paths);
    }

    public ImportConfig withPaths(@NonNull List<@NonNull String> paths) {
      return new ImportConfig(includeProjectDependencies, dependencyArtifacts, paths);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (this.getClass() != obj.getClass()) return false;
      ImportConfig other = (ImportConfig) obj;
      if (!Objects.equals(this.includeProjectDependencies, other.includeProjectDependencies)) return false;
      if (!Objects.equals(this.dependencyArtifacts, other.dependencyArtifacts)) return false;
      if (!Objects.equals(this.paths, other.paths)) return false;
      return true;
    }

    @Override
    public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.includeProjectDependencies);
      result = 31 * result + Objects.hashCode(this.dependencyArtifacts);
      result = 31 * result + Objects.hashCode(this.paths);
      return result;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder(200);
      builder.append(ImportConfig.class.getSimpleName()).append(" {");
      appendProperty(builder, "includeProjectDependencies", this.includeProjectDependencies);
      appendProperty(builder, "dependencyArtifacts", this.dependencyArtifacts);
      appendProperty(builder, "paths", this.paths);
      builder.append("\n}");
      return builder.toString();
    }
  }

  public static final class DescriptorConfig {
    public final @NonNull String fileName;

    public final boolean includeImports;

    public final boolean includeSourceInfo;

    public DescriptorConfig(@Named("fileName") @NonNull String fileName,
        @Named("includeImports") boolean includeImports,
        @Named("includeSourceInfo") boolean includeSourceInfo) {
      this.fileName = fileName;
      this.includeImports = includeImports;
      this.includeSourceInfo = includeSourceInfo;
    }

    public DescriptorConfig withFileName(@NonNull String fileName) {
      return new DescriptorConfig(fileName, includeImports, includeSourceInfo);
    }

    public DescriptorConfig withIncludeImports(boolean includeImports) {
      return new DescriptorConfig(fileName, includeImports, includeSourceInfo);
    }

    public DescriptorConfig withIncludeSourceInfo(boolean includeSourceInfo) {
      return new DescriptorConfig(fileName, includeImports, includeSourceInfo);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (this.getClass() != obj.getClass()) return false;
      DescriptorConfig other = (DescriptorConfig) obj;
      if (!Objects.equals(this.fileName, other.fileName)) return false;
      if (!Objects.equals(this.includeImports, other.includeImports)) return false;
      if (!Objects.equals(this.includeSourceInfo, other.includeSourceInfo)) return false;
      return true;
    }

    @Override
    public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.fileName);
      result = 31 * result + Objects.hashCode(this.includeImports);
      result = 31 * result + Objects.hashCode(this.includeSourceInfo);
      return result;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder(200);
      builder.append(DescriptorConfig.class.getSimpleName()).append(" {");
      appendProperty(builder, "fileName", this.fileName);
      appendProperty(builder, "includeImports", this.includeImports);
      appendProperty(builder, "includeSourceInfo", this.includeSourceInfo);
      builder.append("\n}");
      return builder.toString();
    }
  }

  public enum ResolverKind {
    SYSTEM_PATH("system-path"),

    PATH("path"),

    BINARY_MAVEN("binary-maven"),

    JVM_MAVEN("jvm-maven");

    private String value;

    private ResolverKind(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return this.value;
    }
  }
}
