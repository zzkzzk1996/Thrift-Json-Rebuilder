package com.example.main;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.*;
import org.apache.thrift.transport.TTransport;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Stack;

public class NewTSimpleJsonProtocol extends TProtocol {
    private static final byte[] COMMA = new byte[]{44};
    private static final byte[] COLON = new byte[]{58};
    private static final byte[] LBRACE = new byte[]{123};
    private static final byte[] RBRACE = new byte[]{125};
    private static final byte[] LBRACKET = new byte[]{91};
    private static final byte[] RBRACKET = new byte[]{93};
    private static final byte[] QUOTE = new byte[]{34};
    private static final TStruct ANONYMOUS_STRUCT = new TStruct();
    private static final TField ANONYMOUS_FIELD = new TField();
    private static final TMessage EMPTY_MESSAGE = new TMessage();
    private static final TSet EMPTY_SET = new TSet();
    private static final TList EMPTY_LIST = new TList();
    private static final TMap EMPTY_MAP = new TMap();
    private static final String LIST = "list";
    private static final String SET = "set";
    private static final String MAP = "map";
    protected final NewTSimpleJsonProtocol.Context BASE_CONTEXT = new NewTSimpleJsonProtocol.Context();
    protected Stack<NewTSimpleJsonProtocol.Context> writeContextStack_ = new Stack();
    protected NewTSimpleJsonProtocol.Context writeContext_;
    private NewTSimpleJsonProtocol.JSONBaseContext context_ = new NewTSimpleJsonProtocol.JSONBaseContext();
    private byte[] tmpbuf_ = new byte[4];

    protected void pushWriteContext(NewTSimpleJsonProtocol.Context c) {
        this.writeContextStack_.push(this.writeContext_);
        this.writeContext_ = c;
    }

    protected void popWriteContext() {
        this.writeContext_ = (NewTSimpleJsonProtocol.Context)this.writeContextStack_.pop();
    }

    protected void assertContextIsNotMapKey(String invalidKeyType) throws NewTSimpleJsonProtocol.CollectionMapKeyException {
        if (this.writeContext_.isMapKey()) {
            throw new NewTSimpleJsonProtocol.CollectionMapKeyException("Cannot serialize a map with keys that are of type " + invalidKeyType);
        }
    }

    public NewTSimpleJsonProtocol(TTransport trans) {
        super(trans);
        this.writeContext_ = this.BASE_CONTEXT;
    }

    public void writeMessageBegin(TMessage message) throws TException {
        this.trans_.write(LBRACKET);
        this.pushWriteContext(new NewTSimpleJsonProtocol.ListContext());
        this.writeString(message.name);
        this.writeByte(message.type);
        this.writeI32(message.seqid);
    }

    public void writeMessageEnd() throws TException {
        this.popWriteContext();
        this.trans_.write(RBRACKET);
    }

    public void writeStructBegin(TStruct struct) throws TException {
        this.writeContext_.write();
        this.trans_.write(LBRACE);
        this.pushWriteContext(new NewTSimpleJsonProtocol.StructContext());
    }

    public void writeStructEnd() throws TException {
        this.popWriteContext();
        this.trans_.write(RBRACE);
    }

    public void writeFieldBegin(TField field) throws TException {
        this.writeString(field.name);
    }

    public void writeFieldEnd() {
    }

    public void writeFieldStop() {
    }

    public void writeMapBegin(TMap map) throws TException {
        this.assertContextIsNotMapKey("map");
        this.writeContext_.write();
        this.trans_.write(LBRACE);
        this.pushWriteContext(new NewTSimpleJsonProtocol.MapContext());
    }

    public void writeMapEnd() throws TException {
        this.popWriteContext();
        this.trans_.write(RBRACE);
    }

    public void writeListBegin(TList list) throws TException {
        this.assertContextIsNotMapKey("list");
        this.writeContext_.write();
        this.trans_.write(LBRACKET);
        this.pushWriteContext(new NewTSimpleJsonProtocol.ListContext());
    }

    public void writeListEnd() throws TException {
        this.popWriteContext();
        this.trans_.write(RBRACKET);
    }

    public void writeSetBegin(TSet set) throws TException {
        this.assertContextIsNotMapKey("set");
        this.writeContext_.write();
        this.trans_.write(LBRACKET);
        this.pushWriteContext(new NewTSimpleJsonProtocol.ListContext());
    }

    public void writeSetEnd() throws TException {
        this.popWriteContext();
        this.trans_.write(RBRACKET);
    }

    public void writeBool(boolean b) throws TException {
        this.writeByte((byte)(b ? 1 : 0));
    }

    public void writeByte(byte b) throws TException {
        this.writeI32(b);
    }

    public void writeI16(short i16) throws TException {
        this.writeI32(i16);
    }

    public void writeI32(int i32) throws TException {
        if (this.writeContext_.isMapKey()) {
            this.writeString(Integer.toString(i32));
        } else {
            this.writeContext_.write();
            this._writeStringData(Integer.toString(i32));
        }

    }

    public void _writeStringData(String s) throws TException {
        try {
            byte[] b = s.getBytes("UTF-8");
            this.trans_.write(b);
        } catch (UnsupportedEncodingException var3) {
            throw new TException("JVM DOES NOT SUPPORT UTF-8");
        }
    }

    public void writeI64(long i64) throws TException {
        if (this.writeContext_.isMapKey()) {
            this.writeString(Long.toString(i64));
        } else {
            this.writeContext_.write();
            this._writeStringData(Long.toString(i64));
        }

    }

    public void writeDouble(double dub) throws TException {
        if (this.writeContext_.isMapKey()) {
            this.writeString(Double.toString(dub));
        } else {
            this.writeContext_.write();
            this._writeStringData(Double.toString(dub));
        }

    }

    public void writeString(String str) throws TException {
        this.writeContext_.write();
        int length = str.length();
        StringBuffer escape = new StringBuffer(length + 16);
        escape.append('"');

        for(int i = 0; i < length; ++i) {
            char c = str.charAt(i);
            String hex;
            int j;
            switch(c) {
                case '\b':
                    escape.append('\\');
                    escape.append('b');
                    continue;
                case '\t':
                    escape.append('\\');
                    escape.append('t');
                    continue;
                case '\n':
                    escape.append('\\');
                    escape.append('n');
                    continue;
                case '\f':
                    escape.append('\\');
                    escape.append('f');
                    continue;
                case '\r':
                    escape.append('\\');
                    escape.append('r');
                    continue;
                case '"':
                case '\\':
                    escape.append('\\');
                    escape.append(c);
                    continue;
                default:
                    if (c >= ' ') {
                        escape.append(c);
                        continue;
                    }

                    hex = Integer.toHexString(c);
                    escape.append('\\');
                    escape.append('u');
                    j = 4;
            }

            while(j > hex.length()) {
                escape.append('0');
                --j;
            }

            escape.append(hex);
        }

        escape.append('"');
        this._writeStringData(escape.toString());
    }

    /**
     * 修改了TSimpleJson的binary方法
     * @param bin
     * @throws TException
     */
    public void writeBinary(ByteBuffer bin) throws TException {
        this.writeContext_.write(); // 勿删，删掉过后没有冒号
        this.writeJSONBase64(bin.array(), bin.position() + bin.arrayOffset(), bin.limit() - bin.position() - bin.arrayOffset());
    }

    public TMessage readMessageBegin() throws TException {
        return EMPTY_MESSAGE;
    }

    public void readMessageEnd() {
    }

    public TStruct readStructBegin() {
        return ANONYMOUS_STRUCT;
    }

    public void readStructEnd() {
    }

    public TField readFieldBegin() throws TException {
        return ANONYMOUS_FIELD;
    }

    public void readFieldEnd() {
    }

    public TMap readMapBegin() throws TException {
        return EMPTY_MAP;
    }

    public void readMapEnd() {
    }

    public TList readListBegin() throws TException {
        return EMPTY_LIST;
    }

    public void readListEnd() {
    }

    public TSet readSetBegin() throws TException {
        return EMPTY_SET;
    }

    public void readSetEnd() {
    }

    public boolean readBool() throws TException {
        return this.readByte() == 1;
    }

    public byte readByte() throws TException {
        return 0;
    }

    public short readI16() throws TException {
        return 0;
    }

    public int readI32() throws TException {
        return 0;
    }

    public long readI64() throws TException {
        return 0L;
    }

    public double readDouble() throws TException {
        return 0.0D;
    }

    public String readString() throws TException {
        return "";
    }

    public String readStringBody(int size) throws TException {
        return "";
    }

    public ByteBuffer readBinary() throws TException {
        return ByteBuffer.wrap(new byte[0]);
    }

    public static class CollectionMapKeyException extends TException {
        public CollectionMapKeyException(String message) {
            super(message);
        }
    }

    protected class MapContext extends NewTSimpleJsonProtocol.StructContext {
        protected boolean isKey = true;

        protected MapContext() {
            super();
        }

        protected void write() throws TException {
            super.write();
            this.isKey = !this.isKey;
        }

        protected boolean isMapKey() {
            return this.isKey;
        }
    }

    protected class StructContext extends NewTSimpleJsonProtocol.Context {
        protected boolean first_ = true;
        protected boolean colon_ = true;

        protected StructContext() {
            super();
        }

        protected void write() throws TException {
            if (this.first_) {
                this.first_ = false;
                this.colon_ = true;
            } else {
                NewTSimpleJsonProtocol.this.trans_.write(this.colon_ ? NewTSimpleJsonProtocol.COLON : NewTSimpleJsonProtocol.COMMA);
                this.colon_ = !this.colon_;
            }

        }
    }

    protected class ListContext extends NewTSimpleJsonProtocol.Context {
        protected boolean first_ = true;

        protected ListContext() {
            super();
        }

        protected void write() throws TException {
            if (this.first_) {
                this.first_ = false;
            } else {
                NewTSimpleJsonProtocol.this.trans_.write(NewTSimpleJsonProtocol.COMMA);
            }

        }
    }

    protected class Context {
        protected Context() {
        }

        protected void write() throws TException {
        }

        protected boolean isMapKey() {
            return false;
        }
    }

    public static class Factory implements TProtocolFactory {
        public Factory() {
        }

        public TProtocol getProtocol(TTransport trans) {
            return new NewTSimpleJsonProtocol(trans);
        }
    }

    private void writeJSONBase64(byte[] b, int offset, int length) throws TException {
        this.context_.write();
        this.trans_.write(QUOTE);
        int len = length;

        int off;
        for(off = offset; len >= 3; len -= 3) {
            ThriftJsonUtils.encode(b, off, 3, this.tmpbuf_, 0);
            this.trans_.write(this.tmpbuf_, 0, 4);
            off += 3;
        }

        if (len > 0) {
            ThriftJsonUtils.encode(b, off, len, this.tmpbuf_, 0);
            this.trans_.write(this.tmpbuf_, 0, len + 1);
        }

        this.trans_.write(QUOTE);
    }

    protected class JSONBaseContext {
        protected JSONBaseContext() {
        }

        protected void write() throws TException {
        }

        protected void read() throws TException {
        }

        protected boolean escapeNum() {
            return false;
        }
    }
}
