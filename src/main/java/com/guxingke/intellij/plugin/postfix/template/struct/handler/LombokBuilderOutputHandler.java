package com.guxingke.intellij.plugin.postfix.template.struct.handler;

import com.guxingke.intellij.plugin.postfix.template.struct.MapperContext;
import com.guxingke.intellij.plugin.postfix.template.struct.MapperResult;
import com.intellij.codeInsight.template.impl.Variable;
import com.intellij.openapi.diagnostic.Logger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * pojo -> pojo with lombok builder
 */
public class LombokBuilderOutputHandler extends AbstractHandler implements MapperHandler {

  private static final Logger log = Logger.getInstance(LombokBuilderOutputHandler.class);

  @Override
  public MapperResult handle(MapperContext ctx) {
    var sb = new StringBuilder();
    List<Variable> vars = new ArrayList<>();

    vars.add(var("__name", ctx.getExpression().getText()));
    sb.append("if ($__name$ == null) {return null;}");
    vars.add(var("__out", "b"));
    var name = ctx.getOutput().getQualifiedName();

    log.info("output name " + name);

    vars.add(var("outputClassName", name));
    sb.append("var $__out$ = $outputClassName$.builder();");
    // builder

    // all set method
    var ims = ctx.getInput().getAllMethods();
    var igm = Arrays.stream(ims).filter(it -> it.getParameterList().isEmpty()) // 无参
        .collect(Collectors.toMap(it -> propertyName(it.getName(), ctx.getInput().isRecord()), it -> it, (l, r) -> l));

    for (var set : ctx.getOutput().getAllFields()) {
      var mn = set.getName();
      var pn = propertyName(mn, true);

      var get = igm.get(pn);
      var setVar = "__set_" + pn;
      var getVar = "__get_" + pn;
      if (get == null) {
        vars.add(var(setVar, mn));
        vars.add(var(getVar, null));
        sb.append("$__out$.$" + setVar + "$($" + getVar + "$);");
        continue;
      }
      vars.add(var(setVar, mn));
      vars.add(var(getVar, String.format("%s()", get.getName())));

      // type
      var ot = set.getType().getCanonicalText();
      var it = get.getReturnType().getCanonicalText();

      if (Objects.equals(ot, it)) {
        sb.append("$__out$.$" + setVar + "$" + "(" + "$__name$.$" + getVar + "$" + ");");
      } else { // type not equals
        // filter template
        var md = match(ctx, it, ot);
        if (md == null) { // not found
          sb.append("$__out$.$" + setVar + "$" + "(" + "$__name$.$" + getVar + "$" + ");");
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

    sb.append("return $__out$.build();$END$");
    return new MapperResult(sb.toString(), vars);
  }

  @Override
  public boolean match(MapperContext ctx) {
    var bo = Arrays.stream(ctx.getOutput().findMethodsByName("builder"))
        .filter(it -> !it.hasParameters())
        .findFirst()
        .orElse(null) != null;
    var bi = Arrays.stream(ctx.getInput().findMethodsByName("builder"))
        .filter(it -> !it.hasParameters())
        .findFirst()
        .orElse(null) != null;

    return bo && !bi;
  }
}
