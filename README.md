ThriftJsonRebuilder
==

> Human Readable JSON To Thrift POJO「_Base Apache Thrift 0.9.2_」

##一、痛点
- 目前涉及 _Thrift POJO_ 和 _JSON_ 转化的 `Apache Thrift` 原生 `Protocol` 只有两种：`TSimpleJSONProtocol` 和 `TJSONProtocol`
  - `TSimpleJSONProtocol` 能把 _Thrift POJO_ 转换成 _Human Readable JSON_ ，但是没法把 _JSON_ 反序列化成 _Thrift POJO_
  - `TJSONProtocol` 可以在 _Thrift POJO_ 和 _JSON_ 之间相互转换，但是这个 _JSON_ 没有字段的名称信息，是以字段 _ID_ 作为名称，这个 _JSON_ 对人来说几乎是不可读的，无法提供给用户进行修改<br>

##二、解决方案
###2.1 主要解决两个问题
>1. 完成 _Thrift POJO_ 到 _Human Readable JSON_ 再把 _Human Readable JSON_ 到 _Thrift POJO_ 的转换
>2. 提供统一的解决方案能够支持泛化的 _Thrift POJO_，即：只要有 `.thrift` 文件通过 Thrift compile 生成的 `.java`，就可开箱即用 

###2.2 具体实现思路

- 基于痛点的问题，最终考虑通过把 `TSimpleJsonProtocol` 生成的 _Human Readable JSON_ 进行 `Rebuild` 处理成` TJSONProtocol` 支持的形式完成 _JSON_ 到 _Thrift POJO_ 的转换
- ![Img]()<br>
- 具体设计思路如上图，结合 _Thrift POJO_ 和 _JSON_ 中的信息，提取出字段的 **名称**、**类型**、**ID**、**Size** 等信息
    0. 使用 `Gson` 的 `JsonReader` 读取输入 _JSON_
    1. 利用 _JSON_ 中字段 _Name_ 通过递归反射的方式来获取到 _Thrift POJO Class_ 中的 **Type**、**ID** 等信息
    2. 同时通过遍历 _JSON_ 的方式来获取到 **Size** 信息
    3. 将两部分信息组合到临时的数据结构 `ThriftMeta` 中，动态生成 `TJSONProtocol` 能够处理的 _JSON_，完成反序列化

###2.3 其他处理
>由于 `TSimpleJsonProtocol` 在序列化 `binary` 类型的字段的时候是直接采用了 `toString` 方法，而 `TJsonProtocol` 在序列化 `binary` 类型的字段的时候则是使用了 `Base64` 编码，经过 `Rebuild` 处理会导致 `binary` 字段不可用的问题
- 基于上述问题，重写了 `TSimpleJsonProtocol` ，修改了写 `binary` 类型字段的方法，详情参考 `NewTSimpleJsonProtocol.java` 