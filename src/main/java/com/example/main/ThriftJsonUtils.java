package com.example.main;

import org.apache.thrift.TBase;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TProtocolFactory;

public class ThriftJsonUtils {
    private static final String ENCODE_TABLE = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
    private static final byte[] DECODE_TABLE = new byte[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};

    ThriftJsonUtils(){}

    static final void encode(byte[] src, int srcOff, int len, byte[] dst, int dstOff) {
        dst[dstOff] = (byte)"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".charAt(src[srcOff] >> 2 & 63);
        if (len == 3) {
            dst[dstOff + 1] = (byte)"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".charAt(src[srcOff] << 4 & 48 | src[srcOff + 1] >> 4 & 15);
            dst[dstOff + 2] = (byte)"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".charAt(src[srcOff + 1] << 2 & 60 | src[srcOff + 2] >> 6 & 3);
            dst[dstOff + 3] = (byte)"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".charAt(src[srcOff + 2] & 63);
        } else if (len == 2) {
            dst[dstOff + 1] = (byte)"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".charAt(src[srcOff] << 4 & 48 | src[srcOff + 1] >> 4 & 15);
            dst[dstOff + 2] = (byte)"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".charAt(src[srcOff + 1] << 2 & 60);
        } else {
            dst[dstOff + 1] = (byte)"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".charAt(src[srcOff] << 4 & 48);
        }
    }

    static final void decode(byte[] src, int srcOff, int len, byte[] dst, int dstOff) {
        dst[dstOff] = (byte)(DECODE_TABLE[src[srcOff] & 255] << 2 | DECODE_TABLE[src[srcOff + 1] & 255] >> 4);
        if (len > 2) {
            dst[dstOff + 1] = (byte)(DECODE_TABLE[src[srcOff + 1] & 255] << 4 & 240 | DECODE_TABLE[src[srcOff + 2] & 255] >> 2);
            if (len > 3) {
                dst[dstOff + 2] = (byte)(DECODE_TABLE[src[srcOff + 2] & 255] << 6 & 192 | DECODE_TABLE[src[srcOff + 3] & 255]);
            }
        }
    }

    public static <T> T newTSimpleJsonDeserializer(String adMockTSimpleJson, TBase serializedObj) throws TException {
        TProtocolFactory tProtocolFactory = new NewTSimpleJsonProtocol.Factory();
        new TDeserializer(tProtocolFactory).deserialize(serializedObj, adMockTSimpleJson, "UTF-8");
        return (T) serializedObj;
    }

    public static String newTSimpleJsonSerializer(TBase thriftObj) throws TException {
        TProtocolFactory tProtocolFactory = new NewTSimpleJsonProtocol.Factory();
        return new TSerializer(tProtocolFactory).toString(thriftObj);
    }
}
