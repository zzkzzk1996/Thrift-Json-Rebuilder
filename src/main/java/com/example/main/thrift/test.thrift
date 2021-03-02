namespace java com.example.main.thrift.generated

struct TestStruct {
    1: bool testBool,
    2: byte testSingleByte,
    3: binary testBytes,
    4: i16 testShort,
    5: i32 testInt,
    6: i64 testLong,
    7: double testDouble,
    8: string testString,
}

enum TestEnum {
    TEST_1 = 1,
    TEST_2 = 2,
}

struct TestRequest {
    1: bool testBool,
    2: byte testSingleByte,
    3: binary testBytes,
    4: i16 testShort,
    5: i32 testInt,
    6: i64 testLong,
    7: double testDouble,
    8: string testString,
    9: list<i32> listInt,
    10: list<string> listString,
    11: list<TestStruct> listStruct,
    12: set<i32> setInt,
    13: set<string> setString,
    14: set<TestStruct> setStruct,
    15: map<i32, string> intToStrMap,
    16: map<i32, TestStruct> intToStructMap,
    17: map<i32, list<string>> intToStrListMap,
    18: map<i32, list<TestStruct>> intToStructListMap,
    19: map<i32, map<i32, string>> intToIntToStrMapMap,
    20: TestEnum testEnum,
}

struct TestResponse {
    1: bool success,
}

service TestService {
    TestResponse testService(TestRequest testRequest),
}