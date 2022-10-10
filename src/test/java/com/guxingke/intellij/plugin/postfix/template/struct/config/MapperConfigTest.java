package com.guxingke.intellij.plugin.postfix.template.struct.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.Assert;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

public class MapperConfigTest {

  @Test
  public void test() throws IOException {
    var yaml = new Yaml();
    var cfg = yaml.loadAs(Files.newInputStream(Paths.get("docs", "mapper.yml")), MapperConfig.class);

    Assert.assertNotNull(cfg);
  }

}