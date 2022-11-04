package com.guxingke.intellij.plugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.yaml.snakeyaml.Yaml;

public class Configs {

  private static Path confDir;
  private static OneConfig config;

  static {
    confDir = Paths.get(System.getenv("HOME"), ".config", "the-one-toolbox");
    var envDir = System.getenv("INTELLIJ_THE_ONE_TOOLBOX_CONFIG_DIR");
    if (envDir != null) {
      confDir = Paths.get(envDir);
    }

    config = loadConfig();
  }

  public static Path getConfDir() {
    return confDir;
  }


  public static OneConfig getConfig() {
    return config;
  }

  public static String env(
      String key,
      String defaultValue
  ) {
    var v = System.getenv(key);
    if (v == null) {
      return defaultValue;
    }
    return v;
  }

  /**
   * 默认配置目录，当前用户目录下 `.config/the-one-toolbox`。 可通过环境变量覆盖 INTELLIJ_THE_ONE_TOOLBOX_CONFIG_DIR
   *
   * 文件 config.yml
   */
  public static OneConfig loadConfig() {
    var dir = getConfDir();
    if (!dir.toFile().exists()) {
      // not found config dir
      return OneConfig.defaultCfg();
    }
    var cfp = dir.resolve("config.yml");
    if (!cfp.toFile().exists() || !cfp.toFile().isFile()) {
      return OneConfig.defaultCfg();
    }

    var yaml = new Yaml();
    try {
      return yaml.loadAs(Files.newInputStream(cfp), OneConfig.class);
    } catch (IOException e) {
      e.printStackTrace();
      return OneConfig.defaultCfg();
    }
  }

  /*
  postfix:
    enable: true # 默认为 true, 如果禁用，则插件的 postfix 相关功能失效。
    # 内部由代码提供的 postfix template
    internal:
      enable: true # 默认为 true, 如果禁用，则内部提供的 postfix template 失效, 如 map 。
      # 在 enable 的情况下，部分禁用。
      blocklist:
        - log
    # 内置基于配置文件的 postfix template
    builtin:
      enable: true
      blocklist:
        - log
    external:
      enable: true
      blocklist:
        - className
   */
  public static class OneConfig {

    private boolean debug = false;
    private PostfixConfig postfix = new PostfixConfig();
    private LineMarkerConfig linemarker = new LineMarkerConfig();

    public PostfixConfig getPostfix() {
      return postfix;
    }

    public void setPostfix(PostfixConfig postfix) {
      this.postfix = postfix;
    }

    public static OneConfig defaultCfg() {
      return new OneConfig();
    }

    public boolean isDebug() {
      return debug;
    }

    public void setDebug(boolean debug) {
      this.debug = debug;
    }

    public LineMarkerConfig getLinemarker() {
      return linemarker;
    }

    public void setLinemarker(LineMarkerConfig linemarker) {
      this.linemarker = linemarker;
    }
  }

  public static class LineMarkerConfig {

    // defualt to false
    private boolean enable = false;
    private String handler = "com.company.Handler";
    private String dispatcher = "com.company.Dispatcher";

    public boolean isEnable() {
      return enable;
    }

    public void setEnable(boolean enable) {
      this.enable = enable;
    }

    public String getHandler() {
      return handler;
    }

    public void setHandler(String handler) {
      this.handler = handler;
    }

    public String getDispatcher() {
      return dispatcher;
    }

    public void setDispatcher(String dispatcher) {
      this.dispatcher = dispatcher;
    }
  }

  public static class PostfixConfig {

    private boolean enable = true;
    private PostfixNamespaceConfig internal = new PostfixNamespaceConfig();
    private PostfixNamespaceConfig builtin = new PostfixNamespaceConfig();
    private PostfixNamespaceConfig external = new PostfixNamespaceConfig();

    public boolean isEnable() {
      return enable;
    }

    public void setEnable(boolean enable) {
      this.enable = enable;
    }

    public PostfixNamespaceConfig getInternal() {
      return internal;
    }

    public void setInternal(PostfixNamespaceConfig internal) {
      this.internal = internal;
    }

    public PostfixNamespaceConfig getBuiltin() {
      return builtin;
    }

    public void setBuiltin(PostfixNamespaceConfig builtin) {
      this.builtin = builtin;
    }

    public PostfixNamespaceConfig getExternal() {
      return external;
    }

    public void setExternal(PostfixNamespaceConfig external) {
      this.external = external;
    }
  }

  public static class PostfixNamespaceConfig {

    private boolean enable = true;

    private List<String> blocklist = new ArrayList<>();

    public boolean isEnable() {
      return enable;
    }

    public void setEnable(boolean enable) {
      this.enable = enable;
    }

    public List<String> getBlocklist() {
      return blocklist;
    }

    public void setBlocklist(List<String> blocklist) {
      this.blocklist = blocklist;
    }
  }

}
