package com.guxingke.intellij.plugin.postfix.template;

import com.guxingke.intellij.plugin.postfix.template.conf.TemplateDefinition;
import com.guxingke.intellij.plugin.util.PsiExpressionUtils;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.TextExpression;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiArrayType;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiPrimitiveType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ScriptPostfixTemplate extends BasePostfixTemplate {

  private TemplateDefinition definition;

  protected ScriptPostfixTemplate(
      @Nullable String id,
      @NotNull String name,
      @NotNull String example,
      @NotNull TemplateDefinition definition,
      @NotNull Condition<PsiElement> condition,
      @Nullable PostfixTemplateProvider provider
  ) {
    super(id, name, example, condition, provider);
    this.definition = definition;
  }

  @Override
  protected Template createTemplate(
      @NotNull TemplateManager manager,
      @NotNull PsiExpression e
  ) {
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
    var item = TemplateFactory.match(ctx, definition.getTemplates());

    var tpl = manager.createTemplate(getId(), "", item.getTemplate());
    tpl.addVariable("expr", new TextExpression(e.getText()), false);
    tpl.addVariable("exprClass", new TextExpression(ctx.getClazzName()), false);
    if (ctx.getComponentClass() != null) {
      tpl.addVariable("componentClass", new TextExpression(ctx.getComponentClassName()), false);
    }

    return tpl;
  }
}
