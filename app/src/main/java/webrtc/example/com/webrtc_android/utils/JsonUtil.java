package webrtc.example.com.webrtc_android.utils;


import com.google.gson.Gson;

import java.io.IOException;

/**
 * @Auther: liuqi
 * @Date: 2019/3/14 10:40
 * @Description: json工具类
 */
public class JsonUtil {
    public static Gson gson = new Gson();

    /**
     * json转对象
     *
     * @param json
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T parse(String json, Class<T> clazz) {
        return gson.fromJson(json,clazz);
    }

    /**
     * 对象转json
     *
     * @param t
     * @param <T>
     * @return
     */
    public static <T> String toJson(T t) {
        return gson.toJson(t);
    }
}
