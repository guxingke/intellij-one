package com.guxingke.intellij.plugin.postfix.template.struct;

import com.guxingke.intellij.plugin.Const;
import com.guxingke.intellij.plugin.util.PsiExpressionUtils;
import com.intellij.psi.PsiClass;

public class StringEnumConverter implements Converter {

  @Override
  public String to(
      String out,
      String in,
      String val
  ) {
    return "%s == null ? null : Enum.valueOf(%s.class, %s)".formatted(val, out, val);
  }

  @Override
  public boolean match(
      PsiClass out,
      PsiClass in
  ) {
    return out.isEnum() && PsiExpressionUtils.isClass(in, Const.CLS_JAVA_LANG_STRING);
  }
}
