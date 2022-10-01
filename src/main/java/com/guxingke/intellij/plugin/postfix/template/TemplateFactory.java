package com.guxingke.intellij.plugin.postfix.template;

import com.guxingke.intellij.plugin.postfix.template.conf.TemplateConf;
import com.guxingke.intellij.plugin.postfix.template.conf.TemplateConfig;
import com.guxingke.intellij.plugin.postfix.template.conf.TemplateDefinition;
import com.guxingke.intellij.plugin.postfix.template.conf.TemplateItem;
import com.guxingke.intellij.plugin.postfix.template.conf.TemplateItemRequire;
import com.guxingke.intellij.plugin.util.PsiExpressionUtils;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplate;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiArrayType;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiPrimitiveType;
import com.intellij.psi.search.GlobalSearchScope;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class TemplateFactory {

  /**
   * 默认配置目录，当前用户目录下 `.config/the-one-toolbox`。
   *
   * 自定义模板配置配置， 配置目录下 `templates` 目录下的 yml 文件。
   */
  public static Set<PostfixTemplate> createTemplates(PostfixTemplateProvider provider) {
    // default config path
    String dir = System.getenv("HOME") + "/.config/the-one-toolbox";
    var cfgDir = System.getenv("INTELLIJ_THE_ONE_TOOLBOX_CONFIG_DIR");
    if (cfgDir != null) {
      dir = cfgDir;
    }

    if (!Files.exists(Paths.get(dir))) {
      return new HashSet<>();
    }

    if (!Files.isDirectory(Paths.get(dir))) {
      return new HashSet<>();
    }

    try {
      var cfgs = Files.list(Paths.get(dir, "templates"))
          .filter(it -> it.getFileName().toString().endsWith(".yml") || it.getFileName().toString().endsWith(".yaml"))
          .map(it -> TemplateConfig.load(it.toAbsolutePath().toString()))
          .toList();
      return cfgs.stream()
          .flatMap(it -> build(it, provider).stream())
          .filter(Objects::nonNull)
          .collect(Collectors.toSet());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static Set<PostfixTemplate> createBuiltinTemplates(PostfixTemplateProvider provider) {
    var resource = TemplateFactory.class.getResourceAsStream("/builtin.yml");
    if (resource == null) {
      return new HashSet<>();
    }

    var cfg = TemplateConfig.load(resource);
    if (cfg == null) {
      return new HashSet<>();
    }

    return build(cfg, provider).stream().filter(Objects::nonNull).collect(Collectors.toSet());
  }

  public static Set<PostfixTemplate> build(
      TemplateConf conf,
      PostfixTemplateProvider provider
  ) {
    var ns = conf.getNamespace();
    return conf.getDefinitions().stream().map(it -> build(ns, it, provider)).collect(Collectors.toSet());
  }

  public static PostfixTemplate build(
      String ns,
      TemplateDefinition definition,
      PostfixTemplateProvider provider
  ) {

    var name = definition.getName();
    var id = ns + "_" + name;
    var example = definition.getDescription();

    var tpls = definition.getTemplates();
    if (tpls == null) {
      return null;
    }

    Condition<PsiElement> condition = build(tpls);
    return new ScriptPostfixTemplate(id, name, example, definition, condition, provider);
  }

  private static Condition<PsiElement> build(List<TemplateItem> tpls) {
    return expr -> {
      var e = (PsiExpression) expr;
      var project = e.getProject();
      var type = e.getType();

      var facade = JavaPsiFacade.getInstance(project);

      String clazz = null;
      PsiClass psiClass = null;
      String componentClazz = null;
      PsiClass cc = null;

      boolean primitive = false;
      if (type instanceof PsiPrimitiveType pt) {
        primitive = true;
        clazz = pt.getName();
      }
      boolean normal = false;
      boolean generic = false;
      if (type instanceof PsiClassType ct) {
        psiClass = PsiExpressionUtils.findClass(e);
        clazz = psiClass.getQualifiedName();

        if (ct.resolve().hasTypeParameters()) { // 泛型
          normal = true;
          generic = true;
          cc = PsiExpressionUtils.findComponentClass(e);
          componentClazz = cc.getQualifiedName();
        } else {
          normal = true;
        }
      }
      boolean array = false;
      if (type instanceof PsiArrayType at) {
        array = true;
        clazz = "ARRAY";
        componentClazz = PsiExpressionUtils.findClass(at.getComponentType()).getQualifiedName();
      }

      var ctx = new ConditionContext(primitive,
                                     array,
                                     generic,
                                     normal,
                                     clazz,
                                     psiClass,
                                     componentClazz,
                                     cc,
                                     e,
                                     project,
                                     facade
      );
      var tpl = match(ctx, tpls);
      return tpl != null;
    };
  }

  public static TemplateItem match(
      ConditionContext ctx,
      List<TemplateItem> tpls
  ) {
    for (TemplateItem tpl : tpls) {
      var match = match(ctx, tpl);
      if (match != null) {
        return match;
      }
    }

    return null;
  }

  public static TemplateItem match(
      ConditionContext ctx,
      TemplateItem tpl
  ) {
    // check type
    switch (tpl.getType()) {
      case "primitive":
        if (!ctx.isPrimitive()) {
          return null;
        }
        if (!ctx.getClazzName().equals(tpl.getClazz())) {
          return null;
        }
        break;
      case "generic":
        if (!ctx.isGeneric()) {
          return null;
        }
        if (!PsiExpressionUtils.isClass(ctx.getExpression(), tpl.getClazz())) {
          return null;
        }
        break;
      case "array":
        if (!ctx.isArray()) {
          return null;
        }
        if (!ctx.getClazzName().equals(tpl.getClazz())) {
          return null;
        }
        break;
      case "normal":
        if (!ctx.isNormal()) {
          return null;
        }
        if (!PsiExpressionUtils.isClass(ctx.getExpression(), tpl.getClazz())) {
          return null;
        }
        break;
      default:
        return null;
    }

    // requires
    if (tpl.getRequires() == null || tpl.getRequires().isEmpty()) {
      return tpl;
    }

    for (TemplateItemRequire require : tpl.getRequires()) {
      if (require.isClass()) {
        var name = require.getName();
        var cc = ctx.getFacade().findClass(name, GlobalSearchScope.allScope(ctx.getProject()));
        if (cc != null) {
          return tpl;
        }
      }

      if (require.isMethod()) { // method
        var cls = require.getClazz();
        var name = require.getName();
        PsiClass cc = null;
        if (cls.startsWith("$") && cls.endsWith("$")) { // 变量
          var vn = cls.substring(1, cls.length() - 2);
          if (vn.equals("componentClass")) {
            if (ctx.getComponentClass() != null) {
              cc = ctx.getComponentClass();
            }
          }
        } else {
          cc = ctx.getFacade().findClass(cls, GlobalSearchScope.projectScope(ctx.getProject()));
        }

        if (cc != null) {
          for (PsiMethod m : cc.getAllMethods()) {
            if (m.getName().equals(name)) {
              return tpl;
            }
          }
        }
      }

      if (require.isComponentClass()) {
        if (ctx.getComponentClass() != null) {
          if (Objects.equals(ctx.getComponentClassName(), require.getName())) {
            return tpl;
          }
        }
      }
    }

    return null;
  }
}
