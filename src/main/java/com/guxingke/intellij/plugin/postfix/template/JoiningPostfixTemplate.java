package com.guxingke.intellij.plugin.postfix.template;

import com.guxingke.intellij.plugin.Const;
import com.guxingke.intellij.plugin.util.PsiExpressionUtils;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.TextExpression;
import com.intellij.codeInsight.template.impl.Variable;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateWithExpressionSelector;
import com.intellij.codeInsight.template.postfix.util.JavaPostfixTemplatesUtils;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpression;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JoiningPostfixTemplate extends PostfixTemplateWithExpressionSelector {

  public JoiningPostfixTemplate(@Nullable PostfixTemplateProvider provider) {
    super("joining", "joining", "joining strings", JavaPostfixTemplatesUtils.selectorTopmost(cond()), provider);
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

      var cc = PsiExpressionUtils.findComponentClass(ee);
      return cc != null && Objects.equals(cc.getQualifiedName(), Const.CLS_JAVA_LANG_STRING);
    };
  }

  @Override
  protected void expandForChooseExpression(
      @NotNull PsiElement expression,
      @NotNull Editor editor
  ) {
    var e = (PsiExpression) expression;

    var project = expression.getProject();
    var manager = TemplateManager.getInstance(project);
    var document = editor.getDocument();

    var cls = PsiExpressionUtils.findComponentClass(e);
    if (cls == null) {
      return;
    }

    var common = ".collect(java.util.stream.Collectors.joining($keyF$))$END$";
    var stream = PsiExpressionUtils.isClass(e, Const.CLS_JAVA_UTIL_STREAM_STREAM);
    var ts = "$expr$.stream()" + common;
    if (stream) {
      ts = "$expr$" + common;
    }

    document.deleteString(expression.getTextRange().getStartOffset(), expression.getTextRange().getEndOffset());

    var tpl = manager.createTemplate(getId(), "", ts);
    tpl.addVariable("expr", new TextExpression(expression.getText()), false);

    var fv = new Variable("keyF", "", ",", true);
    tpl.addVariable(fv.getName(), fv.getExpression(), fv.getDefaultValueExpression(), true, false);
    tpl.setToReformat(true);
    manager.startTemplate(editor, tpl);
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
