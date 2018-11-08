package com.xcxcxcxcx.nio.chat;

import com.xcxcxcxcx.nio.chat.buffers.Buffers;
import com.xcxcxcxcx.nio.chat.handler.ClientRequestHandler;
import com.xcxcxcxcx.nio.chat.listener.ClientEventListener;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Scanner;
import java.util.Set;

/**
 * @author XCXCXCXCX
 * @date 2018/11/7
 * @comments
 */
public class NioChatClient {

    public static class NioClientThread implements Runnable {

        private InetSocketAddress remoteAddress;

        public NioClientThread(InetSocketAddress remoteAddress) {
            this.remoteAddress = remoteAddress;
        }

        private SocketChannel socketChannel;

        private Selector selector;

        private void init() throws IOException {
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            selector = Selector.open();
            int interestSet = SelectionKey.OP_READ | SelectionKey.OP_WRITE;
            socketChannel.register(selector, interestSet, new Buffers(256, 256));
            socketChannel.connect(remoteAddress);
            System.out.println("connecting...");
            /*等待三次握手完成*/
            while (!socketChannel.finishConnect()) {

            }
            System.out.println("connected!");

        }

        @Override
        public void run() {

            try {
                init();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //启动channel事件监听
            initListener();

            //监听client端输入
            while (!Thread.currentThread().isInterrupted()) {
                Scanner in = new Scanner(System.in);
                String clientMsg = in.next();

                //发送到队列
                ClientRequestHandler.queue.add(clientMsg);

                //由clientHandler轮流处理

            }

        }

        private void initListener() {

            new Thread(new ClientEventListener(selector)).start();
        }
    }

    public static void main(String[] args) {
        new Thread(new NioClientThread(new InetSocketAddress("0.0.0.0", 9999)), "client-1").start();
    }

}
