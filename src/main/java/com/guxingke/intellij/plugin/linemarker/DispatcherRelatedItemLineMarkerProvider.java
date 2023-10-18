package com.guxingke.intellij.plugin.linemarker;

import com.guxingke.intellij.plugin.Configs;
import com.guxingke.intellij.plugin.util.PsiExpressionUtils;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.icons.AllIcons.Gutter;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.psi.impl.source.PsiClassImpl;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * @author gxk
 * @since 2022/11/03
 */
public class DispatcherRelatedItemLineMarkerProvider extends RelatedItemLineMarkerProvider {

  private static final Logger log = Logger.getInstance(DispatcherRelatedItemLineMarkerProvider.class);
  private String handlerClassName = null;
  private String dispatcherClassName = null;


  // handler 实现方法到调用方的跳转
  // [x] 调用方到实现方法的跳转
  @Override
  protected void collectNavigationMarkers(
      @NotNull PsiElement element,
      @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result
  ) {
    if (!Configs.getConfig().getLinemarker().isEnable()) {
      return;
    }
//    if (log.isDebugEnabled()) {
//      log.debug(String.format("current cfg %s %s", handlerClassName, dispatcherClassName));
//    }
    handlerClassName = Configs.getConfig().getLinemarker().getHandler();
    dispatcherClassName = Configs.getConfig().getLinemarker().getDispatcher();

    // is handler
    forHandler(element, result);
    // end handler
    forDispatcher(element, result);

  }

  private void forDispatcher(
      @NotNull PsiElement element,
      Collection<? super RelatedItemLineMarkerInfo<?>> result
  ) {
    var pmce = element instanceof PsiMethodCallExpression;
    if (!pmce) {
      return;
    }

    var ce = (PsiMethodCallExpression) element;
    var argumentList = ce.getArgumentList();
    if (argumentList.getExpressionCount() != 2) { // 俩参数
      return;
    }
    var methodExpression = ce.getMethodExpression();

    var caller = methodExpression.getFirstChild();
    if (caller instanceof PsiExpression cpe) {
      PsiClass callerCls;
      try {
        callerCls = PsiExpressionUtils.findClass(cpe);
      } catch (Exception e) {
        return;
      }
      if (callerCls == null) {
        return;
      }
      var x = PsiExpressionUtils.isClass(callerCls, dispatcherClassName);
      if (!x) {
        return;
      }
    }

    // 调用方确定
    // 解析参数类型
    var expressions = argumentList.getExpressions();
    var at = expressions[0].getType();
    var ct = expressions[1].getType();

    PsiClass ac = null;
    PsiClass cc = null;
    try {
      if (at instanceof PsiClassType pcrt) {
        ac = pcrt.resolve();
      }
      if (ct instanceof PsiClassType pict) {
        var tc = pict.resolve();
        if (tc.getTypeParameters().length > 0) {
          var tcg = pict.resolveGenerics();
          cc = ((PsiClassType) tcg.getSubstitutor().getSubstitutionMap().get(tc.getTypeParameters()[0])).resolve();
        }
      }
    } catch (Exception e) {
      log.info("failed parse call types");
    }

    if (ac == null || cc == null) {
      return;
    }

    // find handler
    var methods = findHandlers(element.getProject(), ac, cc);
    if (methods.isEmpty()) {
      return;
    }

    var builder = NavigationGutterIconBuilder.create(Gutter.Colors)
        .setTargets(methods)
        .setTooltipText("Navigate to handler");

    result.add(builder.createLineMarkerInfo(element));
  }

  private void forHandler(
      @NotNull PsiElement element,
      @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result
  ) {
    if (element instanceof PsiMethod pm) {
      var body = pm.getBody();
      if (body == null) {
        return;
      }

      var pc = (PsiClassImpl) pm.getParent();
      if (!PsiExpressionUtils.isImplInterface(pc, handlerClassName)) {
        return;
      }

      if (pm.getParameterList().getParametersCount() != 1) {
        return;
      }

      var ora = pm.getAnnotation("java.lang.Override");
      if (ora == null) {
        return;
      }

      var oc = pm.getReturnType().getCanonicalText();
      var ic = pm.getParameterList().getParameter(0).getType().getCanonicalText();

      if (oc == null || ic == null) {
        return;
      }

      var caller = findCallers(element.getProject(), ic, oc);

      if (caller.isEmpty()) {
        return;
      }

      var builder = NavigationGutterIconBuilder.create(Gutter.Colors)
          .setTargets(caller)
          .setTooltipText("Navigate to dispatcher caller");
      var lineMarkerInfo = builder.createLineMarkerInfo(element);
      result.add(lineMarkerInfo);
    }
  }

  private List<PsiMethodCallExpression> findCallers(
      Project project,
      String input,
      String output
  ) {
    var vfs = FileTypeIndex.getFiles(JavaFileType.INSTANCE, GlobalSearchScope.projectScope(project));
    List<PsiMethodCallExpression> results = new ArrayList<>();
    for (VirtualFile vf : vfs) {
      var f = PsiManager.getInstance(project).findFile(vf);

      if (!f.getName().contains("Facade")) {
        continue;
      }

      var classes = PsiTreeUtil.getChildrenOfType(f, PsiClass.class);

      for (PsiClass cls : classes) {

        for (PsiMethod m : cls.getAllMethods()) {
          m.accept(new PsiRecursiveElementVisitor() {
            @Override
            public void visitElement(@NotNull PsiElement element) {

              if (element instanceof PsiMethodCallExpression pce) {

                var me = pce.getMethodExpression();

                var caller = me.getFirstChild();
                if (caller instanceof PsiExpression cpe) {
                  PsiClass callerCls;
                  try {
                    callerCls = PsiExpressionUtils.findClass(cpe);
                  } catch (Exception e) {
                    return;
                  }
                  if (callerCls == null) {
                    return;
                  }
                  var x = PsiExpressionUtils.isClass(callerCls, dispatcherClassName);
                  if (!x) {
                    return;
                  }
                }

                var argumentList = pce.getArgumentList();
                if (argumentList.getExpressionCount() != 2) {
                  return;
                }

                var expressions = argumentList.getExpressions();
                var it = expressions[0].getType();
                var ot = expressions[1].getType();

                PsiClass cic = null;
                PsiClass coc = null;
                try {
                  if (it instanceof PsiClassType pcrt) {
                    cic = pcrt.resolve();
                  }
                  if (ot instanceof PsiClassType pict) {
                    var tc = pict.resolve();
                    if (tc.getTypeParameters().length > 0) {
                      var tcg = pict.resolveGenerics();
                      coc = ((PsiClassType) tcg.getSubstitutor()
                          .getSubstitutionMap()
                          .get(tc.getTypeParameters()[0])).resolve();
                    }
                  }
                } catch (Exception e) {
                  log.error("failed parse call types");
                }

                if (cic == null || coc == null) {
                  return;
                }
                if (Objects.equals(cic.getQualifiedName(), input) && Objects.equals(coc.getQualifiedName(), output)) {
                  results.add(pce);
                }
              }

              super.visitElement(element);
            }
          });
        }
      }
    }

    return results;
  }

  public List<PsiMethod> findHandlers(
      Project project,
      PsiClass input,
      PsiClass output
  ) {
    var vfs = FileTypeIndex.getFiles(JavaFileType.INSTANCE, GlobalSearchScope.projectScope(project));

    List<PsiMethod> results = new ArrayList<>();
    for (VirtualFile vf : vfs) {
      var f = PsiManager.getInstance(project).findFile(vf);
      if (!f.getName().contains("Handler")) {
        continue;
      }

      var classes = PsiTreeUtil.getChildrenOfType(f, PsiClass.class);
      for (PsiClass cls : classes) {
        if (cls.isInterface()) {
          continue;
        }
        if (PsiExpressionUtils.isClass(cls, handlerClassName)) {
          if (log.isDebugEnabled()) {
            log.debug("found handler " + cls.getQualifiedName());
          }

          // match
          for (PsiMethod m : cls.getAllMethods()) {
            var pl = m.getParameterList();
            if (pl.getParametersCount() != 1) {
              continue;
            }
            if (m.getReturnType() == null) {
              continue;
            }
            var mot = m.getReturnType().getCanonicalText();
            var mit = pl.getParameter(0).getType().getCanonicalText();
            if (!Objects.equals(mot, output.getQualifiedName()) || !Objects.equals(mit, input.getQualifiedName())) {
              continue;
            }

            var v = String.format("%s %s %s", mot, m.getName(), mit);
            if (log.isDebugEnabled()) {
              log.debug("found handler method" + v);
            }
            results.add(m);
          }
        }
      }
    }
    return results;
  }
}

