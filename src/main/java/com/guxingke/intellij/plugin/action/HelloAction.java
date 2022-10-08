package com.guxingke.intellij.plugin.action;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.actions.CodeInsightAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public class HelloAction extends CodeInsightAction {

  @Override
  protected boolean isValidForFile(
      @NotNull Project project,
      @NotNull Editor editor,
      @NotNull PsiFile file
  ) {
    return false;
  }

  @Override
  protected @NotNull CodeInsightActionHandler getHandler() {
    return null;
  }

}
