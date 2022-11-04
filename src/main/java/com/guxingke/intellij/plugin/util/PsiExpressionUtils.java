package com.guxingke.intellij.plugin.util;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiType;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public interface PsiExpressionUtils {

  /**
   * 判定表达式类型是否包含泛型
   */
  static boolean isTypeGeneric(@NotNull PsiExpression e) {
    var type = e.getType();
    if (!(type instanceof PsiClassType)) {
      return false;
    }

    var pc = ((PsiClassType) type).resolve();
    if (pc == null) {
      return false;
    }

    return pc.getTypeParameters().length > 0;
  }

  /**
   * 判定表达式类型是否是指定类
   */
  static boolean isClass(
      @NotNull PsiExpression e,
      @NotNull String qualifiedName
  ) {
    var type = e.getType();
    if (!(type instanceof PsiClassType)) {
      return false;
    }

    var pc = ((PsiClassType) type).resolve();
    return isClass(pc, qualifiedName);
  }

  public static boolean isClass(
      PsiClass pc,
      @NotNull String qualifiedName
  ) {
    if (pc == null) {
      return false;
    }

    // current
    if (Objects.equals(pc.getQualifiedName(), qualifiedName)) {
      return true;
    }

    for (PsiClass cc : pc.getSupers()) {
      if (Objects.equals(cc.getQualifiedName(), qualifiedName)) {
        return true;
      }
      for (PsiClass ccc : cc.getSupers()) {
        var cccc = isClass(ccc, qualifiedName);
        if (cccc) {
          return true;
        }
      }
    }
    return false;
  }

  public static boolean isImplInterface(
      PsiClass pc,
      @NotNull String qualifiedName
  ) {
    if (pc == null) {
      return false;
    }
    if (Objects.equals(pc.getQualifiedName(), qualifiedName)) {
      return true;
    }

    for (PsiClass cc : pc.getInterfaces()) {
      if (Objects.equals(cc.getQualifiedName(), qualifiedName)) {
        return true;
      }
      for (PsiClass ci  : cc.getInterfaces()) {
        var cic = isImplInterface(ci, qualifiedName);
        if (cic) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * 获取表达式类型对应的类
   */
  static PsiClass findClass(@NotNull PsiExpression e) {
    var type = e.getType();
    return findClass(type);
  }

  static PsiClass findClass(
      @NotNull PsiType type
  ) {
    if (!(type instanceof PsiClassType)) {
      return null;
    }

    var pc = ((PsiClassType) type).resolve();
    return pc;
  }

  /**
   * 获取表达式类型为泛型的具体泛型类型, 只取第一个
   */
  static PsiClass findComponentClass(@NotNull PsiExpression e) {
    if (!isTypeGeneric(e)) {
      return null;
    }
    var type = e.getType();
    if (!(type instanceof PsiClassType)) {
      return null;
    }

    var pc = ((PsiClassType) type).resolve();
    var pgc = ((PsiClassType) type).resolveGenerics();

    return ((PsiClassType) pgc.getSubstitutor().getSubstitutionMap().get(pc.getTypeParameters()[0])).resolve();
  }

  static PsiMethod findMethod(PsiElement ele) {
    if (ele == null) {
      return null;
    }
    PsiMethod m = null;

    for (; ; ) {
      var p = ele.getParent();
      if (p == null) {
        break;
      }
      if (p instanceof PsiMethod) {
        m = (PsiMethod) p;
        break;
      }
      ele = p;
    }
    return m;
  }
}
