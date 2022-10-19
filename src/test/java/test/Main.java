package test;

import java.util.List;
import java.util.stream.Collectors;

public class Main {

  public static void main(String[] args) {
    var demos = List.of(new Demo());
    var da = demos.toArray(Demo[]::new);

    List.of("123").stream().collect(Collectors.joining());

  }

  private static DemoDTO map(Demo obj) {
    if (obj == null) {
      return null;
    }
    var d = new DemoDTO();
    d.setId(obj.getId());
    d.setName(obj.getName());
    d.setCate(obj.getCate() == null ? null : obj.getCate().name());
    d.setVersion(obj.getVersion());
    return d;
  }

  private static Demo map(DemoDTO obj) {
    if (obj == null) {
      return null;
    }
    var d = new Demo();
    d.setId(obj.getId());
    d.setName(obj.getName());
    d.setCate(obj.getCate() == null ? null : Enum.valueOf(CateEnum.class, obj.getCate()));
    return d;
  }

  private static Demo map(DemoRecord obj) {
    if (obj == null) {
      return null;
    }
    var d = new Demo();
    d.setId(obj.id());
    d.setName(obj.name());
    d.setCate(obj.cate() == null ? null : Enum.valueOf(CateEnum.class, obj.cate()));
//    d.setVersion();
    return d;
  }

  static class Demo {

    private Integer id;
    private String name;

    private CateEnum cate;

    private Integer version;

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

    public CateEnum getCate() {
      return cate;
    }

    public void setCate(CateEnum cate) {
      this.cate = cate;
    }

    public Integer getVersion() {
      return version;
    }

    public void setVersion(Integer version) {
      this.version = version;
    }
  }

  static class DemoDTO {

    private Integer id;

    private String name;

    private String cate;

    private Integer version;

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

    public String getCate() {
      return cate;
    }

    public void setCate(String cate) {
      this.cate = cate;
    }

    public Integer getVersion() {
      return version;
    }

    public void setVersion(Integer version) {
      this.version = version;
    }
  }

  static record DemoRecord(Integer id, String name, String cate) {

  }


  enum CateEnum {
    A,
    B,
    C,
    D,
    ;
  }
}
