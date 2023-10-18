package com.guxingke.intellij.plugin.postfix.template.struct.handler;

import com.guxingke.intellij.plugin.Const;
import com.guxingke.intellij.plugin.postfix.template.struct.MapperContext;
import com.guxingke.intellij.plugin.postfix.template.struct.MapperResult;
import com.guxingke.intellij.plugin.util.PsiExpressionUtils;
import com.intellij.codeInsight.template.impl.Variable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.GlobalSearchScope;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * protobuf -> pojo
 */
public class ProtobufInputHandler extends AbstractHandler implements MapperHandler {

  private static final Logger log = Logger.getInstance(ProtobufInputHandler.class);

  @Override
  public MapperResult handle(MapperContext ctx) {
    var sb = new StringBuilder();
    List<Variable> vars = new ArrayList<>();

    vars.add(var("__name", ctx.getExpression().getText()));
    sb.append("if ($__name$ == null) {return null;}");
    vars.add(var("__out", "d"));
    var name = ctx.getFacade()
        .findClass(ctx.getOutput().getQualifiedName(), GlobalSearchScope.allScope(ctx.getProject()))
        .getQualifiedName();
    vars.add(var("outputClassName", name));
    sb.append("var $__out$ = new $outputClassName$();");
    // all set method
    var ims = ctx.getInput().getAllMethods();
    var imcm = Arrays.stream(ims)
        .filter(it -> it.getParameterList().isEmpty())
        .filter(it -> it.getName().startsWith("has"))
        .filter(it -> it.getReturnType().getCanonicalText().equalsIgnoreCase("boolean"))
        .collect(Collectors.toMap(PsiMethod::getName, it -> it, (l, r) -> l));

    var igm = Arrays.stream(ims).filter(it -> it.getParameterList().isEmpty()) // 无参
        .filter(it -> !it.getName().startsWith("has"))
        .collect(Collectors.toMap(it -> propertyName(it.getName(), ctx.getInput().isRecord()), it -> it, (l, r) -> l));

    for (PsiMethod method : ctx.getOutput().getAllMethods()) {
      if (method.getParameterList().getParameters().length != 1) {
        continue;
      }
      var mn = method.getName();
      if (!mn.startsWith("set")) {
        continue;
      }

      var pn = propertyName(mn, ctx.getOutput().isRecord());
      var get = igm.get(pn);
      var getCheck = imcm.get("has" + pn);
      var setVar = "__set_" + pn;
      var getVar = "__get_" + pn;
      var getCheckVar = "__get_check_" + pn;
      if (get == null) {
        vars.add(var(setVar, mn));
        vars.add(var(getVar, null));
        sb.append("$__out$.$" + setVar + "$($" + getVar + "$);");
        continue;
      }
      vars.add(var(setVar, mn));
      vars.add(var(getVar, String.format("%s()", get.getName())));
      vars.add(var(getCheckVar, String.format("%s()", getCheck == null ? null : getCheck.getName())));

      // type
      var ot = method.getParameterList().getParameter(0).getType().getCanonicalText();
      var it = get.getReturnType().getCanonicalText();

      if (Objects.equals(ot, it)) {
        if (getCheck != null) {
          // d.setY(obj.hasY() ? obj.getY(): null)
          var to = "$__out$.$" + setVar + "$" + "(" + "$__name$.$" + getCheckVar + "$ ?" + "$__name$.$" + getVar
              + "$ : null" + ");";
          log.info("optional ... " + pn + " " + to);
          sb.append(to);
        } else {
          sb.append("$__out$.$" + setVar + "$" + "(" + "$__name$.$" + getVar + "$" + ");");
        }
      } else { // type not equals
        // filter template
        var md = match(ctx, it, ot);
        log.info("mapping for " + pn + " " + it + " to " + ot + " | " + getCheck + " | " + md);
        if (md == null) { // not found
          if (getCheck != null) {
            // d.setY(obj.hasY() ? obj.getY(): null)
            var to = "$__out$.$" + setVar + "$" + "(" + "$__name$.$" + getCheckVar + "$ ?" + "$__name$.$" + getVar
                + "$ : null" + ");";
            log.info("optional ... " + pn + " " + to);
            sb.append(to);
          } else {
            sb.append("$__out$.$" + setVar + "$" + "(" + "$__name$.$" + getVar + "$" + ");");
          }
        } else {
          // add variable
          var getExpr = "$__name$.$" + getVar + "$";
          var tpl = md.getTemplate();
          tpl = tpl.replace("$__getExpr$", getExpr);
          tpl = tpl.replace("$__outputName$", ot);
          tpl = tpl.replace("$__inputName$", it);
          sb.append("$__out$.$" + setVar + "$" + "(" + tpl + ");");
        }
      }
    }

    sb.append("return $__out$;$END$");
    return new MapperResult(sb.toString(), vars);
  }

  @Override
  public boolean match(MapperContext ctx) {
    var i = PsiExpressionUtils.isClass(ctx.getInput(), Const.CLS_COM_GOOGLE_PROTOBUF_GENERATEDMESSAGEV3);
    var o = PsiExpressionUtils.isClass(ctx.getOutput(), Const.CLS_COM_GOOGLE_PROTOBUF_GENERATEDMESSAGEV3);
    return i && !o;
  }
}
