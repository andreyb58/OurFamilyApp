package com.example.killer.api;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.killer.LoginActivity;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

public class ApiClient {
    private static final String TAG = "ApiClient";
    private static final String DEFAULT_URL = "http://192.168.1.50:8080/api/";
    private static final String PREFS_NAME  = "server_config";
    private static final String KEY_URL     = "server_url";

    private static Retrofit retrofit      = null;
    private static String currentBaseUrl  = null;

    private static Context appContext;

    public static Retrofit getClient(Context context) {
        if (appContext == null) appContext = context.getApplicationContext();
        String baseUrl = getBaseUrl(context);
        if (retrofit == null || !baseUrl.equals(currentBaseUrl)) {
            currentBaseUrl = baseUrl;
            retrofit = buildRetrofit(context.getApplicationContext(), baseUrl);
        }
        return retrofit;
    }

    public static String getBaseUrl(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String saved = prefs.getString(KEY_URL, null);
        if (saved != null && !saved.isEmpty())
            return saved.endsWith("/") ? saved : saved + "/";
        return DEFAULT_URL;
    }

    private static Retrofit buildRetrofit(Context ctx, String baseUrl) {
        HttpLoggingInterceptor log = new HttpLoggingInterceptor();
        log.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    SharedPreferences authPrefs = ctx.getSharedPreferences("auth", Context.MODE_PRIVATE);
                    String token = authPrefs.getString("jwt_token", null);

                    Request.Builder builder = original.newBuilder()
                            .header("Accept", "application/json")
                            .header("Content-Type", "application/json");

                    if (token != null && !token.isEmpty()) {
                        builder.header("Authorization", "Bearer " + token);
                    }

                    Response response = chain.proceed(builder.build());

                    if (response.code() == 401) {
                        Log.w(TAG, "401 — токен устарел, очищаем сессию");
                        authPrefs.edit().remove("jwt_token").apply();
                        retrofit = null;
                        currentBaseUrl = null;
                        // Перенаправляем на логин из любого потока
                        if (appContext != null) {
                            Intent intent = new Intent(appContext, LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            appContext.startActivity(intent);
                        }
                    }
                    return response;
                })
                .addInterceptor(log)
                .build();

        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static void updateBaseUrl(String newUrl, Context context) {
        if (newUrl == null || newUrl.isEmpty()) return;
        String url = newUrl.trim();
        if (!url.endsWith("/")) url += "/";
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putString(KEY_URL, url).apply();
        retrofit = null;
        currentBaseUrl = null;
    }

    public static void resetClient() {
        retrofit = null;
        currentBaseUrl = null;
    }
}
