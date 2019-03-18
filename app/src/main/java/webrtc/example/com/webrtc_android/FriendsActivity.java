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
import org.springframework.util.StringUtils;
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
    boolean isCall=false;

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
        btPermit.setOnClickListener(v -> permit());
//        用户点击拒绝按钮
        btDeny.setOnClickListener(v -> deny(true));
//        用户点击挂机按钮
        btHangup.setOnClickListener(v -> hangup(true));

        //初始化webrtc链接对象
        PeerConnectUtil.init(getApplicationContext());


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
                //websocket 连接服务器
                initWebSocket();
            }

        });

        // create AudioSource
        AudioSource audioSource = PeerConnectUtil.getInstance().getPeerConnectionFactory().createAudioSource(new MediaConstraints());
        AudioTrack audioTrack = PeerConnectUtil.getInstance().getPeerConnectionFactory().createAudioTrack("101", audioSource);


        // create videoSource
        SurfaceTextureHelper surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", PeerConnectUtil.getInstance().getEglBaseContext());
        // create VideoCapturer
        VideoCapturer videoCapturer = createCameraCapturer(true);
        VideoSource videoSource = PeerConnectUtil.getInstance().getPeerConnectionFactory().createVideoSource(videoCapturer.isScreencast());
        videoCapturer.initialize(surfaceTextureHelper, getApplicationContext(), videoSource.getCapturerObserver());
        videoCapturer.startCapture(480, 640, 30);

        // render to localView
        localView = findViewById(R.id.localView);
        localView.setMirror(true);
        localView.init(PeerConnectUtil.getInstance().getEglBaseContext(), null);
        // create VideoTrack
        VideoTrack videoTrack = PeerConnectUtil.getInstance().getPeerConnectionFactory().createVideoTrack("100", videoSource);
        // display in localView
        videoTrack.addSink(localView);


        // render to remoteView
        remoteView = findViewById(R.id.remoteView);
        remoteView.setMirror(true);
        remoteView.init(PeerConnectUtil.getInstance().getEglBaseContext(), null);


        mediaStream = PeerConnectUtil.getInstance().getPeerConnectionFactory().createLocalMediaStream("mediaStream");
        mediaStream.addTrack(videoTrack);
        mediaStream.addTrack(audioTrack);

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
            if(isCall){
                call.setVisibility(View.INVISIBLE);
            }else{
                call.setVisibility(View.VISIBLE);
            }
            call.setOnClickListener(v -> {
                Log.d(TAG, "开始呼叫-->>" + username);
                //隐藏呼叫，显示挂机
                isCall=true;
                notifyDataSetChanged();
                btHangup.setVisibility(View.VISIBLE);
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
//  隐藏拒绝、同意按钮 显示挂机按钮
        btDeny.setVisibility(View.INVISIBLE);
        btPermit.setVisibility(View.INVISIBLE);

        btHangup.setVisibility(View.VISIBLE);

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

        //  隐藏同意、拒绝、挂机按钮，呼叫按钮显示
        btPermit.setVisibility(View.INVISIBLE);
        btDeny.setVisibility(View.INVISIBLE);
        btHangup.setVisibility(View.INVISIBLE);
        isCall=false;
        myFriendsAdapter.notifyDataSetChanged();

        PeerConnectUtil.getInstance().deny(b);
    }

    /**
     * 挂机
     *
     * @param b
     */
    public void hangup(boolean b) {

        // 按钮挂机隐藏，呼叫显示
        btHangup.setVisibility(View.INVISIBLE);
        isCall=false;
        myFriendsAdapter.notifyDataSetChanged();


        PeerConnectUtil.getInstance().hangup(b);
    }


    private void initWebSocket() {
        //打开websocket长连接
        WebSocketUtil.init(getApplicationContext(), sdpMsg -> {
            if ("call".equals(sdpMsg.getType())) {
                runOnUiThread(() -> {
                    //  显示同意、拒绝按钮
                    btPermit.setVisibility(View.VISIBLE);
                    btDeny.setVisibility(View.VISIBLE);
                    isCall=true;
                    myFriendsAdapter.notifyDataSetChanged();
                });

            } else if ("offer".equals(sdpMsg.getType())) {
                PeerConnectUtil.getInstance().receiveOffer(sdpMsg);
            } else if ("answer".equals(sdpMsg.getType())) {
                PeerConnectUtil.getInstance().receiveAnswer(sdpMsg);
            } else if ("deny".equals(sdpMsg.getType())) {
                runOnUiThread(()->{
                    isCall=false;
                    myFriendsAdapter.notifyDataSetChanged();

                    btPermit.setVisibility(View.INVISIBLE);
                    btDeny.setVisibility(View.INVISIBLE);
                    btHangup.setVisibility(View.INVISIBLE);

                });

                PeerConnectUtil.getInstance().deny(false);

            } else if ("hangup".equals(sdpMsg.getType())) {
                runOnUiThread(() -> {
                    //  隐藏同意、拒绝、挂机按钮，显示呼叫按钮
                    btPermit.setVisibility(View.INVISIBLE);
                    btDeny.setVisibility(View.INVISIBLE);
                    btHangup.setVisibility(View.INVISIBLE);
                    isCall=false;
                    myFriendsAdapter.notifyDataSetChanged();
                });

                PeerConnectUtil.getInstance().hangup(false);

            } else if ("candidate".equals(sdpMsg.getType()) || StringUtils.hasLength(sdpMsg.getCandidate())) {
                PeerConnectUtil.getInstance().setIceCandidate(sdpMsg, PeerConnectUtil.getInstance().getPeerConnection());
            }
        });
    }
}
