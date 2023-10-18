package com.guxingke.intellij.plugin.postfix.template.struct;

import com.guxingke.intellij.plugin.Configs;
import com.guxingke.intellij.plugin.postfix.template.BasePostfixTemplate;
import com.guxingke.intellij.plugin.postfix.template.struct.config.MapperConfig;
import com.guxingke.intellij.plugin.postfix.template.struct.handler.DefaultHandler;
import com.guxingke.intellij.plugin.postfix.template.struct.handler.MapperHandler;
import com.guxingke.intellij.plugin.postfix.template.struct.handler.ProtobufInputHandler;
import com.guxingke.intellij.plugin.postfix.template.struct.handler.ProtobufOutputHandler;
import com.guxingke.intellij.plugin.util.PsiExpressionUtils;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiReferenceExpression;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

/**
 * pojo mapper
 *
 * 暂时只支持单入参
 */
public class DynamicStructMapperPostfixTemplate extends BasePostfixTemplate {

  private static final Logger log = Logger.getInstance(DynamicStructMapperPostfixTemplate.class);

  private List<MapperConfig> cfgs;

  private List<MapperHandler> handlers;

  public DynamicStructMapperPostfixTemplate(@Nullable PostfixTemplateProvider provider) {
    super("map", "map", "pojo mapper", cond(), provider);
    cfgs = loadCfg();

    handlers = List.of(new ProtobufOutputHandler(), new ProtobufInputHandler(), new DefaultHandler());
  }

  private static Condition<PsiElement> cond() {
    return expr -> {
      var e = (PsiExpression) expr;
      if (!(e instanceof PsiReferenceExpression)) {
        return false;
      }

      // current ele
      var ele = ((PsiReferenceExpression) e).getElement();
      // find current method
      PsiMethod method = PsiExpressionUtils.findMethod(ele);
      if (method == null) {
        return false;
      }

      // single parameter
      if (method.getParameterList().getParameters().length != 1) {
        return false;
      }

      var p1 = method.getParameterList().getParameter(0);
      if (p1.isVarArgs()) {
        return false;
      }
      if (!p1.getName().equals(e.getText())) {
        return false;
      }

      if (method.getReturnType() == null) {
        return false;
      }
      var prt = PsiExpressionUtils.findClass(method.getReturnType());
      var pt = PsiExpressionUtils.findClass(p1.getType());
      return pt != null && prt != null;
    };
  }

  @Override
  protected Template createTemplate(
      @NotNull TemplateManager manager,
      @NotNull PsiExpression e
  ) {
    // current ele
    var ele = ((PsiReferenceExpression) e).getElement();
    // find current method
    PsiMethod method = PsiExpressionUtils.findMethod(ele);
    var p1 = method.getParameterList().getParameter(0);
    var prt = PsiExpressionUtils.findClass(method.getReturnType());
    var pt = PsiExpressionUtils.findClass(p1.getType());

    var ctx = new MapperContext(method, pt, prt, e, e.getProject(), JavaPsiFacade.getInstance(e.getProject()), cfgs);

    var r = map(ctx);
    var tpl = manager.createTemplate(getId(), "", r.getTs());
    for (var var : r.getVars()) {
      tpl.addVariable(var.getName(),
                      var.getExpression(),
                      var.getDefaultValueExpression(),
                      var.isAlwaysStopAt(),
                      var.skipOnStart()
      );
    }
    return tpl;
  }

  private MapperResult map(MapperContext ctx) {
    var handler = handlers.stream().filter(it -> it.match(ctx)).findFirst().orElse(null);
    if (handler == null) {
      log.warn("not found mapper handler " + ctx.getExpression().getText());
      return new MapperResult("", List.of());
    } else {
      log.info("found mapper for " + ctx + " | " + handler.getClass().getName());
    }
    return handler.handle(ctx);
  }

  @Override
  public boolean isBuiltin() {
    return false;
  }

  @Override
  public boolean isEditable() {
    return false;
  }

  private List<MapperConfig> loadCfg() {
    var confDir = Configs.getConfDir();
    if (!confDir.toFile().exists() || !confDir.toFile().isDirectory()) {
      return List.of(MapperConfig.defaultConfig());
    }

    var mcd = confDir.resolve("mapper");
    if (!mcd.toFile().exists() || !mcd.toFile().isDirectory()) {
      return List.of(MapperConfig.defaultConfig());
    }

    try {
      var y = new Yaml();
      var mcs = Files.list(mcd)
          .peek(it -> System.out.println(it.getFileName()))
          .filter(it -> it.getFileName().toString().endsWith(".yml") || it.getFileName().toString().endsWith(".yaml"))
          .map(it -> {
            try {
              return y.loadAs(Files.newInputStream(it), MapperConfig.class);
            } catch (IOException e) {
              // TODO log
              return null;
            }
          })
          .filter(Objects::nonNull)
          .collect(Collectors.toList());

      if (mcs.isEmpty()) {
        return List.of(MapperConfig.defaultConfig());
      }
      return mcs;
    } catch (IOException e) {
      return List.of(MapperConfig.defaultConfig());
    }
  }
}
