package com.guxingke.intellij.plugin.postfix;

import com.guxingke.intellij.plugin.postfix.template.TestPostfixTemplate;
import com.guxingke.intellij.plugin.postfix.template.ToIdentifierMapPostfixTemplate;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplate;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public class ToolsPostfixTemplateProvider implements PostfixTemplateProvider {

  private final Set<PostfixTemplate> templates = Set.of(new TestPostfixTemplate(this),
                                                        new ToIdentifierMapPostfixTemplate(this)
  );

  @Override
  public @NotNull Set<PostfixTemplate> getTemplates() {
    return templates;
  }

  @Override
  public boolean isTerminalSymbol(char currentChar) {
    return currentChar == '.';
  }

  @Override
  public void preExpand(
      @NotNull PsiFile file,
      @NotNull Editor editor
  ) {
  }

  @Override
  public void afterExpand(
      @NotNull PsiFile file,
      @NotNull Editor editor
  ) {
  }

  @Override
  public @NotNull PsiFile preCheck(
      @NotNull PsiFile copyFile,
      @NotNull Editor realEditor,
      int currentOffset
  ) {
    return copyFile;
  }
}
