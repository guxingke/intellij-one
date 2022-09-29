package com.guxingke.intellij.plugin.postfix.template;

import com.guxingke.intellij.plugin.util.PsiExpressionUtils;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateWithExpressionSelector;
import com.intellij.codeInsight.template.postfix.util.JavaPostfixTemplatesUtils;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiReferenceExpression;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * pojo mapper
 *
 * 暂时只支持单入参
 */
public class StructMapperPostfixTemplate extends PostfixTemplateWithExpressionSelector {

  public StructMapperPostfixTemplate(@Nullable PostfixTemplateProvider provider) {
    super("mapper", "mapper", "pojo mapper", JavaPostfixTemplatesUtils.selectorTopmost(cond()), provider);
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
  protected void expandForChooseExpression(
      @NotNull PsiElement expression,
      @NotNull Editor editor
  ) {
    var e = (PsiExpression) expression;

    var project = expression.getProject();
    var manager = TemplateManager.getInstance(project);
    var document = editor.getDocument();

    // current ele
    var ele = ((PsiReferenceExpression) e).getElement();
    // find current method
    PsiMethod method = PsiExpressionUtils.findMethod(ele);
    var p1 = method.getParameterList().getParameter(0);
    var prt = PsiExpressionUtils.findClass(method.getReturnType());
    var pt = PsiExpressionUtils.findClass(p1.getType());

    var ts = mapper(ele.getText(), pt, prt);

    document.deleteString(expression.getTextRange().getStartOffset(), expression.getTextRange().getEndOffset());

    var tpl = manager.createTemplate(getId(), "", ts);
    tpl.setToReformat(true);
    manager.startTemplate(editor, tpl);
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
        .collect(Collectors.toMap(it -> propertyName(it.getName()), PsiMethod::getName));

    for (PsiMethod method : output.getAllMethods()) {
      if (method.getParameterList().getParameters().length != 1) {
        continue;
      }
      var mn = method.getName();
      if (!mn.startsWith("set")) {
        continue;
      }

      var pn = propertyName(mn);
      if (igm.containsKey(pn)) {
        ss.add("d." + mn + "(%s.%s());".formatted(name, igm.get(pn)));
      } else {
        ss.add("d." + mn + "();");
      }
    }

    ss.add("return d;");
    return String.join("\n", ss);
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
