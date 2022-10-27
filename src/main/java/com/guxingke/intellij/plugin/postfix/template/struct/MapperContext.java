package com.guxingke.intellij.plugin.postfix.template.struct;

import com.guxingke.intellij.plugin.postfix.template.struct.config.MapperConfig;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiMethod;
import java.util.List;

public class MapperContext {

  private PsiMethod method;

  private PsiClass input;
  private PsiClass output;

  private PsiExpression expression;
  private Project project;
  private JavaPsiFacade facade;

  private List<MapperConfig> cfgs;

  public MapperContext(
      PsiMethod method,
      PsiClass input,
      PsiClass output,
      PsiExpression expression,
      Project project,
      JavaPsiFacade facade,
      List<MapperConfig> cfgs
  ) {
    this.method = method;
    this.input = input;
    this.output = output;
    this.expression = expression;
    this.project = project;
    this.facade = facade;
    this.cfgs = cfgs;
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

  public List<MapperConfig> getCfgs() {
    return cfgs;
  }
}
