package com.guxingke.intellij.plugin.action;

/**
 * @author gxk
 * @since 2022/11/15
 */
public class SnakeCase2CamelCaseAction extends SelectionReplacerAction {

  @Override
  protected String replace(String input) {
    var ia = input.split("_");

    var sb = new StringBuilder();
    sb.append(ia[0]);
    for (int i = 1; i < ia.length; i++) {
      var v = ia[i];
      sb.append(capitalize(v));
    }
    return sb.toString();
  }

  private String capitalize(String obj) {
    return obj.substring(0, 1).toUpperCase().concat(obj.substring(1));
  }

  @Override
  protected boolean match(String input) {
    return !input.contains(" ") && input.contains("_") && !input.startsWith("_") && !input.endsWith("_");
  }

}
