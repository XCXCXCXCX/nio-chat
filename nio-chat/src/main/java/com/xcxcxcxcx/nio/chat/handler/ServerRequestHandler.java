package com.xcxcxcxcx.nio.chat.handler;

import com.xcxcxcxcx.nio.chat.buffers.Buffers;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author XCXCXCXCX
 * @date 2018/11/7
 * @comments
 */
public class ServerRequestHandler extends AbstractRequestHandler{

    private static Map<SocketAddress, SocketChannel> everyoneInChat = new ConcurrentHashMap<>();

    private static Map<SocketChannel, Buffers> channelBuffersMap = new ConcurrentHashMap<>();

    private ServerSocketChannel serverSocketChannel;

    private String message;

    private static final String DEFAULT_ACCEPT_MESSAGE = " connect to server !";

    public ServerRequestHandler(Selector selector, SelectionKey key, ServerSocketChannel serverSocketChannel) {
        super(selector, key);
        this.serverSocketChannel = serverSocketChannel;
    }

    @Override
    public void handleConnect() {
        //do nothing
    }

    @Override
    public void handleRead() throws IOException {

        //获取缓冲区
        Buffers buffers = (Buffers) getKey().attachment();
        ByteBuffer readBuffer = buffers.getReadBuffer();

        //获取通道并read缓冲区内容
        try{
            int readCount = getSocketChannel().read(readBuffer);
            if(readCount == -1){
                everyoneInChat.remove(getSocketChannel().getRemoteAddress());
                getSocketChannel().close();
                return;
            }
        }catch (IOException e){
            everyoneInChat.remove(getSocketChannel().getRemoteAddress());
            getSocketChannel().close();
            throw e;
        }

        readBuffer.flip();

        CharBuffer charBuffer = Buffers.decode(readBuffer);
        String message = getSocketChannel().getRemoteAddress().toString() + " : " + charBuffer;
        System.out.println(message);

        readBuffer.rewind();

        this.message = message == null ?
                getSocketChannel().getRemoteAddress().toString() + DEFAULT_ACCEPT_MESSAGE : message;
        sendOther();

        readBuffer.clear();

        //注册感兴趣事件
        int interestSet = SelectionKey.OP_READ | SelectionKey.OP_WRITE;
        getSocketChannel().register(getSelector(), interestSet, new Buffers(256, 256));


    }



    @Override
    public void handleWrite() throws IOException {

        //doNothing

    }


    //消息发送
    private void sendOther() throws IOException {

        SocketAddress clientSender = getSocketChannel().getRemoteAddress();

        //遍历everyoneInChat
        for(Map.Entry<SocketAddress,SocketChannel> entry: everyoneInChat.entrySet()){

            //只发送给不是clientSender的
            if(!entry.getKey().equals(clientSender)){

                SocketChannel channel = entry.getValue();
                Buffers buffers = channelBuffersMap.get(channel);
                ByteBuffer writeBuffer = buffers.getWriteBuffer();
                writeBuffer.put(message.getBytes("UTF-8"));
                writeBuffer.flip();

                int len = 0;
                while(writeBuffer.hasRemaining()){
                    len = channel.write(writeBuffer);
                    /*说明底层的socket写缓冲已满*/
                    if(len == 0){
                        break;
                    }
                }

                writeBuffer.compact();

            }

        }

    }

    @Override
    public void handleAccept() throws IOException {
        //接受connect请求并配置非阻塞
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);

        //注册到chat map中
        everyoneInChat.put(socketChannel.getRemoteAddress(), socketChannel);

        //给client端channel注册感兴趣事件
        //如果不注册感兴趣事件，将不会触发任何可读可写事件
        int interestSet = SelectionKey.OP_READ;
        socketChannel.register(getSelector(), interestSet, new Buffers(256, 256));
        channelBuffersMap.put(socketChannel, (Buffers)getKey().attachment());

        System.out.println("accept connect from : " + socketChannel.getRemoteAddress());
    }
}
