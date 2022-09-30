package com.guxingke.intellij.plugin.postfix;

import com.guxingke.intellij.plugin.postfix.template.TemplateFactory;
import com.guxingke.intellij.plugin.postfix.template.collection.GroupingByPostfixTemplate;
import com.guxingke.intellij.plugin.postfix.template.collection.JoiningPostfixTemplate;
import com.guxingke.intellij.plugin.postfix.template.collection.PartitioningByPostfixTemplate;
import com.guxingke.intellij.plugin.postfix.template.struct.StructMapperPostfixTemplate;
import com.guxingke.intellij.plugin.postfix.template.collection.ToArrayPostfixTemplate;
import com.guxingke.intellij.plugin.postfix.template.collection.ToIdentifierMapPostfixTemplate;
import com.guxingke.intellij.plugin.postfix.template.collection.ToListPostfixTemplate;
import com.guxingke.intellij.plugin.postfix.template.collection.ToMapPostfixTemplate;
import com.guxingke.intellij.plugin.postfix.template.collection.ToSetPostfixTemplate;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplate;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

public class ToolsPostfixTemplateProvider implements PostfixTemplateProvider {

  private final Set<PostfixTemplate> templates;

  public ToolsPostfixTemplateProvider() {
    var builtin = Set.of(new ToIdentifierMapPostfixTemplate(this),
                         new ToMapPostfixTemplate(this),
                         new ToListPostfixTemplate(this),
                         new ToSetPostfixTemplate(this),
                         new JoiningPostfixTemplate(this),
                         new GroupingByPostfixTemplate(this),
                         new PartitioningByPostfixTemplate(this),
                         new ToArrayPostfixTemplate(this),
                         new StructMapperPostfixTemplate(this)
    );

    var externals = TemplateFactory.createTemplates(this);
    templates = Stream.concat(externals.stream(), builtin.stream()).collect(Collectors.toSet());
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
