package com.guxingke.intellij.plugin.postfix.template.conf;

public class TemplateItemRequire {

  private String type;
  private String name;
  private String clazz;

  public boolean isClass() {
    return "class".equals(type);
  }

  public boolean isMethod() {
    return "method".equals(type);
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getClazz() {
    return clazz;
  }

  public void setClazz(String clazz) {
    this.clazz = clazz;
  }
}
