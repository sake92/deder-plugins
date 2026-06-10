package ba.sake.deder.plugins;

import ba.sake.deder.config.DederPlugins;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.util.List;
import java.util.Objects;
import org.pkl.config.java.mapper.Named;
import org.pkl.config.java.mapper.NonNull;

public final class WebDashboard {
  public final @NonNull WebDashboardPluginConfig config;

  public WebDashboard(@Named("config") @NonNull WebDashboardPluginConfig config) {
    this.config = config;
  }

  public WebDashboard withConfig(@NonNull WebDashboardPluginConfig config) {
    return new WebDashboard(config);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (this.getClass() != obj.getClass()) return false;
    WebDashboard other = (WebDashboard) obj;
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
    builder.append(WebDashboard.class.getSimpleName()).append(" {");
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

  public static final class WebDashboardPlugin extends DederPlugins.DederPlugin {
    public final @NonNull String version;

    public final @NonNull WebDashboardPluginConfig config;

    public WebDashboardPlugin(@Named("id") @NonNull String id,
        @Named("deps") @NonNull List<@NonNull String> deps,
        @Named("version") @NonNull String version,
        @Named("config") @NonNull WebDashboardPluginConfig config) {
      super(id, deps);
      this.version = version;
      this.config = config;
    }

    public WebDashboardPlugin withId(@NonNull String id) {
      return new WebDashboardPlugin(id, deps, version, config);
    }

    public WebDashboardPlugin withDeps(@NonNull List<@NonNull String> deps) {
      return new WebDashboardPlugin(id, deps, version, config);
    }

    public WebDashboardPlugin withVersion(@NonNull String version) {
      return new WebDashboardPlugin(id, deps, version, config);
    }

    public WebDashboardPlugin withConfig(@NonNull WebDashboardPluginConfig config) {
      return new WebDashboardPlugin(id, deps, version, config);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (this.getClass() != obj.getClass()) return false;
      WebDashboardPlugin other = (WebDashboardPlugin) obj;
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
      builder.append(WebDashboardPlugin.class.getSimpleName()).append(" {");
      appendProperty(builder, "id", this.id);
      appendProperty(builder, "deps", this.deps);
      appendProperty(builder, "version", this.version);
      appendProperty(builder, "config", this.config);
      builder.append("\n}");
      return builder.toString();
    }
  }

  public static final class WebDashboardPluginConfig {
    public final boolean enabled;

    public final @NonNull String host;

    public final long port;

    public final long statsRefreshIntervalMs;

    public WebDashboardPluginConfig(@Named("enabled") boolean enabled,
        @Named("host") @NonNull String host, @Named("port") long port,
        @Named("statsRefreshIntervalMs") long statsRefreshIntervalMs) {
      this.enabled = enabled;
      this.host = host;
      this.port = port;
      this.statsRefreshIntervalMs = statsRefreshIntervalMs;
    }

    public WebDashboardPluginConfig withEnabled(boolean enabled) {
      return new WebDashboardPluginConfig(enabled, host, port, statsRefreshIntervalMs);
    }

    public WebDashboardPluginConfig withHost(@NonNull String host) {
      return new WebDashboardPluginConfig(enabled, host, port, statsRefreshIntervalMs);
    }

    public WebDashboardPluginConfig withPort(long port) {
      return new WebDashboardPluginConfig(enabled, host, port, statsRefreshIntervalMs);
    }

    public WebDashboardPluginConfig withStatsRefreshIntervalMs(long statsRefreshIntervalMs) {
      return new WebDashboardPluginConfig(enabled, host, port, statsRefreshIntervalMs);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (this.getClass() != obj.getClass()) return false;
      WebDashboardPluginConfig other = (WebDashboardPluginConfig) obj;
      if (!Objects.equals(this.enabled, other.enabled)) return false;
      if (!Objects.equals(this.host, other.host)) return false;
      if (!Objects.equals(this.port, other.port)) return false;
      if (!Objects.equals(this.statsRefreshIntervalMs, other.statsRefreshIntervalMs)) return false;
      return true;
    }

    @Override
    public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.enabled);
      result = 31 * result + Objects.hashCode(this.host);
      result = 31 * result + Objects.hashCode(this.port);
      result = 31 * result + Objects.hashCode(this.statsRefreshIntervalMs);
      return result;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder(250);
      builder.append(WebDashboardPluginConfig.class.getSimpleName()).append(" {");
      appendProperty(builder, "enabled", this.enabled);
      appendProperty(builder, "host", this.host);
      appendProperty(builder, "port", this.port);
      appendProperty(builder, "statsRefreshIntervalMs", this.statsRefreshIntervalMs);
      builder.append("\n}");
      return builder.toString();
    }
  }
}
