package com.guxingke.intellij.plugin.postfix.template;

import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiExpression;

public class ConditionContext {

  private boolean primitive;
  private boolean array;
  private boolean generic;
  private boolean normal;

  private String clazzName;
  private PsiClass clazz;

  private String componentClassName;
  private PsiClass componentClass;

  private PsiExpression expression;
  private Project project;
  private JavaPsiFacade facade;

  public ConditionContext(
      boolean primitive,
      boolean array,
      boolean generic,
      boolean normal,
      String clazzName,
      PsiClass clazz,
      String componentClassName,
      PsiClass componentClass,
      PsiExpression expression,
      Project project,
      JavaPsiFacade facade
  ) {
    this.primitive = primitive;
    this.array = array;
    this.generic = generic;
    this.normal = normal;
    this.clazzName = clazzName;
    this.clazz = clazz;
    this.componentClassName = componentClassName;
    this.componentClass = componentClass;
    this.expression = expression;
    this.project = project;
    this.facade = facade;
  }

  public boolean isPrimitive() {
    return primitive;
  }

  public boolean isArray() {
    return array;
  }

  public boolean isGeneric() {
    return generic;
  }

  public boolean isNormal() {
    return normal;
  }

  public String getClazzName() {
    return clazzName;
  }

  public PsiClass getClazz() {
    return clazz;
  }

  public String getComponentClassName() {
    return componentClassName;
  }

  public PsiClass getComponentClass() {
    return componentClass;
  }

  public PsiExpression getExpression() {
    return expression;
  }

  public Project getProject() {
    return project;
  }

  public JavaPsiFacade getFacade() {
    return facade;
  }
}
