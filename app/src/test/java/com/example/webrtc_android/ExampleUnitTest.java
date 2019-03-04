package com.example.webrtc_android;

import com.example.webrtc_android.websocket.WebSocketUtil;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testWs(){
        WebSocketUtil ws=new WebSocketUtil();
        ws.connect("ws://localhost:2019/chat/websocket");
    }
}