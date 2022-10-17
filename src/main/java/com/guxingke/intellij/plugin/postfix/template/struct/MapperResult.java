package com.guxingke.intellij.plugin.postfix.template.struct;

import com.intellij.codeInsight.template.impl.Variable;
import java.util.List;

public class MapperResult {

  private String ts;
  private List<Variable> vars;

  public MapperResult(
      String ts,
      List<Variable> vars
  ) {
    this.ts = ts;
    this.vars = vars;
  }

  public String getTs() {
    return ts;
  }

  public List<Variable> getVars() {
    return vars;
  }
}
