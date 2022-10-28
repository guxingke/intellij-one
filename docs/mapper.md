# Mapper

## proto mapper

### pojo -> proto request

output

```proto
message CreateMaterialInventoryCmd {
  string materialId = 1;
  string name = 2;
  string desc = 3;
  string mainPhoto = 4;

  int32 normalPrice = 5;
  int32 sellPrice = 6;
  optional int32 vipPrice = 7;
  optional int32 svipPrice = 8;

  repeated string payChannels = 9;
  int32 status = 10;
}
```

input

```java
@Data
public class MaterialInventoryCreateForm {


  @NotNull
  private String materialId;

  @NotNull
  private String name;
  @NotNull
  private String desc;
  @NotNull
  private String mainPhoto;

  @NotNull
  private Integer normalPrice;
  @NotNull
  private Integer sellPrice;
  @NotNull
  private Integer vipPrice;
  @NotNull
  private Integer svipPrice;

  @NotEmpty
  private List<String> payChannels;

  @NotNull
  private Integer status;

}
```

-> mapper 

```java

private CreateMaterialInventoryCmd map(MaterialInventoryCreateForm obj) {
    if (obj == null) {
      return null;
    }
    var b = CreateMaterialInventoryCmd.newBuilder();
    b.setMaterialId(obj.getMaterialId());
    b.setName(obj.getName());
    b.setDesc(obj.getDesc());
    b.setMainPhoto(obj.getMainPhoto());
    b.setNormalPrice(obj.getNormalPrice());
    b.setSellPrice(obj.getSellPrice());
    b.setVipPrice(obj.getVipPrice());
    b.setSvipPrice(obj.getSvipPrice());
    b.addAllPayChannels(obj.getPayChannels());
    b.setStatus(obj.getStatus());
    return b.build();
  }
```
