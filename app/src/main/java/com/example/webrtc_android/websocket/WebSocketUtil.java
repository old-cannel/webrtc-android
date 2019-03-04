package com.example.webrtc_android.websocket;

import android.util.Log;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketFactory;

import java.io.IOException;

/**
 * websockte 工具类
 */
public class WebSocketUtil {
    private WebSocket webSocket;

    public WebSocket getWebSocket() {
        return webSocket;
    }

    public void setWebSocket(WebSocket webSocket) {
        this.webSocket = webSocket;
    }

    public static WebSocketUtil getInstance(String url) {
        WebSocketUtil webSocketUtil = new WebSocketUtil();
        webSocketUtil.setWebSocket(webSocketUtil.connect(url));
        return webSocketUtil;
    }

    private WebSocket connect(String url) {
        try {

            return webSocket = new WebSocketFactory().createSocket(url, 30) //ws地址，和设置超时时间
                    .setFrameQueueSize(5)//设置帧队列最大值为5
                    .setMissingCloseFrameAllowed(false)//设置不允许服务端关闭连接却未发送关闭帧
                    .addListener(myWsListener())//添加回调监听
                    .connectAsynchronously();//异步连接
        } catch (IOException e) {
            Log.d("WebSocketClient", "建立websocket 链接");
            return null;
        }
    }

    public void sendMsg(String username, String msg) {
        getWebSocket().sendText("{'username':'" + username + "','message': '" + msg + "'}");
    }

    public void sendSdpMsg(WebSocket webSocket, String username, String sdpMsg) {
        getWebSocket().sendText("{'username':'" + username + "','sdpMsg':true,'sdpMessage':' " + sdpMsg + "'}");
    }

    public void applySdp(WebSocket webSocket, String username, String type) {
        getWebSocket().sendText("{'username':'" + username + "','sdpMsg':true,'sdpMessage': {'type':' " + type + "'}}");
    }

    public void disconnect(WebSocket webSocket) {
        getWebSocket().disconnect();
    }

    public WebSocketAdapter myWsListener() {
        return new MyWebSocketListener();
    }


}
