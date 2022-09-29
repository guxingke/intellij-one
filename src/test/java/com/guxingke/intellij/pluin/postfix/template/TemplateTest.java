package com.guxingke.intellij.pluin.postfix.template;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;

public class TemplateTest extends LightJavaCodeInsightFixtureTestCase {

  private String v1 = """
            package test;
            public class Main {
              public static void main(String[] args) {
                var d = new Demo();
                d<caret>
              }
            }
            static class Demo {
              private Integer id;
              
              public Integer getId() {
                return id;
              }
            }
      """;

  private String v2 = """
           package test;
           import java.util.List;
           public class Main {
           
             public static void main(String[] args) {
               var demos = List.of(new Demo());
               demos<caret>
             }
            
             static class Demo {
               private Integer id;
              
               public Integer getId() {
                 return id;
               }
             }
           }
      """;

  @Override
  protected @NotNull LightProjectDescriptor getProjectDescriptor() {
    return new ProjectDescriptor(LanguageLevel.JDK_17, true) {
      @Override
      public Sdk getSdk() {
        return JavaSdk.getInstance()
            .createJdk(System.getProperty("java.specification.version"), System.getProperty("java.home"), false);
      }
    };
  }

  public void test_tsoutv() {
    var file = myFixture.configureByText(JavaFileType.INSTANCE, v1);
    myFixture.type(".tsoutv\t");
    Assert.assertTrue(file.getText().contains("System.out.println(d);"));
  }

  public void test_toIdMap() {
    var file = myFixture.configureByText(JavaFileType.INSTANCE, v2);
    myFixture.type(".toIdMap\t");
    Assert.assertTrue(file.getText()
                          .contains("demos.stream().collect(Collectors.toMap(Demo::getId, it -> it, (l, r) -> l))"));
  }

  public void test_toMap() {
    var file = myFixture.configureByText(JavaFileType.INSTANCE, v2);
    myFixture.type(".toMap\t");
    Assert.assertTrue(file.getText()
                          .contains("demos.stream().collect(Collectors.toMap(Demo::, ))"));
  }
}
