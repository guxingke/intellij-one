package com.guxingke.intellij.plugin.postfix.template.conf;

import java.util.List;
import org.jetbrains.annotations.NotNull;

public class TemplateItem {

  @NotNull
  private String type;
  @NotNull
  private String clazz;
  private List<TemplateItemRequire> requires;
  @NotNull
  private String template;

  public @NotNull String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public @NotNull String getClazz() {
    return clazz;
  }

  public void setClazz(String clazz) {
    this.clazz = clazz;
  }

  public List<TemplateItemRequire> getRequires() {
    return requires;
  }

  public void setRequires(List<TemplateItemRequire> requires) {
    this.requires = requires;
  }

  public @NotNull String getTemplate() {
    return template;
  }

  public void setTemplate(@NotNull String template) {
    this.template = template;
  }
}
