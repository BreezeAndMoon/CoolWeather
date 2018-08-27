package com.example.fym.coolweather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by fym on 2018/8/21.
 */

public class HttpUtil {
    public static void sendOkHttpRequest(String url, okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(callback);
    }
}
