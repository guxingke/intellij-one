package com.guxingke.intellij.plugin.util;

import org.junit.Assert;
import org.junit.Test;

public class VariableUtilsTest {

  @Test
  public void parseVariableNames() {
    var vals = VariableUtils.parseVariableNames("$expr$.alsdkfjla.$kesj$a.sdfalsd.eo$valk$$END$");
    Assert.assertTrue(vals.size() > 0);
  }
}