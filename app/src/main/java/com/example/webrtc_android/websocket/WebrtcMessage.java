package com.example.webrtc_android.websocket;

import java.io.Serializable;

/**
 * @Auther: liuqi
 * @Date: 2019/1/29 10:47
 * @Description: websocket 消息
 */

public class WebrtcMessage implements Serializable {
    //  普通消息
    private String message;
    //    发消息人用户名
    private String fromUserName;
    //接收消息用户名
    private String username;

    //    是否是sdp消息
    private boolean sdpMsg;
    //    sdp消息体
    private SdpMessage sdpMessage;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFromUserName() {
        return fromUserName;
    }

    public void setFromUserName(String fromUserName) {
        this.fromUserName = fromUserName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isSdpMsg() {
        return sdpMsg;
    }

    public void setSdpMsg(boolean sdpMsg) {
        this.sdpMsg = sdpMsg;
    }

    public SdpMessage getSdpMessage() {
        return sdpMessage;
    }

    public void setSdpMessage(SdpMessage sdpMessage) {
        this.sdpMessage = sdpMessage;
    }
}
