package com.example.webrtc_android;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.alibaba.fastjson.JSONObject;
import com.example.webrtc_android.webrtc.PeerConnectUtil;
import com.example.webrtc_android.webrtc.PeerConnectionAdapter;
import com.example.webrtc_android.websocket.SdpMessage;
import com.example.webrtc_android.websocket.WebSocketUtil;

import org.webrtc.Camera1Enumerator;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;


public class MainActivity extends AppCompatActivity {
    SurfaceViewRenderer localView;
    SurfaceViewRenderer remoteView;
    MediaStream mediaStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PeerConnectionFactory.initialize(PeerConnectionFactory.InitializationOptions
                .builder(this)
                .createInitializationOptions());
        WebSocketUtil.getInstance();
//单例实例化peerConnectFactory
        PeerConnectUtil peerConnectUtil = PeerConnectUtil.getInstance();

        // create AudioSource
      /*  AudioSource audioSource = peerConnectionFactory.createAudioSource(new MediaConstraints());
        AudioTrack audioTrack = peerConnectionFactory.createAudioTrack("101", audioSource);*/


        // create videoSource
        SurfaceTextureHelper surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", peerConnectUtil.getEglBaseContext());
        // create VideoCapturer
        VideoCapturer videoCapturer = createCameraCapturer(true);
        VideoSource videoSource = peerConnectUtil.getPeerConnectionFactory().createVideoSource(videoCapturer.isScreencast());
        videoCapturer.initialize(surfaceTextureHelper, getApplicationContext(), videoSource.getCapturerObserver());
        videoCapturer.startCapture(480, 640, 30);

        // render to localView
        localView = findViewById(R.id.localView);
        localView.setMirror(true);
        localView.init(peerConnectUtil.getEglBaseContext(), null);
        // create VideoTrack
        VideoTrack videoTrack = peerConnectUtil.getPeerConnectionFactory().createVideoTrack("100", videoSource);
        // display in localView
        videoTrack.addSink(localView);


        // render to remoteView
        remoteView = findViewById(R.id.remoteView);
        remoteView.setMirror(true);
        remoteView.init(peerConnectUtil.getEglBaseContext(), null);


        mediaStream = peerConnectUtil.getPeerConnectionFactory().createLocalMediaStream("mediaStream");
        mediaStream.addTrack(videoTrack);


    }


    public void send1(View view) {
        call("111");
        Log.d("MainActivity", "呼叫111");
    }

    public void send2(View view) {
        call("222");
        Log.d("MainActivity", "呼叫222");
    }


    private VideoCapturer createCameraCapturer(boolean isFront) {
        Camera1Enumerator enumerator = new Camera1Enumerator(false);
        final String[] deviceNames = enumerator.getDeviceNames();

        // First, try to find front facing camera
        for (String deviceName : deviceNames) {
            if (isFront ? enumerator.isFrontFacing(deviceName) : enumerator.isBackFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }

    private void call(String userName) {
        WebSocketUtil.getInstance().setToUserName(userName);
        PeerConnection.Observer observer = new PeerConnectionAdapter("call") {
            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                super.onIceCandidate(iceCandidate);
                gotIceCandidate(iceCandidate);
            }

            @Override
            public void onAddStream(MediaStream mediaStream) {
                super.onAddStream(mediaStream);
                gotRemoteStream(mediaStream);
            }
        };
        PeerConnectUtil.getInstance().createPeerConnect(observer);
        PeerConnectUtil.getInstance().getPeerConnection().addStream(mediaStream);

        WebSocketUtil.getInstance().applySdp(userName, "call");
    }

    public void permit(View view) {
        Log.d("MainActivity", "同意");
        PeerConnection.Observer observer = new PeerConnectionAdapter("call") {
            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                super.onIceCandidate(iceCandidate);
                gotIceCandidate(iceCandidate);
            }

            @Override
            public void onAddStream(MediaStream mediaStream) {
                super.onAddStream(mediaStream);
                gotRemoteStream(mediaStream);
            }
        };
        PeerConnectUtil.getInstance().createPeerConnect(observer);
        PeerConnectUtil.getInstance().getPeerConnection().addStream(mediaStream);
        PeerConnectUtil.getInstance().offer();
    }

    private void gotIceCandidate(IceCandidate iceCandidate) {
        SdpMessage sdpMessage = new SdpMessage();
        sdpMessage.setType("candidate");
        sdpMessage.setSdp(iceCandidate.sdp);
        sdpMessage.setCandidate(iceCandidate.sdp);
        sdpMessage.setSdpMid(iceCandidate.sdpMid);
        sdpMessage.setSdpMLineIndex(String.valueOf(iceCandidate.sdpMLineIndex));
        WebSocketUtil.getInstance().sendSdpMsg(WebSocketUtil.getInstance().getToUserName(), JSONObject.toJSONString(sdpMessage));
    }

    private void gotRemoteStream(MediaStream mediaStream) {
        VideoTrack remoteVideoTrack = mediaStream.videoTracks.get(0);
        runOnUiThread(() -> {
            remoteVideoTrack.addSink(remoteView);
        });
    }


}
