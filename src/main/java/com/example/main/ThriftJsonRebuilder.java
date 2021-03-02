package com.example.main;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.*;
import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.springframework.util.StringUtils;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.io.*;
import java.lang.reflect.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.main.ThriftJsonType.*;

@Slf4j
public class ThriftJsonRebuilder {

    private final static Map<Class<?>, String> thriftTypeNameMap = new HashMap<>();

    private final static Set<Class<?>> simpleClass = new HashSet<>();

    static {
        thriftTypeNameMap.put(boolean.class, NAME_BOOL);
        thriftTypeNameMap.put(Boolean.class, NAME_BOOL);
        thriftTypeNameMap.put(byte.class, NAME_BYTE);
        thriftTypeNameMap.put(ByteBuffer.class, NAME_STRING);
        thriftTypeNameMap.put(short.class, NAME_I16);
        thriftTypeNameMap.put(Short.class, NAME_I16);
        thriftTypeNameMap.put(int.class, NAME_I32);
        thriftTypeNameMap.put(Integer.class, NAME_I32);
        thriftTypeNameMap.put(long.class, NAME_I64);
        thriftTypeNameMap.put(Long.class, NAME_I64);
        thriftTypeNameMap.put(double.class, NAME_DOUBLE);
        thriftTypeNameMap.put(Double.class, NAME_DOUBLE);
        thriftTypeNameMap.put(String.class, NAME_STRING);
        thriftTypeNameMap.put(Map.class, NAME_MAP);
        thriftTypeNameMap.put(List.class, NAME_LIST);
        thriftTypeNameMap.put(Set.class, NAME_SET);
        thriftTypeNameMap.put(TBase.class, NAME_STRUCT);
        thriftTypeNameMap.put(TEnum.class, NAME_ENUM);

        simpleClass.addAll(Arrays.asList(
                boolean.class, Boolean.class, byte.class, ByteBuffer.class, short.class, Short.class, int.class, Integer.class,
                long.class, Long.class, double.class, Double.class, String.class));
    }

    public static <T> T jsonRebuild(String json, T request) throws TException, IOException {
        String reformatJson = jsonReformat(json, request);
        TProtocolFactory tProtocolFactory = new TJSONProtocol.Factory(false);
        new TDeserializer(tProtocolFactory).deserialize((TBase) request, reformatJson, "UTF-8");
        return (T) request;
    }

    public static <T> String jsonReformat(String json, T request) throws IOException {
        JsonReader jsonReader = new JsonReader(new StringReader(json));
        StringWriter resultWriter = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(resultWriter);
        jsonReader.beginObject();
        jsonWriter.beginObject();
        recursiveIterStruct(jsonReader, request.getClass(), jsonWriter);
        return resultWriter.toString();
    }

    private static void recursiveIterStruct(JsonReader jsonReader, Class<?> base, JsonWriter jsonWriter) throws IOException {
        String lastTypeName = "";
        try {
            while (true) {
                JsonToken nextToken = jsonReader.peek();
                if (JsonToken.BEGIN_OBJECT.equals(nextToken)) {
                    jsonReader.beginObject();
                    jsonWriter.beginObject();
                } else if (JsonToken.END_OBJECT.equals(nextToken)) {
                    jsonReader.endObject();
                    jsonWriter.endObject();
                    break;
                } else if (JsonToken.NAME.equals(nextToken)) {
                    String name = jsonReader.nextName();
                    ThriftMeta thriftMeta = getThriftMeta(name, base);
                    jsonWriter.name(String.valueOf(thriftMeta.getThriftFieldId()));
                    jsonWriter.beginObject();
                    String typeName = getTypeName(thriftMeta.getThriftFieldType());
                    jsonWriter.name(typeName);
                    lastTypeName = getTypeName(thriftMeta.getThriftFieldType());
                    if (NAME_LIST.equals(typeName) || NAME_SET.equals(typeName)) {
                        jsonWriter.beginArray();
                        jsonWriter.value(getTypeName(thriftMeta.getThriftFieldSubTypeFirst()));
                    } else if (NAME_MAP.equals(typeName)) {
                        jsonWriter.beginArray();
                        jsonWriter.value(getTypeName(thriftMeta.getThriftFieldSubTypeFirst()));
                        jsonWriter.value(getTypeName(thriftMeta.getThriftFieldSubTypeSecond()));
                    }

                    if (isSimpleClass(thriftMeta.getThriftFieldType())) {
                        writeSimpleData(jsonWriter, jsonReader, lastTypeName);
                    } else if (NAME_LIST.equals(typeName) || NAME_SET.equals(typeName)) {
                        recursiveIterList(jsonReader,
                                jsonWriter, thriftMeta.getThriftFieldSubTypeFirst());
                    } else if (NAME_MAP.equals(typeName)) {
                        StringWriter subResultWriter = new StringWriter();
                        JsonWriter subWriter = new JsonWriter(subResultWriter);
                        Integer count =
                                recursiveIterMap(jsonReader, subWriter, thriftMeta.getThriftFieldSubTypeFirst(), thriftMeta.getThriftFieldSubTypeSecond());
                        jsonWriter.value(count);
                        jsonWriter.jsonValue(subResultWriter.toString());
                        jsonWriter.endArray();
                    } else if (NAME_ENUM.equals(typeName)) {
                        writeSimpleData(jsonWriter, jsonReader, lastTypeName);
                    } else {
                        recursiveIterStruct(jsonReader, thriftMeta.getThriftFieldType(), jsonWriter);
                    }
                    jsonWriter.endObject();
                } else if (JsonToken.STRING.equals(nextToken)) {
                    String value = jsonReader.nextString();
                    jsonWriter.value(value);
                } else if (JsonToken.NUMBER.equals(nextToken)) {
                    try {
                        Number value = jsonReader.nextLong();
                        jsonWriter.value(value);
                    } catch (NumberFormatException e) {
                        Number value = jsonReader.nextDouble();
                        jsonWriter.value(value);
                    }
                } else if (JsonToken.BOOLEAN.equals(nextToken)) {
                    boolean value = jsonReader.nextBoolean();
                    jsonWriter.value(value);
                } else if (JsonToken.NULL.equals(nextToken)) {
                    jsonReader.nextNull();
                    jsonWriter.nullValue();
                } else if (JsonToken.END_DOCUMENT.equals(nextToken)) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    private static Integer recursiveIterMap(JsonReader jsonReader, JsonWriter jsonWriter, Type keyParam, Type valueParam) throws IOException {
        String lastFieldName = "";
        Integer count = 0;
        try {
            while (true) {
                JsonToken nextToken = jsonReader.peek();
                if (JsonToken.BEGIN_OBJECT.equals(nextToken)) {
                    jsonReader.beginObject();
                    if (StringUtils.hasText(lastFieldName) && isTBaseClass((Class<?>) valueParam)) {
                        StringWriter subResultWriter = new StringWriter();
                        JsonWriter subWriter = new JsonWriter(subResultWriter);
                        subWriter.beginObject();
                        recursiveIterStruct(jsonReader, (Class<?>) valueParam, subWriter);
                        jsonWriter.jsonValue(subResultWriter.toString());
                    } else {
                        jsonWriter.beginObject();
                    }

                } else if (JsonToken.END_OBJECT.equals(nextToken)) {
                    jsonReader.endObject();
                    jsonWriter.endObject();
                    break;
                } else if (JsonToken.BEGIN_ARRAY.equals(nextToken)) {
                    jsonReader.beginArray();
                    jsonWriter.beginArray();
                    if (valueParam instanceof ParameterizedTypeImpl
                            && List.class.equals(((ParameterizedTypeImpl) valueParam).getRawType())) {
                        jsonWriter.value(getTypeName(((ParameterizedTypeImpl) valueParam).getActualTypeArguments()[0]));
                        recursiveIterList(jsonReader, jsonWriter, ((ParameterizedTypeImpl) valueParam).getActualTypeArguments()[0]);
                    }
                } else if (JsonToken.END_ARRAY.equals(nextToken)) {
                    jsonReader.endArray();
                    jsonWriter.endArray();
                } else if (JsonToken.NAME.equals(nextToken)) {
                    String name = jsonReader.nextName();
                    jsonWriter.name(name);
                    count++;
                    lastFieldName = name;
                } else if (JsonToken.STRING.equals(nextToken)) {
                    String value = jsonReader.nextString();
                    jsonWriter.value(value);
                } else if (JsonToken.NUMBER.equals(nextToken)) {
                    try {
                        Number value = jsonReader.nextLong();
                        jsonWriter.value(value);
                    } catch (NumberFormatException e) {
                        Number value = jsonReader.nextDouble();
                        jsonWriter.value(value);
                    }
                } else if (JsonToken.BOOLEAN.equals(nextToken)) {
                    boolean value = jsonReader.nextBoolean();
                    jsonWriter.value(value);
                } else if (JsonToken.NULL.equals(nextToken)) {
                    jsonReader.nextNull();
                    jsonWriter.nullValue();
                } else if (JsonToken.END_DOCUMENT.equals(nextToken)) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return count;
    }

    private static void recursiveIterList(JsonReader jsonReader, JsonWriter jsonWriter, Type param)
            throws IOException {
        Queue<Object> queue = new LinkedList<>();
        try {
            while (true) {
                JsonToken nextToken = jsonReader.peek();
                if (JsonToken.BEGIN_OBJECT.equals(nextToken)) {
                    jsonReader.beginObject();
                    StringWriter subResultWriter = new StringWriter();
                    JsonWriter subWriter = new JsonWriter(subResultWriter);
                    subWriter.beginObject();
                    recursiveIterStruct(jsonReader, (Class<?>) param, subWriter);   // TODO 目前不支持嵌套List
                    queue.add(subResultWriter);
                } else if (JsonToken.END_OBJECT.equals(nextToken)) {
                    jsonReader.endObject();
                    jsonWriter.endObject();
                    break;
                } else if (JsonToken.BEGIN_ARRAY.equals(nextToken)) {
                    jsonReader.beginArray();
                    queue.clear();
                } else if (JsonToken.END_ARRAY.equals(nextToken)) {
                    jsonReader.endArray();
                    queueOut(queue, jsonWriter);
                    jsonWriter.endArray();
                    return;
                } else if (JsonToken.STRING.equals(nextToken)) {
                    String value = jsonReader.nextString();
                    queue.add(value);
                } else if (JsonToken.NUMBER.equals(nextToken)) {
                    try {
                        Number value = jsonReader.nextLong();
                        queue.add(value);
                    } catch (NumberFormatException e) {
                        Number value = jsonReader.nextDouble();
                        queue.add(value);
                    }
                } else if (JsonToken.BOOLEAN.equals(nextToken)) {
                    boolean value = jsonReader.nextBoolean();
                    queue.add(value);
                } else if (JsonToken.NULL.equals(nextToken)) {
                    jsonReader.nextNull();
                    jsonWriter.nullValue();
                } else if (JsonToken.END_DOCUMENT.equals(nextToken)) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getTypeName(Type type) {
        if (ParameterizedTypeImpl.class.equals(type.getClass())) {
            return getParameterizedTypeName((ParameterizedTypeImpl) type);
        }
        String typeName = thriftTypeNameMap.get((Class<?>) type);
        if (typeName == null) {
            if (isTBaseClass((Class<?>) type)) {
                typeName = thriftTypeNameMap.get(TBase.class);
            } else if (isTEnumClass((Class<?>) type)) {
                typeName = thriftTypeNameMap.get(TEnum.class);
            } else {
                log.error(String.format("Can't find a thrift type with type %s", type.getTypeName()));
                throw new RuntimeException(String.format("Can't find a thrift type with type %s", type.getTypeName()));
            }
        }
        return typeName;
    }

    private static String getParameterizedTypeName(ParameterizedTypeImpl parameterizedType) {
        Class<?> rawType = (Class<?>) parameterizedType.getRawType();
        String typeName = thriftTypeNameMap.get(rawType);
        if (typeName == null && isTBaseClass(rawType)) {
            typeName = thriftTypeNameMap.get(TBase.class);
        }
        return typeName;
    }

    private static boolean isSimpleClass(Class<?> type) {
        return simpleClass.contains(type);
    }

    private static void writeSimpleData(JsonWriter jsonWriter, JsonReader jsonReader, String lastTypeName) throws IOException {
        JsonToken nextToken = jsonReader.peek();
        if (JsonToken.STRING.equals(nextToken)) {
            String value = jsonReader.nextString();
            jsonWriter.value(value);
        } else if (JsonToken.NUMBER.equals(nextToken)) {
            try {
                Number value = jsonReader.nextLong();
                jsonWriter.value(value);
            } catch (NumberFormatException e) {
                Number value = jsonReader.nextDouble();
                jsonWriter.value(value);
            }
        } else if (JsonToken.BOOLEAN.equals(nextToken)) {
            boolean value = jsonReader.nextBoolean();
            jsonWriter.value(value);
        } else if (JsonToken.NULL.equals(nextToken)) {
            jsonReader.nextNull();
            jsonWriter.nullValue();
        }
    }

    private static void queueOut(Queue<Object> queue, JsonWriter jsonWriter) throws IOException {
        jsonWriter.value(queue.size());
        while (!queue.isEmpty()) {
            Object obj = queue.poll();
            if (obj instanceof String) {
                jsonWriter.value((String) obj);
            } else if (obj instanceof Number) {
                jsonWriter.value((Number) obj);
            } else if (obj instanceof Boolean) {
                jsonWriter.value((Boolean) obj);
            } else if (obj == null) {
                jsonWriter.nullValue();
            } else if (obj instanceof StringWriter) {
                jsonWriter.jsonValue(obj.toString());
            } else {
                throw new RuntimeException(
                        String.format("Type error with object %s", obj.getClass().getTypeName()));
            }
        }
    }

    private static boolean isTBaseClass(Class<?> base) {
        if (base.getInterfaces().length == 0) {
            return false;
        }
        // always contains one element
        List<Class<?>> fieldInterfaces =
                Arrays.stream(base.getInterfaces())
                        .filter(interfaceClz -> (interfaceClz.equals(TBase.class)))
                        .collect(Collectors.toList());
        return !fieldInterfaces.isEmpty();
    }

    private static boolean isTEnumClass(Class<?> base) {
        if (base.getInterfaces().length == 0) {
            return false;
        }
        // always contains one element
        List<Class<?>> fieldInterfaces =
                Arrays.stream(base.getInterfaces())
                        .filter(interfaceClz -> (interfaceClz.equals(TEnum.class)))
                        .collect(Collectors.toList());
        return !fieldInterfaces.isEmpty();
    }

    private static ThriftMeta getThriftMeta(String fieldName, Class<?> base)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        Class<?>[] innerCLz = base.getDeclaredClasses();
        Optional<?> optionalClz = Optional.ofNullable(Arrays.stream(innerCLz).filter(Class::isEnum).findAny().get());
        Class<Enum> enumClz = (Class<Enum>) optionalClz.orElse(null);
        Method method = enumClz.getMethod("findByName", String.class);
        TFieldIdEnum tFieldIdEnum = (TFieldIdEnum) method.invoke(null, fieldName);
        Field field = base.getField(tFieldIdEnum.getFieldName());
        ThriftMeta meta = ThriftMeta.builder()
                .thriftFieldId(tFieldIdEnum.getThriftFieldId())
                .thriftFieldName(tFieldIdEnum.getFieldName())
                .thriftFieldType(field.getType())
                .thriftFieldTypeName(field.getType().getTypeName())
                .build();

        if (List.class.equals(field.getType()) || Set.class.equals(field.getType()) || Map.class.equals(field.getType())) {
            ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            if (actualTypeArguments.length > 0) {
                meta = meta.toBuilder().thriftFieldSubTypeFirst((actualTypeArguments[0])).build();
            }
            if (actualTypeArguments.length > 1) {
                meta = meta.toBuilder().thriftFieldSubTypeSecond(actualTypeArguments[1]).build();
            }
        }
        log.debug(tFieldIdEnum.getThriftFieldId() + " + " + tFieldIdEnum.getFieldName() + " + " + field.getType().getTypeName());
        return meta;
    }
}
