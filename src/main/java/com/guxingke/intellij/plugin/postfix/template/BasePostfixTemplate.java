package com.guxingke.intellij.plugin.postfix.template;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateWithExpressionSelector;
import com.intellij.codeInsight.template.postfix.util.JavaPostfixTemplatesUtils;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpression;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 基础 postfix template
 */
public abstract class BasePostfixTemplate extends PostfixTemplateWithExpressionSelector {

  protected BasePostfixTemplate(
      @Nullable @NonNls String id,
      @NotNull @NlsSafe String name,
      @NotNull @NlsSafe String example,
      @NotNull Condition<PsiElement> condition,
      @Nullable PostfixTemplateProvider provider
  ) {
    super(id, name, example, JavaPostfixTemplatesUtils.selectorTopmost(condition), provider);
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

    var tpl = createTemplate(manager, e);
    if (tpl == null) {
      document.deleteString(expression.getTextRange().getStartOffset(), expression.getTextRange().getEndOffset());
      return;
    }

    tpl.setToReformat(true);
    document.deleteString(expression.getTextRange().getStartOffset(), expression.getTextRange().getEndOffset());
    manager.startTemplate(editor, tpl);
  }

  /**
   * 生成模板
   */
  protected abstract Template createTemplate(
      @NotNull TemplateManager manager,
      @NotNull PsiExpression e
  );

}
