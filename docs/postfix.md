# Postfix Template

## 内置变量

## expr

触发 postfix 时的表达式文本, 不为空。

e.g

```plain

var demos = List.of("");
demos.<caret>

```

->

demos

## exprClass

表达式类型对应的 Java Class Name， 不为空。

e.g

java.util.List

## componentClass

表达式类型为数组时，该值为数组的组件类型。

e.g

```plain
var demo = new String[0];

demo.<caret>
```

->

java.lang.String

表达式类型为泛型时，该值为第一个泛型参数的类名。

```plain
var demos = List.of(new String("xxx"));
demos.<caret>
```

->

java.lang.String
