package webrtc.example.com.webrtc_android.websocket;

import android.content.Context;
import android.util.Log;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketFactory;
import webrtc.example.com.webrtc_android.ssl.MySSLConnectionSocketFactory;
import webrtc.example.com.webrtc_android.utils.JwtUtil;
import webrtc.example.com.webrtc_android.utils.RestTemplateUtil;

import java.io.IOException;

/**
 * websockte 工具类
 * 工具类使用前需要调用init初始化
 */
public class WebSocketUtil {
    private WebSocketUtil() {

    }

    private static String URL = "wss://" + RestTemplateUtil.HOST + "/chat/websocket";
    private static WebSocketUtil webSocketUtil;
    private WebSocket webSocket;
    private String toUserName;

    /**
     * 初始化
     *
     * @param context
     */
    public static void init(Context context, MessageHandle messageHandle) {
        getInstance().setWebSocket(webSocketUtil.connect(context, URL, messageHandle));
    }



    /**
     * @return
     */
    public static WebSocketUtil getInstance() {
        if (webSocketUtil == null) {
            webSocketUtil = new WebSocketUtil();
        }
        return webSocketUtil;
    }



    private WebSocket connect(Context context, String url, MessageHandle messageHandle) {
        try {

            WebSocketFactory websocketFactory = new WebSocketFactory();
            if (MySSLConnectionSocketFactory.getInstance().getTrustrCertificates() != null) {
                websocketFactory.setSSLSocketFactory(MySSLConnectionSocketFactory.getInstance().getSslSocketFactory());
            }
            return websocketFactory.createSocket(url, 30) //ws地址，和设置超时时间
                    .setFrameQueueSize(5)//设置帧队列最大值为5
                    .setMissingCloseFrameAllowed(false)//设置不允许服务端关闭连接却未发送关闭帧
                    .addListener(myWsListener(messageHandle))//添加回调监听
                    .addProtocol(JwtUtil.get(context))
                    .connectAsynchronously();//异步连接
        } catch (IOException e) {
            Log.d("WebSocketClient", "建立websocket 链接");
            return null;
        }
    }

    public void sendMsg(String username, String msg) {
        getWebSocket().sendText("{'username':'" + username + "','message': '" + msg + "'}");
    }

    public void sendSdpMsg(String username, String sdpMsg) {
        getWebSocket().sendText("{\"username\":\"" + username + "\",\"sdpMsg\":true,\"sdpMessage\":" + sdpMsg + "}");
    }

    public void applySdp(String username, String type) {
        getWebSocket().sendText("{'username':'" + username + "','sdpMsg':true,'sdpMessage': {'type':'" + type + "'}}");
    }

    public void disconnect() {
        getWebSocket().disconnect();
    }

    public WebSocketAdapter myWsListener(MessageHandle messageHandle) {
        return new MyWebSocketListener(messageHandle);
    }

    public String getToUserName() {
        return toUserName;
    }

    public void setToUserName(String toUserName) {
        this.toUserName = toUserName;
    }

    public WebSocket getWebSocket() {
        return webSocket;
    }

    public void setWebSocket(WebSocket webSocket) {
        this.webSocket = webSocket;
    }
}
