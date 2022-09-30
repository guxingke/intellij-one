package com.guxingke.intellij.plugin.postfix.template.conf;

import java.util.List;
import org.jetbrains.annotations.NotNull;

public class TemplateDefinition {
  @NotNull
  private String name;
  @NotNull
  private String description;
  @NotNull
  private List<TemplateItem> templates;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<TemplateItem> getTemplates() {
    return templates;
  }

  public void setTemplates(List<TemplateItem> templates) {
    this.templates = templates;
  }
}
