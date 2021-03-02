package com.example.main;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;

@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class ThriftMeta {

    public short thriftFieldId;

    public String thriftFieldName;

    public Class<?> thriftFieldType;

    public String thriftFieldTypeName;

    public Type thriftFieldSubTypeFirst;

    public Type thriftFieldSubTypeSecond;

    public Integer thriftDataSize;

}