package com.guxingke.intellij.plugin.postfix.template.conf;

import junit.framework.TestCase;
import org.junit.Assert;

public class TemplateConfigTest extends TestCase {

  public void testLoad() {
    var cfg = TemplateConfig.load("./docs/example.yml");
    Assert.assertNotNull(cfg);
  }
}