package com.guxingke.intellij.plugin.postfix;

import com.guxingke.intellij.plugin.Configs;
import com.guxingke.intellij.plugin.postfix.template.TemplateFactory;
import com.guxingke.intellij.plugin.postfix.template.struct.DynamicStructMapperPostfixTemplate;
import com.guxingke.intellij.plugin.postfix.template.struct.StructMapperPostfixTemplate;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplate;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

public class ToolsPostfixTemplateProvider implements PostfixTemplateProvider {

  private static final Logger log = Logger.getInstance(ToolsPostfixTemplateProvider.class);

  private long lastGet = System.currentTimeMillis();

  private Set<PostfixTemplate> templates;
  private boolean init = false;

  public ToolsPostfixTemplateProvider() {
    this.templates = loadTemplates();
  }

  private synchronized Set<PostfixTemplate> loadTemplates() {
    log.info("load templates begin ...");
    var cfg = Configs.getConfig();
    log.info("config mode debug: " + cfg.isDebug());
    if (init && !cfg.isDebug()) { // return exists
      log.info("reuse exists templates " + templates.size());
      return templates;
    }

    if (!cfg.getPostfix().isEnable()) {
      log.info("postfix status is global disable");
      return new HashSet<>();
    }

    var ic = cfg.getPostfix().getInternal();
    Set<PostfixTemplate> internals = new HashSet<>();
    if (ic.isEnable()) {
      internals = Stream.of(new StructMapperPostfixTemplate(this), new DynamicStructMapperPostfixTemplate(this))
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
    init = true;
    var ts = Stream.of(externals, builtinTemplates, internals)
        .flatMap(Collection::stream)
        .collect(Collectors.toMap(PostfixTemplate::getPresentableName, it -> it, (l, r) -> r))
        .values();
    log.info("load templates done " + ts.size());
    return new HashSet<>(ts);
  }

  @Override
  public @NotNull Set<PostfixTemplate> getTemplates() {
    var now = System.currentTimeMillis();
    if (now - lastGet > 60 * 1000) { // 1 min
      this.templates = loadTemplates();
      lastGet = now;
    }
    log.info("got templates " + templates.size());
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
