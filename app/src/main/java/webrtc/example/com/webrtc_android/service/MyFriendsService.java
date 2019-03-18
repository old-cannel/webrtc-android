package webrtc.example.com.webrtc_android.service;

import android.content.Context;
import org.springframework.util.LinkedMultiValueMap;
import webrtc.example.com.webrtc_android.response.ResponseEnum;
import webrtc.example.com.webrtc_android.response.ResponseVo;
import webrtc.example.com.webrtc_android.utils.RestTemplateUtil;

import java.util.List;

/**
 * @Auther: liuqi
 * @Date: 2019/3/13 15:35
 * @Description:
 */
public class MyFriendsService {
    /**
     * 好友列表
     *
     * @param context
     * @return
     */
    public List<String> myFriends(Context context) {
        LinkedMultiValueMap<String, String> loginMap = new LinkedMultiValueMap<>();
        ResponseVo responseVo = RestTemplateUtil.postWithJwt(context, RestTemplateUtil.MY_FRIENDS, loginMap);
        if (responseVo.getCode() == ResponseEnum.SUCCESS.getCode()) {
            return (List<String>) responseVo.getResult();
        }
        return null;
    }
}
