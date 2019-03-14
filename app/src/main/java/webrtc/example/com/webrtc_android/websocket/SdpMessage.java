package webrtc.example.com.webrtc_android.websocket;

import java.io.Serializable;

/**
 * @Auther: liuqi
 * @Date: 2019/1/28 09:26
 * @Description: sdp消息
 */
public class SdpMessage implements Serializable {
    //类型 0标识请求视频链接
    private String type;
    //sdp
    private String sdp;

    private String candidate;
    private String sdpMid;
    private String sdpMLineIndex;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSdp() {
        return sdp;
    }

    public void setSdp(String sdp) {
        this.sdp = sdp;
    }

    public String getCandidate() {
        return candidate;
    }

    public void setCandidate(String candidate) {
        this.candidate = candidate;
    }

    public String getSdpMid() {
        return sdpMid;
    }

    public void setSdpMid(String sdpMid) {
        this.sdpMid = sdpMid;
    }

    public String getSdpMLineIndex() {
        return sdpMLineIndex;
    }

    public void setSdpMLineIndex(String sdpMLineIndex) {
        this.sdpMLineIndex = sdpMLineIndex;
    }
}
