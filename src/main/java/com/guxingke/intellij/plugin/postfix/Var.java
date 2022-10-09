package com.guxingke.intellij.plugin.postfix;

import com.intellij.codeInsight.template.impl.Variable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Var extends Variable {

  private final String varCode;
  private final boolean skipOnStart;
  private final int no;

  public Var(
      @NotNull String name,
      @Nullable String expression,
      @Nullable String defaultValue,
      boolean alwaysStopAt,
      boolean skipOnStart,
      int no,
      String varCode
  ) {
    super(name, expression, defaultValue, alwaysStopAt);
    this.skipOnStart = skipOnStart;
    this.no = no;
    this.varCode = varCode;
  }

  @Override
  public boolean skipOnStart() {
    return skipOnStart;
  }

  public int getNo() {
    return no;
  }

  public String getVarCode() {
    return varCode;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 29 * result + (skipOnStart ? 1 : 0);
    result = 29 * result + no;
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (!super.equals(o)) {
      return false;
    }

    Var that = (Var) o;

    return this.skipOnStart == that.skipOnStart && this.no == that.no;
  }
}
