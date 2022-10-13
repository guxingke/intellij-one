package com.guxingke.intellij.plugin.postfix.template.struct;

import com.guxingke.intellij.plugin.postfix.template.BasePostfixTemplate;
import com.guxingke.intellij.plugin.util.PsiExpressionUtils;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.SelectionNode;
import com.intellij.codeInsight.template.impl.TextExpression;
import com.intellij.codeInsight.template.impl.Variable;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.search.GlobalSearchScope;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * pojo mapper
 */
public class DynamicStructMapperPostfixTemplate extends BasePostfixTemplate {

  List<Converter> converters = List.of(new EnumStringConverter(), new StringEnumConverter());

  public DynamicStructMapperPostfixTemplate(@Nullable PostfixTemplateProvider provider) {
    super("map2", "map2", "pojo mapper", cond(), provider);
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
    var ims = ctx.getIntput().getAllMethods();
    var igm = Arrays.stream(ims)
        .filter(it -> it.getParameterList().isEmpty()) // 无参
        .filter(it -> it.getName().startsWith("get") || it.getName().startsWith("is"))
        .collect(Collectors.toMap(it -> propertyName(it.getName()), it -> it, (l, r) -> l));

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
      sb.append("$out$.$" + mn + "$" + "(" + "$name$.$" + get.getName() + "$" + ");");
    }

    sb.append("return $name$;$END$");
    return new MapperResult(sb.toString(), vars);
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


  private String mapper(
      String name,
      PsiClass input,
      PsiClass output
  ) {
    List<String> ss = new ArrayList<>();
    ss.add("var d = " + "new " + output.getQualifiedName() + "();");
    // all set method
    var ims = input.getAllMethods();
    var igm = Arrays.stream(ims)
        .filter(it -> it.getParameterList().isEmpty()) // 无参
        .filter(it -> it.getName().startsWith("get") || it.getName().startsWith("is"))
        .collect(Collectors.toMap(it -> propertyName(it.getName()), it -> it, (l, r) -> l));

    for (PsiMethod method : output.getAllMethods()) {
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
        ss.add("d." + method.getName() + "($" + method.getName() + "$);");
        continue;
      }
      ss.add("d." + conv(name, method, get));
    }

    ss.add("return d;");
    return String.join("\n", ss);
  }

  private String conv(
      @NotNull String objName,
      @NotNull PsiMethod set,
      @NotNull PsiMethod get
  ) {
    var ot = set.getParameterList().getParameter(0).getType();
    var it = get.getReturnType();
    // if type equals
    assert it != null;
    if (ot.getCanonicalText().equals(it.getCanonicalText())) {
      return "%s(%s.%s());".formatted(set.getName(), objName, get.getName());
    }

    // TODO type cast
    var converter = lookup(ot, it);
    if (converter == null) {
      return "%s(%s.%s());".formatted(set.getName(), objName, get.getName());
    }
    var v = converter.to(ot.getCanonicalText(), it.getCanonicalText(), "%s.%s()".formatted(objName, get.getName()));
    return "%s(%s);".formatted(set.getName(), v);
  }

  private Converter lookup(
      PsiType out,
      PsiType in
  ) {
    if (in == null) {
      return null;
    }
    if (!(in instanceof PsiClassReferenceType)) {
      return null;
    }

    var is = ((PsiClassReferenceType) in).resolve();
    if (is == null) {
      return null;
    }

    if (!(out instanceof PsiClassReferenceType)) {
      return null;
    }

    var os = ((PsiClassReferenceType) out).resolve();
    if (os == null) {
      return null;
    }

    return converters.stream().filter(it -> it.match(os, is)).findFirst().orElse(null);
  }

  private String propertyName(String methodName) {
    if (methodName.startsWith("is")) {
      return methodName.substring(2);
    }
    return methodName.substring(3);
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
