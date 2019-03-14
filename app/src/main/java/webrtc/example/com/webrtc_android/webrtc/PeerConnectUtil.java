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

    public void receiveOffer(SdpMessage sdpMessage) {
        PeerConnectUtil.getInstance().getPeerConnection().setRemoteDescription(new SdpAdapter("localSetRemote"),
                new SessionDescription(SessionDescription.Type.OFFER, sdpMessage.getSdp()));

        SdpAdapter sdpAdapter = new SdpAdapter("localAnswerSdp") {
            @Override
            public void onCreateSuccess(SessionDescription sdp) {
                super.onCreateSuccess(sdp);
                gotDescription("localSetLocal", sdp);
            }
        };
        PeerConnectUtil.getInstance().getPeerConnection().createAnswer(sdpAdapter, new MediaConstraints());

    }

    private void gotDescription(String desc, SessionDescription sessionDescription) {
        PeerConnectUtil.getInstance().getPeerConnection().setLocalDescription(new SdpAdapter(desc), sessionDescription);
        SdpMessage sdpMessage = new SdpMessage();
        sdpMessage.setType(sessionDescription.type.name().toLowerCase());
        sdpMessage.setSdp(sessionDescription.description);
        WebSocketUtil.getInstance().sendSdpMsg(WebSocketUtil.getInstance().getToUserName(), JsonUtil.toJson(sdpMessage));
    }

    public void receiveAnswer(SdpMessage sdpMessage) {
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
}
