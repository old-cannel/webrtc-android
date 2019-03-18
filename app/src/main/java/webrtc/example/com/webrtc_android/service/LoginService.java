package webrtc.example.com.webrtc_android.service;

import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import webrtc.example.com.webrtc_android.response.ResponseVo;
import webrtc.example.com.webrtc_android.utils.RestTemplateUtil;

/**
 * @Auther: liuqi
 * @Date: 2019/3/13 16:09
 * @Description:
 */
public class LoginService {

    public ResponseVo login(String userName, String password){
        LinkedMultiValueMap<String, String> loginMap=new LinkedMultiValueMap<>();

        loginMap.add("username",userName);
        loginMap.add("password",password);
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set(HttpHeaders.CONTENT_TYPE,"application/x-www-form-urlencoded");
        return RestTemplateUtil.post(RestTemplateUtil.LOGIN,loginMap,httpHeaders);

    }
}
