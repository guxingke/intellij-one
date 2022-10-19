package com.guxingke.intellij.plugin.postfix.template.struct;

import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiMethod;

public class MapperContext {

  private PsiMethod method;

  private PsiClass input;
  private PsiClass output;

  private PsiExpression expression;
  private Project project;
  private JavaPsiFacade facade;

  public MapperContext(
      PsiMethod method,
      PsiClass input,
      PsiClass output,
      PsiExpression expression,
      Project project,
      JavaPsiFacade facade
  ) {
    this.method = method;
    this.input = input;
    this.output = output;
    this.expression = expression;
    this.project = project;
    this.facade = facade;
  }

  public PsiMethod getMethod() {
    return method;
  }

  public PsiClass getInput() {
    return input;
  }

  public PsiClass getOutput() {
    return output;
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
