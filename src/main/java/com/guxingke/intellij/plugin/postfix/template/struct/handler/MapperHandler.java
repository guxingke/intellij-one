package com.guxingke.intellij.plugin.postfix.template.struct.handler;

import com.guxingke.intellij.plugin.postfix.template.struct.MapperContext;
import com.guxingke.intellij.plugin.postfix.template.struct.MapperResult;

public interface MapperHandler {

  /**
   * 转换
   */
  MapperResult handle(MapperContext ctx);

  /**
   * 是否支持
   */
  boolean match(MapperContext ctx);
}
