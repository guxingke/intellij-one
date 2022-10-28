//package com.guxingke.intellij.plugin.postfix.template.struct;
//
//import com.guxingke.intellij.plugin.Const;
//import com.guxingke.intellij.plugin.util.PsiExpressionUtils;
//import com.intellij.psi.PsiClass;
//
//public class EnumStringConverter implements Converter {
//
//  @Override
//  public String to(
//      String out,
//      String in,
//      String val
//  ) {
//    return "%s == null ? null : %s.name()".formatted(val, val);
//  }
//
//  @Override
//  public boolean match(
//      PsiClass out,
//      PsiClass in
//  ) {
//    return in.isEnum() && PsiExpressionUtils.isClass(out, Const.CLS_JAVA_LANG_STRING);
//  }
//}
