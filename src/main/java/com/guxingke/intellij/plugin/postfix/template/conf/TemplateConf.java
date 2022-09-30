package com.guxingke.intellij.plugin.postfix.template.conf;

import java.util.List;

public class TemplateConf {

  private String namespace;
  private List<TemplateDefinition> definitions;

  public String getNamespace() {
    return namespace;
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  public List<TemplateDefinition> getDefinitions() {
    return definitions;
  }

  public void setDefinitions(List<TemplateDefinition> definitions) {
    this.definitions = definitions;
  }
}
