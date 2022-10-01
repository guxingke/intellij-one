//package com.guxingke.intellij.plugin.postfix.template.collection;
//
//import com.guxingke.intellij.plugin.Const;
//import com.guxingke.intellij.plugin.postfix.template.BasePostfixTemplate;
//import com.guxingke.intellij.plugin.util.PsiExpressionUtils;
//import com.intellij.codeInsight.template.Template;
//import com.intellij.codeInsight.template.TemplateManager;
//import com.intellij.codeInsight.template.impl.TextExpression;
//import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider;
//import com.intellij.openapi.util.Condition;
//import com.intellij.psi.PsiElement;
//import com.intellij.psi.PsiExpression;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//
//public class ToArrayPostfixTemplate extends BasePostfixTemplate {
//
//  public ToArrayPostfixTemplate(@Nullable PostfixTemplateProvider provider) {
//    super("toArray", "toArray", "convert to array", cond(), provider);
//  }
//
//  private static Condition<PsiElement> cond() {
//    return e -> {
//      var ee = (PsiExpression) e;
//
//      var tg = PsiExpressionUtils.isTypeGeneric(ee);
//      if (!tg) {
//        return false;
//      }
//
//      var coll = PsiExpressionUtils.isClass(ee, Const.CLS_JAVA_UTIL_COLLECTION);
//      var stream = PsiExpressionUtils.isClass(ee, Const.CLS_JAVA_UTIL_STREAM_STREAM);
//      if (!coll && !stream) {
//        return false;
//      }
//
//      return PsiExpressionUtils.findComponentClass(ee) != null;
//    };
//  }
//
//  @Override
//  protected Template createTemplate(
//      @NotNull TemplateManager manager,
//      @NotNull PsiExpression e
//  ) {
//    var cls = PsiExpressionUtils.findComponentClass(e);
//    var stream = PsiExpressionUtils.isClass(e, Const.CLS_JAVA_UTIL_STREAM_STREAM);
//    var ts = "$expr$.toArray($componentClassName$[]::new)$END$";
//    if (stream) {
//      ts = "$expr$.toArray($componentClassName$[]::new)$END$";
//    }
//
//    var tpl = manager.createTemplate(getId(), "", ts);
//    tpl.addVariable("expr", new TextExpression(e.getText()), false);
//    tpl.addVariable("componentClassName", new TextExpression(cls.getQualifiedName()), false);
//    return tpl;
//  }
//
//  @Override
//  public boolean isBuiltin() {
//    return false;
//  }
//
//  @Override
//  public boolean isEditable() {
//    return false;
//  }
//}
