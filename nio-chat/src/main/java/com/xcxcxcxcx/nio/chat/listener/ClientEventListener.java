package com.xcxcxcxcx.nio.chat.listener;

import com.xcxcxcxcx.nio.chat.handler.ClientRequestHandler;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Set;

/**
 * @author XCXCXCXCX
 * @date 2018/11/7
 * @comments
 */
public class ClientEventListener implements Runnable {

    private Selector selector;

    public ClientEventListener(Selector selector) {
        this.selector = selector;
    }

    @Override
    public void run() {
        try {

            while (!Thread.currentThread().isInterrupted()) {

                //判断selector中是否有事件
                selector.select();

                Set<SelectionKey> selectionKeys = selector.selectedKeys();

                for(SelectionKey key : selectionKeys) {
                    //处理一个事件就删除
                    selectionKeys.remove(key);

                    ClientRequestHandler handler = new ClientRequestHandler(selector, key);

                    handler.handle();

                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
