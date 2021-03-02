package com.example.main;

import com.example.main.thrift.generated.TestEnum;
import com.example.main.thrift.generated.TestRequest;
import com.example.main.thrift.generated.TestStruct;
import lombok.SneakyThrows;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TJSONProtocol;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.example.main.ThriftJsonRebuilder.*;
import static org.junit.jupiter.api.Assertions.*;

class ThriftJsonRebuilderTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    @SneakyThrows
    void jsonFormatTest() {
        List<String> jsons = generateJson();
        String reformatJson = jsonReformat(jsons.get(0), new TestRequest());
        assertEquals(jsons.get(1), reformatJson);
        TestRequest request = jsonRebuild(jsons.get(0), new TestRequest());
        System.out.println(request);
    }

    private static List<String> generateJson() throws Exception {

        TestStruct struct1 =
                new TestStruct(
                        false, (byte) 5, ByteBuffer.wrap("testBinary1".getBytes(StandardCharsets.UTF_8)), (short) 11, 21, (long) 31, (double) 41.1, "testStr1");

        TestStruct struct2 =
                new TestStruct(
                        false, (byte) 5, ByteBuffer.wrap("testBinary2".getBytes(StandardCharsets.UTF_8)), (short) 12, 22, (long) 32, (double) 42.2, "testStr2");

        TestStruct struct3 =
                new TestStruct(
                        false, (byte) 5, ByteBuffer.wrap("testBinary3".getBytes(StandardCharsets.UTF_8)), (short) 13, 23, (long) 33, (double) 43.3, "testStr3");

        Map<Integer, String> intToStrMap = new HashMap<>();
        Map<Integer, TestStruct> intToStructMap = new HashMap<>();
        Map<Integer, List<String>> intToStrListMap = new HashMap<>();
        Map<Integer, List<TestStruct>> intToStructListMap = new HashMap<>();
        Map<Integer, Map<Integer, String>> intToIntToStrMapMap = new HashMap<>();

        intToStrMap.put(111, "test1");
        intToStrMap.put(222, "test2");
        intToStrMap.put(333, "test3");

        intToStrListMap.put(111, Arrays.asList("t11", "t12", "t13"));
        intToStrListMap.put(222, Arrays.asList("t21", "t22", "t23"));
        intToStrListMap.put(333, Arrays.asList("t31", "t32", "t33"));

        intToStructListMap.put(111, Arrays.asList(struct1, struct2, struct3));
        intToStructListMap.put(222, Arrays.asList(struct1, struct2, struct3));

        intToStructMap.put(111, struct1);
        intToStructMap.put(222, struct2);
        intToStructMap.put(333, struct3);

        TestRequest req =
                new TestRequest(
                        true, (byte) 5, ByteBuffer.wrap("testBinary".getBytes(StandardCharsets.UTF_8)), (short) 1, 2, (long) 3, (double) 4.1, "testStr",
                                Arrays.asList(1, 2, 3),
                                Arrays.asList("test1", "test2", "test3"),
                                Arrays.asList(struct1, struct2, struct3),
                                new HashSet<>(Arrays.asList(1, 2, 3)),
                                new HashSet<>(Arrays.asList("test1", "test2", "test3")),
                                new HashSet<>(Arrays.asList(struct1, struct2, struct3)),
                                intToStrMap, intToStructMap, intToStrListMap, intToStructListMap, intToIntToStrMapMap,
                                TestEnum.TEST_1);

        TSerializer tSerializer = new TSerializer(new NewTSimpleJsonProtocol.Factory());
        String simpleJson = tSerializer.toString(req);
        System.out.println("SimpleJson: ");
        System.out.println(simpleJson);
        System.out.println("------------------------------------");

        System.out.println("TJson: ");
        tSerializer = new TSerializer(new TJSONProtocol.Factory());
        String tJson = tSerializer.toString(req);
        System.out.println(tJson);
        System.out.println("------------------------------------");

        return Arrays.asList(simpleJson, tJson);
    }
}