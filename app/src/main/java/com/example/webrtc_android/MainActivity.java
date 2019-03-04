package com.example.webrtc_android;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.example.webrtc_android.websocket.WebSocketUtil;

public class MainActivity extends AppCompatActivity {
    WebSocketUtil ws = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ws=WebSocketUtil.getInstance("ws://10.110.6.130:2019/chat/websocket");
    }

    public void send1(View view) {
        ws.sendMsg("111","你好111");
        Log.d("MainActivity", "呼叫111");
    }

    public void send2(View view) {
        ws.sendMsg("222","你好222");
        Log.d("MainActivity", "呼叫222");
    }
}
