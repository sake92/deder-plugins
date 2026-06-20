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

    public final long tasksMaxConcurrent;

    public final long tasksMaxHistory;

    public final long tasksOutputLineLimit;

    public WebDashboardPluginConfig(@Named("enabled") boolean enabled,
        @Named("host") @NonNull String host, @Named("port") long port,
        @Named("statsRefreshIntervalMs") long statsRefreshIntervalMs,
        @Named("tasksMaxConcurrent") long tasksMaxConcurrent,
        @Named("tasksMaxHistory") long tasksMaxHistory,
        @Named("tasksOutputLineLimit") long tasksOutputLineLimit) {
      this.enabled = enabled;
      this.host = host;
      this.port = port;
      this.statsRefreshIntervalMs = statsRefreshIntervalMs;
      this.tasksMaxConcurrent = tasksMaxConcurrent;
      this.tasksMaxHistory = tasksMaxHistory;
      this.tasksOutputLineLimit = tasksOutputLineLimit;
    }

    public WebDashboardPluginConfig withEnabled(boolean enabled) {
      return new WebDashboardPluginConfig(enabled, host, port, statsRefreshIntervalMs, tasksMaxConcurrent, tasksMaxHistory, tasksOutputLineLimit);
    }

    public WebDashboardPluginConfig withHost(@NonNull String host) {
      return new WebDashboardPluginConfig(enabled, host, port, statsRefreshIntervalMs, tasksMaxConcurrent, tasksMaxHistory, tasksOutputLineLimit);
    }

    public WebDashboardPluginConfig withPort(long port) {
      return new WebDashboardPluginConfig(enabled, host, port, statsRefreshIntervalMs, tasksMaxConcurrent, tasksMaxHistory, tasksOutputLineLimit);
    }

    public WebDashboardPluginConfig withStatsRefreshIntervalMs(long statsRefreshIntervalMs) {
      return new WebDashboardPluginConfig(enabled, host, port, statsRefreshIntervalMs, tasksMaxConcurrent, tasksMaxHistory, tasksOutputLineLimit);
    }

    public WebDashboardPluginConfig withTasksMaxConcurrent(long tasksMaxConcurrent) {
      return new WebDashboardPluginConfig(enabled, host, port, statsRefreshIntervalMs, tasksMaxConcurrent, tasksMaxHistory, tasksOutputLineLimit);
    }

    public WebDashboardPluginConfig withTasksMaxHistory(long tasksMaxHistory) {
      return new WebDashboardPluginConfig(enabled, host, port, statsRefreshIntervalMs, tasksMaxConcurrent, tasksMaxHistory, tasksOutputLineLimit);
    }

    public WebDashboardPluginConfig withTasksOutputLineLimit(long tasksOutputLineLimit) {
      return new WebDashboardPluginConfig(enabled, host, port, statsRefreshIntervalMs, tasksMaxConcurrent, tasksMaxHistory, tasksOutputLineLimit);
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
      if (!Objects.equals(this.tasksMaxConcurrent, other.tasksMaxConcurrent)) return false;
      if (!Objects.equals(this.tasksMaxHistory, other.tasksMaxHistory)) return false;
      if (!Objects.equals(this.tasksOutputLineLimit, other.tasksOutputLineLimit)) return false;
      return true;
    }

    @Override
    public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.enabled);
      result = 31 * result + Objects.hashCode(this.host);
      result = 31 * result + Objects.hashCode(this.port);
      result = 31 * result + Objects.hashCode(this.statsRefreshIntervalMs);
      result = 31 * result + Objects.hashCode(this.tasksMaxConcurrent);
      result = 31 * result + Objects.hashCode(this.tasksMaxHistory);
      result = 31 * result + Objects.hashCode(this.tasksOutputLineLimit);
      return result;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder(400);
      builder.append(WebDashboardPluginConfig.class.getSimpleName()).append(" {");
      appendProperty(builder, "enabled", this.enabled);
      appendProperty(builder, "host", this.host);
      appendProperty(builder, "port", this.port);
      appendProperty(builder, "statsRefreshIntervalMs", this.statsRefreshIntervalMs);
      appendProperty(builder, "tasksMaxConcurrent", this.tasksMaxConcurrent);
      appendProperty(builder, "tasksMaxHistory", this.tasksMaxHistory);
      appendProperty(builder, "tasksOutputLineLimit", this.tasksOutputLineLimit);
      builder.append("\n}");
      return builder.toString();
    }
  }
}
