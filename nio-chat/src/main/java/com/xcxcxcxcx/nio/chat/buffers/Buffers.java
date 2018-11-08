package com.xcxcxcxcx.nio.chat.buffers;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

/**
 * @author XCXCXCXCX
 * @date 2018/11/7
 * @comments
 */
public class Buffers {

    private ByteBuffer writeBuffer;

    private ByteBuffer readBuffer;

    public Buffers(int writeBufferCap, int readBufferCap) {
        this.writeBuffer = ByteBuffer.allocate(writeBufferCap);
        this.readBuffer = ByteBuffer.allocate(readBufferCap);
    }

    public ByteBuffer getWriteBuffer() {
        return writeBuffer;
    }

    public ByteBuffer getReadBuffer() {
        return readBuffer;
    }

    private static Charset utf8 = Charset.forName("UTF-8");

    public static ByteBuffer encode(String str){
        return utf8.encode(str);
    }

    public static CharBuffer decode(ByteBuffer bb){
        return utf8.decode(bb);
    }
}
