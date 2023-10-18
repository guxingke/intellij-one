package com.guxingke.intellij.plugin.postfix.template.struct.config;

import java.util.ArrayList;
import java.util.List;

public class MapperConfig {

  private String namespace;

  private List<MapperDefinition> definitions;

  public static MapperConfig defaultConfig() {
    var cfg = new MapperConfig();
    cfg.setNamespace("default");
    cfg.setDefinitions(new ArrayList<>());
    return cfg;
  }

  public String getNamespace() {
    return namespace;
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  public List<MapperDefinition> getDefinitions() {
    return definitions;
  }

  public void setDefinitions(List<MapperDefinition> definitions) {
    this.definitions = definitions;
  }

  public static class MapperDefinition {

    private String name;
    private boolean enable;

    private String inputType;
    private String intputName;

    private String outputType;
    private String outputName;

    private List<MapperRequire> requires;

    private String template;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public boolean isEnable() {
      return enable;
    }

    public void setEnable(boolean enable) {
      this.enable = enable;
    }

    public String getInputType() {
      return inputType;
    }

    public void setInputType(String inputType) {
      this.inputType = inputType;
    }

    public String getIntputName() {
      return intputName;
    }

    public void setIntputName(String intputName) {
      this.intputName = intputName;
    }

    public String getOutputType() {
      return outputType;
    }

    public void setOutputType(String outputType) {
      this.outputType = outputType;
    }

    public String getOutputName() {
      return outputName;
    }

    public void setOutputName(String outputName) {
      this.outputName = outputName;
    }

    public List<MapperRequire> getRequires() {
      return requires;
    }

    public void setRequires(List<MapperRequire> requires) {
      this.requires = requires;
    }

    public String getTemplate() {
      return template;
    }

    public void setTemplate(String template) {
      this.template = template;
    }

    @Override
    public String toString() {
      return "MapperDefinition{" +
          "name='" + name + '\'' +
          ", enable=" + enable +
          ", inputType='" + inputType + '\'' +
          ", intputName='" + intputName + '\'' +
          ", outputType='" + outputType + '\'' +
          ", outputName='" + outputName + '\'' +
          ", requires=" + requires +
          ", template='" + template + '\'' +
          '}';
    }
  }

  public static class MapperRequire {

    private String type;
    private String clazz;
    private String name;

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }

    public String getClazz() {
      return clazz;
    }

    public void setClazz(String clazz) {
      this.clazz = clazz;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }
}
