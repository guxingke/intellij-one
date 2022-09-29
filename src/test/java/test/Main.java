package test;

import java.util.List;

public class Main {

  public static void main(String[] args) {
    var demos = List.of(new Demo());

  }

  private static DemoDTO map(Demo obj) {
    var d = new DemoDTO();
    d.setId(obj.getId());
    d.setName(obj.getName());
    return d;
  }

  static class Demo {

    private Integer id;
    private String name;

    public Integer getId() {
      return id;
    }

    public void setId(Integer id) {
      this.id = id;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }

  static class DemoDTO {

    private Integer id;

    private String name;

    public Integer getId() {
      return id;
    }

    public void setId(Integer id) {
      this.id = id;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }
}
