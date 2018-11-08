package com.xcxcxcxcx.nio.chat;

import com.xcxcxcxcx.nio.chat.buffers.Buffers;
import com.xcxcxcxcx.nio.chat.handler.ServerRequestHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @author XCXCXCXCX
 * @date 2018/11/7
 * @comments
 */
public class NioChatServer {

    public static class NioServerThread implements Runnable{

        private InetSocketAddress hostAndPort;

        public NioServerThread(InetSocketAddress hostAndPort) {
            this.hostAndPort = hostAndPort;
        }

        private ServerSocketChannel serverSocketChannel;

        private Selector selector;

        private void init() throws IOException {
            //线程启动，开启监听

            //开启selector和channel
            serverSocketChannel = ServerSocketChannel.open();
            selector = Selector.open();

            //配置非阻塞并监听端口
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(hostAndPort, 100);

            //注册感兴趣事件
            int interestSet = SelectionKey.OP_ACCEPT;
            serverSocketChannel.register(selector, interestSet, new Buffers(256, 256));

        }

        public void run() {

            try {
                init();
            } catch (IOException e) {
                System.out.println("服务初始化失败");
            }
            System.out.println("服务初始化成功,server address: " + hostAndPort.toString());

            try {
                while (!Thread.currentThread().isInterrupted()) {

                    int n = 0;

                    //判断selector中是否有事件，获取超时时间为1s
                    n = selector.select(1000);

                    if (n == 0) {
                        Thread.currentThread().sleep(1000);
                        System.out.println("等待client连接中...");
                        continue;
                    }

                    Set<SelectionKey> selectionKeys = selector.selectedKeys();

                    Iterator iterator = selectionKeys.iterator();

                    SelectionKey key = null;
                    while(iterator.hasNext()){
                        key = (SelectionKey)iterator.next();
                        //处理一个事件就删除
                        iterator.remove();
                        ServerRequestHandler handler = new ServerRequestHandler(selector, key, serverSocketChannel);
                        try {
                            handler.handle();
                        }catch (IOException e){
                            System.out.println("Error in handling client request : " + e);
                            /*若客户端连接出现异常，从Seletcor中移除这个key*/
                            key.cancel();
                            key.channel().close();
                        }
                    }


                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        }
    }

    public static void main(String[] args) {
        new Thread(new NioServerThread(new InetSocketAddress(9999)),"server-1").start();
    }

}
