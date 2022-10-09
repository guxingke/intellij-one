package com.guxingke.intellij.plugin.postfix;

import com.guxingke.intellij.plugin.Configs;
import com.guxingke.intellij.plugin.postfix.template.TemplateFactory;
import com.guxingke.intellij.plugin.postfix.template.struct.StructMapperPostfixTemplate;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplate;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

public class ToolsPostfixTemplateProvider implements PostfixTemplateProvider {

  private final Set<PostfixTemplate> templates;

  public ToolsPostfixTemplateProvider() {
    var cfg = Configs.getConfig();
    if (!cfg.getPostfix().isEnable()) {
      templates = new HashSet<>();
      return;
    }

    var ic = cfg.getPostfix().getInternal();
    Set<PostfixTemplate> internals = new HashSet<>();
    if (ic.isEnable()) {
      internals = Stream.of(new StructMapperPostfixTemplate(this))
          .filter(it -> !ic.getBlocklist().contains(it.getPresentableName()))
          .collect(Collectors.toSet());
    }

    Set<PostfixTemplate> builtinTemplates = new HashSet<>();
    var bc = cfg.getPostfix().getBuiltin();
    if (bc.isEnable()) {
      builtinTemplates = TemplateFactory.createBuiltinTemplates(this)
          .stream()
          .filter(it -> !bc.getBlocklist().contains(it.getPresentableName()))
          .collect(Collectors.toSet());
    }

    Set<PostfixTemplate> externals = new HashSet<>();
    var ec = cfg.getPostfix().getExternal();
    if (ec.isEnable()) {
      externals = TemplateFactory.createExternalTemplates(this)
          .stream()
          .filter(it -> !ec.getBlocklist().contains(it.getPresentableName()))
          .collect(Collectors.toSet());
    }
    // 按触发词去重, external -> builtin -> internal 优先级
    templates = new HashSet<>(Stream.of(externals, builtinTemplates, internals)
                                  .flatMap(Collection::stream)
                                  .collect(Collectors.toMap(PostfixTemplate::getPresentableName, it -> it, (l, r) -> r))
                                  .values());
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
