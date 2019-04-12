package com.example.demo.controller.socket;

import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author zfh
 * @version 1.0
 * @since 2019/4/3 9:00
 */
@ServerEndpoint("/chatSocket/{name}")
@Component
public class ChatSocket {
    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int onlineCount = 0;

    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。若要实现服务端与单一客户端通信的话，可以使用Map来存放，其中Key可以为用户标识
    private static CopyOnWriteArraySet<ChatSocket> webSocketSet = new CopyOnWriteArraySet<ChatSocket>();
//    private static ConcurrentHashMap<String, ChatSocket> socketMap = new ConcurrentHashMap<String, ChatSocket>();

    private static ConcurrentLinkedQueue<String> messageQueue = new ConcurrentLinkedQueue();

    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;
    private String name; // 用户名称---与单个客户端对应，客户端关闭则该用户消失

    /**
     * 连接建立成功调用的方法
     * @param session  可选的参数。session为与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("name") String name){
        this.session = session;
        session.getQueryString();
        this.name = name;
        webSocketSet.add(this);     //加入set中
        addOnlineCount();           //在线数加1
        sendEarlyMessage();
        String message = "有新聊友【" + name + "】加入！当前在线人数为" + getOnlineCount();
        messageQueue.add(message);
        System.out.println(message);
        this.sendMessage(message);
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(){
        String message = "有聊友退出聊天室，连接关闭！当前在线人数为" + getOnlineCount();
        messageQueue.add(message);
        System.out.println(message);
        this.sendMessage(message);
        webSocketSet.remove(this);  //从set中删除
        subOnlineCount();           //在线数减1
    }

    /**
     * 收到客户端消息后调用的方法
     * @param message 客户端发送过来的消息
     * @param session 可选的参数
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("来自客户端的消息:" + message);
        messageQueue.add(message);
        //群发消息
        for (ChatSocket item: webSocketSet){
            item.sendMessage(message);
        }
    }

    /**
     * 发生错误时调用
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error){
        System.out.println("发生错误");
        error.printStackTrace();
    }

    private void sendEarlyMessage() {
        if (messageQueue != null && messageQueue.size() > 0) {
            for (String str : messageQueue) {
                sendMessage(str);
            }
        }
    }

    /**
     * 这个方法与上面几个方法不一样。没有用注解，是根据自己需要添加的方法。
     * @param message
     * @throws IOException
     */
    public synchronized void sendMessage(String message) {
        try {
            this.session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            e.printStackTrace();
        } /*catch (InvocationTargetException ex) {
            //ex.printStackTrace();
            System.out.println("");
        }*/
        //this.session.getAsyncRemote().sendText(message);
    }

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        ChatSocket.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        ChatSocket.onlineCount--;
    }
}



