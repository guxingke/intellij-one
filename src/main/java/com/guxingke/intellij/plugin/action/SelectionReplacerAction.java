package com.guxingke.intellij.plugin.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Caret;
import org.jetbrains.annotations.NotNull;

/**
 * @author gxk
 * @since 2022/11/15
 */
public abstract class SelectionReplacerAction extends AnAction {

  @Override
  public void update(@NotNull AnActionEvent e) {
    var project = e.getData(CommonDataKeys.PROJECT);
    var editor = e.getData(CommonDataKeys.EDITOR);
    e.getPresentation().setVisible(project != null && editor != null && editor.getSelectionModel().hasSelection());
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    var project = e.getData(CommonDataKeys.PROJECT);
    var editor = e.getData(CommonDataKeys.EDITOR);
    if (project == null || editor == null || !editor.getSelectionModel().hasSelection()) {
      return;
    }

    var cm = editor.getCaretModel();
    var doc = editor.getDocument();

    for (Caret caret : cm.getAllCarets()) {
      if (!caret.hasSelection()) {
        continue;
      }
      var v = caret.getSelectedText();
      if (v == null) {
        continue;
      }
      if (!match(v)) {
        continue;
      }

      WriteCommandAction.runWriteCommandAction(project,
                                               () -> doc.replaceString(caret.getSelectionStart(),
                                                                       caret.getSelectionEnd(),
                                                                       replace(v)
                                               )
      );
    }

  }

  protected abstract String replace(String input);

  protected abstract boolean match(String input);
}
