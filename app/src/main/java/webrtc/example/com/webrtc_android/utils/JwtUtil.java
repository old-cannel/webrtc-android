package webrtc.example.com.webrtc_android.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * @Auther: liuqi
 * @Date: 2019/3/14 11:12
 * @Description: token 处理类
 */
public class JwtUtil {
    private static final String JWT_KEY = "jwtToken";
    public static final String JWT = "Authorization";

    public static void set(Context context, String result) {
        SharedPreferences sp = context.getSharedPreferences(JWT_KEY, 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(JWT, result);
        editor.commit();
    }

    public static String get(Context context) {
        SharedPreferences sp = context.getSharedPreferences(JWT_KEY, 0);
        return sp.getString(JWT, "");
    }

}
