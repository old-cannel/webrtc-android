package webrtc.example.com.webrtc_android.webrtc;


import org.webrtc.*;
import webrtc.example.com.webrtc_android.utils.JsonUtil;
import webrtc.example.com.webrtc_android.websocket.SdpMessage;
import webrtc.example.com.webrtc_android.websocket.WebSocketUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * webrtc 客户端
 */
public class PeerConnectUtil {
    private static PeerConnectUtil peerConnectUtil;
    private PeerConnectionFactory peerConnectionFactory;
    private PeerConnection peerConnection;
    private EglBase.Context eglBaseContext;

    private static List<PeerConnection.IceServer> iceServers;

    public static PeerConnectUtil getInstance() {
        if (peerConnectUtil == null) {
            peerConnectUtil = new PeerConnectUtil();
            peerConnectUtil.setEglBaseContext(EglBase.create().getEglBaseContext());
            // create PeerConnectionFactory
            PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
            DefaultVideoEncoderFactory defaultVideoEncoderFactory =
                    new DefaultVideoEncoderFactory(peerConnectUtil.getEglBaseContext(), true, true);
            DefaultVideoDecoderFactory defaultVideoDecoderFactory =
                    new DefaultVideoDecoderFactory(peerConnectUtil.getEglBaseContext());
            peerConnectUtil.setPeerConnectionFactory(PeerConnectionFactory.builder()
                    .setOptions(options)
                    .setVideoEncoderFactory(defaultVideoEncoderFactory)
                    .setVideoDecoderFactory(defaultVideoDecoderFactory)
                    .createPeerConnectionFactory());

            iceServers = new ArrayList<>();
            iceServers.add(PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer());
        }
        return peerConnectUtil;
    }

    /**
     * 创建peerConnect对象
     *
     * @param observer
     */
    public void createPeerConnect(PeerConnection.Observer observer) {
        List<PeerConnection.IceServer> iceServers = new ArrayList<>();
        getInstance().setPeerConnection(getInstance().getPeerConnectionFactory().createPeerConnection(iceServers, observer));
    }

    public PeerConnectionFactory getPeerConnectionFactory() {
        return peerConnectionFactory;
    }

    public void setPeerConnectionFactory(PeerConnectionFactory peerConnectionFactory) {
        this.peerConnectionFactory = peerConnectionFactory;
    }

    public PeerConnection getPeerConnection() {
        return peerConnection;
    }

    public void setPeerConnection(PeerConnection peerConnection) {
        this.peerConnection = peerConnection;
    }

    public EglBase.Context getEglBaseContext() {
        return eglBaseContext;
    }

    public void setEglBaseContext(EglBase.Context eglBaseContext) {
        this.eglBaseContext = eglBaseContext;
    }

    /**
     * 创建提供者
     */
    public void offer() {
        SdpAdapter sdpAdapter = new SdpAdapter("local offer sdp") {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                super.onCreateSuccess(sessionDescription);
                gotDescription("local set local", sessionDescription);
            }
        };
        PeerConnectUtil.getInstance().getPeerConnection().createOffer(sdpAdapter, new MediaConstraints());
    }

    /**
     * 接收到提供者信息
     * @param sdpMessage
     */
    public void receiveOffer(SdpMessage sdpMessage) {
//      设置远端会话描述
        PeerConnectUtil.getInstance().getPeerConnection().setRemoteDescription(new SdpAdapter("localSetRemote"),
                new SessionDescription(SessionDescription.Type.OFFER, sdpMessage.getSdp()));

//        创建应答者
        SdpAdapter sdpAdapter = new SdpAdapter("localAnswerSdp") {
            @Override
            public void onCreateSuccess(SessionDescription sdp) {
                super.onCreateSuccess(sdp);
                gotDescription("localSetLocal", sdp);
            }
        };
        PeerConnectUtil.getInstance().getPeerConnection().createAnswer(sdpAdapter, new MediaConstraints());

    }

    /**
     * 得到会话描述--》发送给对方
     * @param desc
     * @param sessionDescription
     */
    private void gotDescription(String desc, SessionDescription sessionDescription) {
//      会话描述放到本地
        PeerConnectUtil.getInstance().getPeerConnection().setLocalDescription(new SdpAdapter(desc), sessionDescription);

//      会话描述发送给远端
        SdpMessage sdpMessage = new SdpMessage();
        sdpMessage.setType(sessionDescription.type.name().toLowerCase());
        sdpMessage.setSdp(sessionDescription.description);
        WebSocketUtil.getInstance().sendSdpMsg(WebSocketUtil.getInstance().getToUserName(), JsonUtil.toJson(sdpMessage));
    }

    /**
     * 收到应答者处理
     * @param sdpMessage
     */
    public void receiveAnswer(SdpMessage sdpMessage) {
//      设置远端会话描述
        PeerConnectUtil.getInstance().getPeerConnection().setRemoteDescription(new SdpAdapter("localSetRemote"),
                new SessionDescription(SessionDescription.Type.ANSWER, sdpMessage.getSdp()));

        String connectedStats = PeerConnection.PeerConnectionState.CONNECTED.name().toLowerCase();
        //没有完成链接就重新发起链接
        if (!connectedStats.equals(PeerConnectUtil.getInstance().getPeerConnection().connectionState().name().toLowerCase())) {
            offer();
        }

    }

    public void setIceCandidate(SdpMessage sdpMsg, PeerConnection peerConnection) {
        if (peerConnection != null) {
            peerConnection.addIceCandidate(new IceCandidate(
                    sdpMsg.getSdpMid(), Integer.parseInt(sdpMsg.getSdpMLineIndex()), sdpMsg.getCandidate()
            ));
        }
    }

    /**
     * 拒绝
     * @param b
     */
    public void deny(boolean b){
        //        关闭连接
        if(peerConnection!=null){
            peerConnection.close();
            peerConnection=null;
        }

        //通知对方拒绝通话
        if(b){
            WebSocketUtil.getInstance().applySdp(WebSocketUtil.getInstance().getToUserName(),"deny");
        }
    }

    /**
     * 挂机
     * @param b
     */
    public void hangup(boolean b) {
//        通知对方挂机
        if(b){
            WebSocketUtil.getInstance().applySdp(WebSocketUtil.getInstance().getToUserName(),"hangup");
        }
//        关闭连接
        if(peerConnection!=null){
            peerConnection.close();
            peerConnection=null;
        }

    }

    /**
     * 接收到呼叫
     */
    public void receiveCall() {
        // TODO: 2019/3/15 显示同意、拒绝按钮
    }
}
