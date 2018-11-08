package com.xcxcxcxcx.nio.chat.handler;


import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * @author XCXCXCXCX
 * @date 2018/11/7
 * @comments
 */
public abstract class AbstractRequestHandler {

    private Selector selector;

    private SelectionKey key;

    private volatile SocketChannel socketChannel;

    public AbstractRequestHandler(Selector selector, SelectionKey key) {
        this.selector = selector;
        this.key = key;
    }

    public abstract void handleConnect();
    public abstract void handleRead() throws IOException;
    public abstract void handleWrite() throws IOException;
    public abstract void handleAccept() throws IOException;

    public Selector getSelector() {
        return selector;
    }

    public SelectionKey getKey() {
        return key;
    }

    public SocketChannel getSocketChannel() {
        //并发情况下获取socketChannel
        //本例子不会出现并发获取的情况
        if(socketChannel == null){
            synchronized (AbstractRequestHandler.class){
                if(socketChannel == null){
                    socketChannel = (SocketChannel) key.channel();
                }
            }
        }
        return socketChannel;
    }

    public void handle() throws IOException {

        if(key.isAcceptable()){
            handleAccept();
        }

        if(key.isConnectable()){
            handleConnect();
        }

        if(key.isReadable()){
            handleRead();
        }

        if(key.isWritable()){
            handleWrite();
        }


    }


}
