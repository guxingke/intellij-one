package com.guxingke.intellij.plugin;

import java.util.Set;

public interface Const {

  String CLS_JAVA_LANG_STRING = "java.lang.String";
  String CLS_COM_GOOGLE_PROTOBUF_GENERATEDMESSAGEV3 = "com.google.protobuf.GeneratedMessageV3";
  Set<String> PREDEFINED_VARS = Set.of("expr", "exprClass", "componentClass", "END");
}
