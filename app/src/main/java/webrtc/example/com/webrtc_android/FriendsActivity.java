package webrtc.example.com.webrtc_android;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import webrtc.example.com.webrtc_android.service.MyFriendsService;

import java.util.List;

public class FriendsActivity extends AppCompatActivity {
    private static final String TAG = "FriendsActivity";
    private static MyFriendsService myFriendsService = new MyFriendsService();
    List<String> friendList;
    myFriendsAdapter myFriendsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        final ListView listView = (ListView) findViewById(R.id.lv_friends);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                friendList =myFriendsService.myFriends(getBaseContext());
                if(friendList.size()>0){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView tip=findViewById(R.id.tv_tip);
                            tip.setVisibility(View.INVISIBLE);
                            //实例化适配器
                            myFriendsAdapter = new myFriendsAdapter();
                            //设置适配器
                            listView.setAdapter(myFriendsAdapter);

                        }
                    });

                }

            }
        });

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
            final String username=friendList.get(position);
            if (convertView == null) {
                view=View.inflate(getApplicationContext(),R.layout.item_friend,null);
            } else {
                view= convertView;
            }

            TextView it_userNmae=view.findViewById(R.id.item_tv_username);
            it_userNmae.setText(username);
            Button call=view.findViewById(R.id.item_bt_call);
            call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG,"开始呼叫-->>"+username);

                }
            });
            return view;
        }
    }

}
