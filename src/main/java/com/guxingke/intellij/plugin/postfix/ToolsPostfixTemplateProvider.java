package com.guxingke.intellij.plugin.postfix;

import com.guxingke.intellij.plugin.postfix.template.TemplateFactory;
import com.guxingke.intellij.plugin.postfix.template.struct.StructMapperPostfixTemplate;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplate;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

public class ToolsPostfixTemplateProvider implements PostfixTemplateProvider {

  private final Set<PostfixTemplate> templates;

  public ToolsPostfixTemplateProvider() {
    var internals = Set.of(new StructMapperPostfixTemplate(this));

    var builtinTemplates = TemplateFactory.createBuiltinTemplates(this);
    var externals = TemplateFactory.createTemplates(this);
    templates = Stream.of(externals, builtinTemplates, internals)
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());
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
