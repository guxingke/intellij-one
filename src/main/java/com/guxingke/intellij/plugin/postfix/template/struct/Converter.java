package com.guxingke.intellij.plugin.postfix.template.struct;


import com.intellij.psi.PsiClass;

public interface Converter {

  String to(
      String out,
      String in,
      String val
  );

  boolean match(
      PsiClass out,
      PsiClass in
  );
}
