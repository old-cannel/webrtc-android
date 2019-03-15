package webrtc.example.com.webrtc_android;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import org.webrtc.*;
import webrtc.example.com.webrtc_android.service.MyFriendsService;
import webrtc.example.com.webrtc_android.utils.JsonUtil;
import webrtc.example.com.webrtc_android.webrtc.PeerConnectUtil;
import webrtc.example.com.webrtc_android.webrtc.PeerConnectionAdapter;
import webrtc.example.com.webrtc_android.websocket.SdpMessage;
import webrtc.example.com.webrtc_android.websocket.WebSocketUtil;

import java.util.List;

public class FriendsActivity extends AppCompatActivity {
    private static final String TAG = "FriendsActivity";
    private static MyFriendsService myFriendsService = new MyFriendsService();
    List<String> friendList;
    myFriendsAdapter myFriendsAdapter;

    SurfaceViewRenderer localView;
    SurfaceViewRenderer remoteView;
    MediaStream mediaStream;

    Button btPermit;
    Button btDeny;
    Button btHangup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        btPermit = findViewById(R.id.bt_permit);
        btDeny = findViewById(R.id.bt_deny);
        btHangup = findViewById(R.id.bt_hangup);
//用户点击同意按钮
        btPermit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                permit();
            }
        });
//        用户点击拒绝按钮
        btDeny.setOnClickListener(v -> deny(true));
//        用户点击挂机按钮
        btHangup.setOnClickListener(v -> hangup(true));

//        初始化连接对象
        PeerConnectionFactory.initialize(PeerConnectionFactory.InitializationOptions
                .builder(this)
                .createInitializationOptions());

        //单例实例化peerConnectFactory
        PeerConnectUtil peerConnectUtil = PeerConnectUtil.getInstance();

//好友列表
        final ListView listView = findViewById(R.id.lv_friends);
        AsyncTask.execute(() -> {
            friendList = myFriendsService.myFriends(getBaseContext());
            if (friendList.size() > 0) {
                runOnUiThread(() -> {
                    TextView tip = findViewById(R.id.tv_tip);
                    tip.setVisibility(View.INVISIBLE);
                    //实例化适配器
                    myFriendsAdapter = new myFriendsAdapter();
                    //设置适配器
                    listView.setAdapter(myFriendsAdapter);
                });
                //打开websocket长连接
                WebSocketUtil.init(getApplicationContext());

            }

        });

        // create AudioSource
//        AudioSource audioSource = peerConnectionFactory.createAudioSource(new MediaConstraints());
//        AudioTrack audioTrack = peerConnectionFactory.createAudioTrack("101", audioSource);


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


    private class myFriendsAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return friendList.size();
        }

        @Override
        public Object getItem(int position) {
            return friendList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            final String username = friendList.get(position);
            if (convertView == null) {
                view = View.inflate(getApplicationContext(), R.layout.item_friend, null);
            } else {
                view = convertView;
            }

            TextView it_userNmae = view.findViewById(R.id.item_tv_username);
            it_userNmae.setText(username);
            Button call = view.findViewById(R.id.item_bt_call);
            call.setOnClickListener(v -> {
                Log.d(TAG, "开始呼叫-->>" + username);
// TODO: 2019/3/15 隐藏呼叫按钮 显示挂机按钮
                call(username);
            });
            return view;
        }
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
            //            呼叫方ice数据发送
            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                super.onIceCandidate(iceCandidate);
                gotIceCandidate(iceCandidate);
            }

            //          呼叫放接收远端媒体流
            @Override
            public void onAddStream(MediaStream mediaStream) {
                super.onAddStream(mediaStream);
                gotRemoteStream(mediaStream);
            }
        };
//        创建连接对象，将本地媒体流放到连接对象
        PeerConnectUtil.getInstance().createPeerConnect(observer);
        PeerConnectUtil.getInstance().getPeerConnection().addStream(mediaStream);

        WebSocketUtil.getInstance().applySdp(userName, "call");
    }


    public void permit() {
// TODO: 2019/3/15 隐藏拒绝、同意按钮 显示挂机按钮

        Log.d("MainActivity", "同意");
        PeerConnection.Observer observer = new PeerConnectionAdapter("call") {
            //            接收方ice数据发送
            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                super.onIceCandidate(iceCandidate);
                gotIceCandidate(iceCandidate);
            }

            //          呼叫放接收远端媒体流
            @Override
            public void onAddStream(MediaStream mediaStream) {
                super.onAddStream(mediaStream);
                gotRemoteStream(mediaStream);
            }
        };
//        创建连接对象，将本地媒体流放到连接对象
        PeerConnectUtil.getInstance().createPeerConnect(observer);
        PeerConnectUtil.getInstance().getPeerConnection().addStream(mediaStream);
//        创建提供者
        PeerConnectUtil.getInstance().offer();
    }

    private void gotIceCandidate(IceCandidate iceCandidate) {
        SdpMessage sdpMessage = new SdpMessage();
        sdpMessage.setType("candidate");
        sdpMessage.setSdp(iceCandidate.sdp);
        sdpMessage.setCandidate(iceCandidate.sdp);
        sdpMessage.setSdpMid(iceCandidate.sdpMid);
        sdpMessage.setSdpMLineIndex(String.valueOf(iceCandidate.sdpMLineIndex));
        WebSocketUtil.getInstance().sendSdpMsg(WebSocketUtil.getInstance().getToUserName(), JsonUtil.toJson(sdpMessage));
    }

    /**
     * 将媒体流显示到页面远端窗口
     *
     * @param mediaStream
     */
    private void gotRemoteStream(MediaStream mediaStream) {
        VideoTrack remoteVideoTrack = mediaStream.videoTracks.get(0);
        runOnUiThread(() -> {
            remoteVideoTrack.addSink(remoteView);
        });
    }

    /**
     * 拒绝
     *
     * @param b
     */
    public void deny(boolean b) {
        PeerConnectUtil.getInstance().deny(b);
        // TODO: 2019/3/15 隐藏同意、拒绝、挂机按钮，呼叫按钮显示
    }

    /**
     * 挂机
     *
     * @param b
     */
    public void hangup(boolean b) {
        PeerConnectUtil.getInstance().hangup(b);
        // TODO: 2019/3/15  按钮挂机隐藏，呼叫显示

    }

}
