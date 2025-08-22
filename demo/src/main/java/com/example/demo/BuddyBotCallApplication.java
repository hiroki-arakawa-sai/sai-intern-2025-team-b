package com.example.demo;

import java.util.HashMap;

import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import java.util.Map;

public class BuddyBotCallApplication{
    private final OkHttpClient client = new OkHttpClient();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public void call(String message) throws Exception {
        String token = "3B1dajYRuNLgRsVlgkttxr8RXKVIzwyPp8YOUJ.P._8Dvt9FKdELZrvEGg29kNxdV806on9bq.Ya16gPKetpykKK5B6M5Hwhcmcfy6qJNZViWemLEQdboK4UBBl3KZ72SNWlGAZ44djkHhxWSvp8OWBkB80NP6tON8S6niAIpXQNc4h4aXxUYLMkLp_pGb0AfP5YHAp5sl32GJYQI609m07Sres88r.b2T_MtsDNrxS7tNNBQrDVzn05hn3yFsMcqyYK3tFxPjE";
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("環境変数 BUDDYBOT_AUTH_TOKEN を設定してください。");
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("chatBotName",   "BuddyBot9f294894b74a5d54@team.b");
        payload.put("targetUserName", "b2@team.b");
        payload.put("language", 1);
        payload.put("type", 4);
        payload.put("text", message);           // ← ここに本文

        String jsonParam = MAPPER.writeValueAsString(payload);
        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("parameter", jsonParam)
                .build();

        Request request = new Request.Builder()
                .url("https://ip0-254.science-arts.com/buddybot/peerchat")
                .post(body)
                .header("Authorization", "Bearer " + token)
                .build();

        try (Response response = client.newCall(request).execute()) {
            System.out.println("HTTP " + response.code() + " - " + System.currentTimeMillis());
            String resBody = response.body() != null ? response.body().string() : "";
            System.out.println(resBody);
        }
    }
}
