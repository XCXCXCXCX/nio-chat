package com.xcxcxcxcx.nio.chat.handler;

import com.xcxcxcxcx.nio.chat.buffers.Buffers;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author XCXCXCXCX
 * @date 2018/11/7
 * @comments
 */
public class ClientRequestHandler extends AbstractRequestHandler {

    public static final LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();


    public ClientRequestHandler(Selector selector, SelectionKey key) {
        super(selector, key);
    }

    @Override
    public void handleConnect() {
        System.out.println("连接成功");
    }

    @Override
    public void handleRead() throws IOException {

        ByteBuffer readBuffer = ((Buffers)getKey().attachment()).getReadBuffer();
        /*从socket的读缓冲区读取到程序定义的缓冲区中*/
        getSocketChannel().read(readBuffer);
        readBuffer.flip();
        /*字节到utf8解码*/
        CharBuffer cb = Buffers.decode(readBuffer);
        /*显示接收到由服务器发送的信息*/
        System.out.println(cb);
        readBuffer.clear();

    }

    @Override
    public void handleWrite() throws IOException {

        String message = queue.poll();
        if(message == null){
            return;
        }

        ByteBuffer writeBuffer = ((Buffers)getKey().attachment()).getWriteBuffer();
        writeBuffer.put(message.getBytes());
        writeBuffer.flip();
        /*将程序定义的缓冲区中的内容写入到socket的写缓冲区中*/
        getSocketChannel().write(writeBuffer);
        writeBuffer.clear();
    }

    @Override
    public void handleAccept() throws IOException {
        //doNothing
    }
}
