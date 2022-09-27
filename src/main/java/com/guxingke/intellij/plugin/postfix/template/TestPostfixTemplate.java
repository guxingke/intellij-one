package com.guxingke.intellij.plugin.postfix.template;

import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.TextExpression;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateWithExpressionSelector;
import com.intellij.codeInsight.template.postfix.util.JavaPostfixTemplatesUtils;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TestPostfixTemplate extends PostfixTemplateWithExpressionSelector {

  public TestPostfixTemplate(@Nullable PostfixTemplateProvider provider) {
    super("tsoutv", "tsoutv", "v", JavaPostfixTemplatesUtils.selectorTopmost(cond()), provider);
  }

  private static Condition<PsiElement> cond() {
    return e -> {
      var type = ((PsiExpression) e).getType();
      if (type == null) {
        return false;
      }
      var pc = JavaPsiFacade.getInstance(e.getProject()).findClass(type.getCanonicalText(), e.getResolveScope());
      return pc != null;
    };
  }

  @Override
  protected void expandForChooseExpression(
      @NotNull PsiElement expression,
      @NotNull Editor editor
  ) {
    var project = expression.getProject();
    var manager = TemplateManager.getInstance(project);
    var document = editor.getDocument();

    document.deleteString(expression.getTextRange().getStartOffset(), expression.getTextRange().getEndOffset());

    // soutv
    var tpl = manager.createTemplate(getId(), "", "System.out.println($expr$);$END$");
    tpl.addVariable("expr", new TextExpression(expression.getText()), false);
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
