package com.example.webrtc_android.websocket;

import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFrame;

import java.util.List;
import java.util.Map;

/**
 * websocket 监听器
 */
public class MyWebSocketListener extends WebSocketAdapter {
    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        Log.i("MyWebSocketListener","建立链接成功");
    }

    @Override
    public void onConnectError(WebSocket websocket, WebSocketException exception) throws Exception {
        Log.i("MyWebSocketListener","建立链接错误，原因："+exception.getMessage());
    }

    @Override
    public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
        Log.i("MyWebSocketListener","断开链接");
    }

    @Override
    public void onTextMessage(WebSocket websocket, String text) throws Exception {

        WebrtcMessage webrtcMessage=  JSONObject.parseObject(text,WebrtcMessage.class);
        if (webrtcMessage.isSdpMsg()) {
            //这个里面处理视频聊天
            WebSocketUtil.getInstance().setToUserName( webrtcMessage.getFromUserName());
            WebSocketUtil.getInstance().dealSdp(webrtcMessage.getSdpMessage());
        } else {
            //    这个里面是普通的聊天
            Log.i("MyWebSocketListener","收到消息："+text);
        }
    }
}
