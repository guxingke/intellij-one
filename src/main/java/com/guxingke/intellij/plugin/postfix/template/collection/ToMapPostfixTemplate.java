package com.guxingke.intellij.plugin.postfix.template.collection;

import com.guxingke.intellij.plugin.Const;
import com.guxingke.intellij.plugin.postfix.template.BasePostfixTemplate;
import com.guxingke.intellij.plugin.util.PsiExpressionUtils;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.TextExpression;
import com.intellij.codeInsight.template.impl.Variable;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ToMapPostfixTemplate extends BasePostfixTemplate {

  public ToMapPostfixTemplate(@Nullable PostfixTemplateProvider provider) {
    super("toMap", "toMap", "convert to map", cond(), provider);
  }

  private static Condition<PsiElement> cond() {
    return e -> {
      var ee = (PsiExpression) e;

      var tg = PsiExpressionUtils.isTypeGeneric(ee);
      if (!tg) {
        return false;
      }

      var coll = PsiExpressionUtils.isClass(ee, Const.CLS_JAVA_UTIL_COLLECTION);
      var stream = PsiExpressionUtils.isClass(ee, Const.CLS_JAVA_UTIL_STREAM_STREAM);
      if (!coll && !stream) {
        return false;
      }

      return PsiExpressionUtils.findComponentClass(ee) != null;
    };
  }

  @Override
  protected Template createTemplate(
      @NotNull TemplateManager manager,
      @NotNull PsiExpression e
  ) {
    var cls = PsiExpressionUtils.findComponentClass(e);
    var common = ".collect(java.util.stream.Collectors.toMap($componentClassName$::$keyF$, $valueF$))$END$";
    var stream = PsiExpressionUtils.isClass(e, Const.CLS_JAVA_UTIL_STREAM_STREAM);
    var ts = "$expr$.stream()" + common;
    if (stream) {
      ts = "$expr$" + common;
    }

    var tpl = manager.createTemplate(getId(), "", ts);
    tpl.addVariable("expr", new TextExpression(e.getText()), false);
    tpl.addVariable("componentClassName", new TextExpression(cls.getQualifiedName()), false);

    var fv = new Variable("keyF", "", "", true);
    tpl.addVariable(fv.getName(), fv.getExpression(), fv.getDefaultValueExpression(), true, false);
    var vv = new Variable("valueF", "", "", true);
    tpl.addVariable(vv.getName(), vv.getExpression(), vv.getDefaultValueExpression(), true, false);
    return tpl;
  }

  @Override
  public boolean isBuiltin() {
    return false;
  }

  @Override
  public boolean isEditable() {
    return false;
  }
}
