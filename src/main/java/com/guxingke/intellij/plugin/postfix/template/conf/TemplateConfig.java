package com.guxingke.intellij.plugin.postfix.template.conf;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.yaml.snakeyaml.Yaml;

public abstract class TemplateConfig {

  public static TemplateConf load(String path) {
    var yaml = new Yaml();
    try {
      return yaml.loadAs(Files.readString(Paths.get(path)), TemplateConf.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static TemplateConf load(InputStream is) {
    var yaml = new Yaml();
    return yaml.loadAs(is, TemplateConf.class);
  }
}
