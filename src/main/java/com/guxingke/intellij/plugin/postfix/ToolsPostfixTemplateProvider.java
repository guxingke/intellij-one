package com.guxingke.intellij.plugin.postfix;

import com.guxingke.intellij.plugin.postfix.template.GroupingByPostfixTemplate;
import com.guxingke.intellij.plugin.postfix.template.JoiningPostfixTemplate;
import com.guxingke.intellij.plugin.postfix.template.PartitioningByPostfixTemplate;
import com.guxingke.intellij.plugin.postfix.template.ToArrayPostfixTemplate;
import com.guxingke.intellij.plugin.postfix.template.ToIdentifierMapPostfixTemplate;
import com.guxingke.intellij.plugin.postfix.template.ToListPostfixTemplate;
import com.guxingke.intellij.plugin.postfix.template.ToMapPostfixTemplate;
import com.guxingke.intellij.plugin.postfix.template.ToSetPostfixTemplate;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplate;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public class ToolsPostfixTemplateProvider implements PostfixTemplateProvider {

  private final Set<PostfixTemplate> templates;

  public ToolsPostfixTemplateProvider() {
    templates = Set.of(new ToIdentifierMapPostfixTemplate(this),
                       new ToMapPostfixTemplate(this),
                       new ToListPostfixTemplate(this),
                       new ToSetPostfixTemplate(this),
                       new JoiningPostfixTemplate(this),
                       new GroupingByPostfixTemplate(this),
                       new PartitioningByPostfixTemplate(this),
                       new ToArrayPostfixTemplate(this)
    );
  }


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
