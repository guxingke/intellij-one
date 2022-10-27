package com.guxingke.intellij.plugin.postfix.template.struct.handler;

import com.guxingke.intellij.plugin.postfix.template.struct.MapperContext;
import com.guxingke.intellij.plugin.postfix.template.struct.config.MapperConfig;
import com.guxingke.intellij.plugin.postfix.template.struct.config.MapperConfig.MapperDefinition;
import com.guxingke.intellij.plugin.postfix.template.struct.config.MapperConfig.MapperRequire;
import com.guxingke.intellij.plugin.util.PsiExpressionUtils;
import com.intellij.codeInsight.template.impl.SelectionNode;
import com.intellij.codeInsight.template.impl.TextExpression;
import com.intellij.codeInsight.template.impl.Variable;
import com.intellij.psi.search.GlobalSearchScope;
import java.util.Arrays;

public abstract class AbstractHandler implements MapperHandler {

  protected Variable var(
      String name,
      String val
  ) {
    if (val == null) {
      return new Variable(name, new SelectionNode(), new SelectionNode(), false, false);
    }
    return new Variable(name, new TextExpression(val), new SelectionNode(), false, false);
  }

  protected String propertyName(
      String methodName,
      boolean record
  ) {
    if (record) {
      return captureName(methodName);
    }
    if (methodName.startsWith("is")) {
      return methodName.substring(2);
    }
    if (methodName.startsWith("addAll")) {
      return methodName.substring(6);
    }
    return methodName.substring(3);
  }

  protected String captureName(String name) {
    char[] cs = name.toCharArray();
    cs[0] -= 32;
    return String.valueOf(cs);
  }

  protected MapperDefinition match(
      MapperContext ctx,
      String inputClassName,
      String outputClassName
  ) {
    var facade = ctx.getFacade();
    var project = ctx.getProject();

    var scope = GlobalSearchScope.allScope(project);
    var is = facade.findClass(inputClassName, scope);
    var os = facade.findClass(outputClassName, scope);

    for (MapperConfig cfg : ctx.getCfgs()) {
      for (MapperDefinition d : cfg.getDefinitions()) {
        if (!PsiExpressionUtils.isClass(is, d.getIntputName())) {
          continue;
        }
        if (!PsiExpressionUtils.isClass(os, d.getOutputName())) {
          continue;
        }

        if (d.getRequires() == null || d.getRequires().isEmpty()) {
          return d;
        }

        boolean r = true;
        for (MapperRequire require : d.getRequires()) {
          if (!r) {
            break;
          }
          final var name = require.getName();
          switch (require.getType()) {
            case "class" -> {
              var cs = facade.findClass(name, scope);
              if (cs == null) { // not exists
                r = false;
              }
            }
            case "method" -> {
              var cls = require.getClazz();
              if (cls.startsWith("$")) {// variable
                var vn = cls.substring(1, cls.length() - 1);
                switch (vn) {
                  case "inputName":
                    cls = inputClassName;
                    break;
                  case "outputName":
                    cls = outputClassName;
                    break;
                  default:
                    r = false;
                    break;
                }

                if (!r) {
                  break;
                }
              }
              var cs = facade.findClass(cls, scope);
              if (cs == null) {
                r = false;
                break;
              }
              var exists = Arrays.stream(cs.getAllMethods()).anyMatch(it -> it.getName().equals(name));
              if (!exists) {
                r = false;
              }
            }
            case "field" -> {
              var cls = require.getClazz();
              if (cls.startsWith("$")) {// variable
                var vn = cls.substring(1, cls.length() - 1);
                switch (vn) {
                  case "inputName":
                    cls = inputClassName;
                    break;
                  case "outputName":
                    cls = outputClassName;
                    break;
                  default:
                    r = false;
                    break;
                }

                if (!r) {
                  break;
                }
              }
              var cs = facade.findClass(cls, scope);
              if (cs == null) {
                r = false;
                break;
              }
              var exists = Arrays.stream(cs.getAllFields()).anyMatch(it -> it.getName().equals(name));
              if (!exists) {
                r = false;
              }
            }
            default -> r = false;
          }
        }

        if (!r) {
          continue;
        }

        return d;
      }
    }
    return null;
  }
}
