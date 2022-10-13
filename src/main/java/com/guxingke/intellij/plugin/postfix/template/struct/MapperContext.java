package com.guxingke.intellij.plugin.postfix.template.struct;

import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiMethod;

public class MapperContext {

  private PsiMethod method;

  private PsiClass intput;
  private PsiClass output;

  private PsiExpression expression;
  private Project project;
  private JavaPsiFacade facade;

  public MapperContext(
      PsiMethod method,
      PsiClass intput,
      PsiClass output,
      PsiExpression expression,
      Project project,
      JavaPsiFacade facade
  ) {
    this.method = method;
    this.intput = intput;
    this.output = output;
    this.expression = expression;
    this.project = project;
    this.facade = facade;
  }

  public PsiMethod getMethod() {
    return method;
  }

  public PsiClass getIntput() {
    return intput;
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
