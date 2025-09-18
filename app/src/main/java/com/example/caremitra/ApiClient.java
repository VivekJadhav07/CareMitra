package com.example.caremitra;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String BASE_URL = "https://uvxkiqrqnxgmsipkjhbe.supabase.co/rest/v1/";


    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InV2eGtpcXJxbnhnbXNpcGtqaGJlIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTYyMjc0MTYsImV4cCI6MjA3MTgwMzQxNn0.GEYSncagmsr8BkBPe8IGRSGke0llj4skHWBENnyyTJI";

    public static Retrofit getClient(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        String accessToken = prefs.getString("supabase_access_token", "");

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request newRequest = chain.request().newBuilder()
                            .addHeader("apikey", SUPABASE_API_KEY)
                            .addHeader("Authorization", "Bearer " + accessToken)
                            .build();
                    return chain.proceed(newRequest);
                })
                .addInterceptor(interceptor)
                .build();

        Gson gson = new GsonBuilder().setLenient().create();

        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();
    }
}
