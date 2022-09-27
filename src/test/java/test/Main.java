package test;

import java.util.List;

public class Main {

  public static void main(String[] args) {
    var demos = List.of(new Demo());
  }

  static class Demo {
    private Integer id;

    public Integer getId() {
      return id;
    }
  }
}
