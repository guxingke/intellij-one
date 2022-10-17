package com.guxingke.intellij.plugin.postfix.template.struct;

import com.guxingke.intellij.plugin.Configs;
import com.guxingke.intellij.plugin.postfix.template.BasePostfixTemplate;
import com.guxingke.intellij.plugin.postfix.template.struct.config.MapperConfig;
import com.guxingke.intellij.plugin.postfix.template.struct.config.MapperConfig.MapperDefinition;
import com.guxingke.intellij.plugin.postfix.template.struct.config.MapperConfig.MapperRequire;
import com.guxingke.intellij.plugin.util.PsiExpressionUtils;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.SelectionNode;
import com.intellij.codeInsight.template.impl.TextExpression;
import com.intellij.codeInsight.template.impl.Variable;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.search.GlobalSearchScope;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.lucene.geo.SimpleWKTShapeParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

/**
 * pojo mapper
 */
public class DynamicStructMapperPostfixTemplate extends BasePostfixTemplate {

  List<MapperConfig> cfgs;

  public DynamicStructMapperPostfixTemplate(@Nullable PostfixTemplateProvider provider) {
    super("map2", "map2", "pojo mapper", cond(), provider);
    cfgs = loadCfg();
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

    var ctx = new MapperContext(method, pt, prt, e, e.getProject(), JavaPsiFacade.getInstance(e.getProject()));

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
    var sb = new StringBuilder();
    List<Variable> vars = new ArrayList<>();

    vars.add(var("name", ctx.getExpression().getText()));
    sb.append("if ($name$ == null) {return null;}");
    vars.add(var("out", "d"));
    var name = ctx.getFacade()
        .findClass(ctx.getOutput().getQualifiedName(), GlobalSearchScope.projectScope(ctx.getProject()))
        .getQualifiedName();
    vars.add(var("inputClassName", name));
    sb.append("var $out$ = new $inputClassName$();");
    // all set method
    var record = ctx.getIntput().isRecord();
    var ims = ctx.getIntput().getAllMethods();
    var igm = Arrays.stream(ims)
        .filter(it -> it.getParameterList().isEmpty()) // 无参
        .filter(it -> it.getName().startsWith("get") || it.getName().startsWith("is"))
        .collect(Collectors.toMap(it -> propertyName(it.getName()), it -> it, (l, r) -> l));

    // record mode
    if (record) {
      var rgm = Arrays.stream(ims).filter(it -> it.getParameterList().isEmpty()) // 无参
          .collect(Collectors.toMap(it -> captureName(it.getName()), it -> it, (l, r) -> l));
      igm.putAll(rgm);
    }

    for (PsiMethod method : ctx.getOutput().getAllMethods()) {
      if (method.getParameterList().getParameters().length != 1) {
        continue;
      }
      var mn = method.getName();
      if (!mn.startsWith("set")) {
        continue;
      }

      var pn = propertyName(mn);
      var get = igm.get(pn);
      if (get == null) {
        vars.add(var(mn, null));
        sb.append("$out$.($" + mn + "$);");
        continue;
      }
      vars.add(var(mn, mn));
      vars.add(var(get.getName(), "%s()".formatted(get.getName())));

      // type
      var ot = method.getParameterList().getParameter(0).getType().getCanonicalText();
      var it = get.getReturnType().getCanonicalText();

      if (Objects.equals(ot, it)) {
        sb.append("$out$.$" + mn + "$" + "(" + "$name$.$" + get.getName() + "$" + ");");
      } else { // type not equals
        // filter template
        var md = match(ctx, it, ot);
        if (md == null) { // not found
          sb.append("$out$.$" + mn + "$" + "(" + "$name$.$" + get.getName() + "$" + ");");
        } else {
          // add variable
          var getExpr = "$name$.$" + get.getName() + "$";
          var tpl = md.getTemplate();
          tpl = tpl.replace("$getExpr$", getExpr);
          tpl = tpl.replace("$getReturnTypeName$", ot);
          sb.append("$out$.$" + mn + "$" + "(" + tpl + ");");
        }
      }
    }

    sb.append("return $out$;$END$");
    return new MapperResult(sb.toString(), vars);
  }

  private MapperDefinition match(
      MapperContext ctx,
      String inputClassName,
      String outputClassName
  ) {
    var facade = ctx.getFacade();
    var project = ctx.getProject();

    var is = facade.findClass(inputClassName, GlobalSearchScope.allScope(project));
    var os = facade.findClass(outputClassName, GlobalSearchScope.allScope(project));

    for (MapperConfig cfg : cfgs) {
      for (MapperDefinition d : cfg.getDefinitions()) {
        if (!PsiExpressionUtils.isClass(is, d.getIntputName())) {
          continue;
        }
        if (!PsiExpressionUtils.isClass(os, d.getOutputName())) {
          continue;
        }

        // TODO check requires
        return d;
      }
    }

    return null;
  }

  private Variable var(
      String name,
      String val
  ) {
    if (val == null) {
      return new Variable(name, new SelectionNode(), new SelectionNode(), false, false);
    }
    return new Variable(name, new TextExpression(val), new SelectionNode(), false, false);
  }

  private String propertyName(String methodName) {
    if (methodName.startsWith("is")) {
      return methodName.substring(2);
    }
    return methodName.substring(3);
  }

  public static String captureName(String name) {
    char[] cs = name.toCharArray();
    cs[0] -= 32;
    return String.valueOf(cs);
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
          .toList();

      if (mcs.isEmpty()) {
        return List.of(MapperConfig.defaultConfig());
      }
      return mcs;
    } catch (IOException e) {
      return List.of(MapperConfig.defaultConfig());
    }
  }
}
