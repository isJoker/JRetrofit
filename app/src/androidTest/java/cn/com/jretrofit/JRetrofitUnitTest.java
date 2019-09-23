package cn.com.jretrofit;


import org.junit.Test;

import cn.com.retrofit.Retrofit;
import cn.com.retrofit.http.Field;
import cn.com.retrofit.http.GET;
import cn.com.retrofit.http.POST;
import cn.com.retrofit.http.Query;
import okhttp3.Call;
import okhttp3.Response;

public class JRetrofitUnitTest {

    private final static String IP = "144.34.161.97";
    private final static String KEY = "aa205eeb45aa76c6afe3c52151b52160";
    private final static String BASE_URL = "http://apis.juhe.cn/";

    interface HOST {
        @GET("/ip/ipNew")
        Call get(@Query("ip") String ip, @Query("key") String key);

        @POST("/ip/ipNew")
        Call post(@Field("ip") String ip, @Field("key") String key);
    }


    @Test
    public void testMyRetrofit() throws Exception {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL).build();
        HOST host = retrofit.create(HOST.class);
        // Retrofit GET同步请求
        Call call = host.get(IP, KEY);
        Response response = call.execute();
        if (response.body() != null) {
            System.out.println("Retrofit GET同步请求 >>> " + response.body().string());
        }

        // Retrofit POST同步请求
        Call callPost = host.post(IP, KEY);
        Response responsePost = callPost.execute();
        if (responsePost.body() != null) {
            System.out.println("Retrofit POST同步请求 >>> " + responsePost.body().string());
        }
    }
}
