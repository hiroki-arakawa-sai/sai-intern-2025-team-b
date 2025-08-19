package com.example.demo;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BuddyBotCall {
    public static void main(String[] args) throws Exception {
        OkHttpClient client = new OkHttpClient();

        // 認証トークンは環境変数から取得（書き換え不要）
        String token = System.getenv("BUDDYBOT_AUTH_TOKEN");
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("環境変数 BUDDYBOT_AUTH_TOKEN を設定してください。");
        }

        // フォームの 'parameter' に入れるJSON文字列
        String jsonParam =
            "{\n" +
            "  \"chatBotName\":\"BuddyBot9f294894b74a5d54@team.b\",\n" +
            "  \"targetUserName\":\"b1@team.b\",\n" +
            "  \"language\":1,\n" +
            "  \"type\":4,\n" +
            "  \"text\":\"こんにちは\"\n" +
            "}";

        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("parameter", jsonParam) // ← 重要ポイント
                .build();

        Request request = new Request.Builder()
                // .url("https://ip0-254.science-arts.com/buddybot/peerchat")
                .url("http://localhost:8080/_debug/echo")
                .post(body)
                .header("Authorization", "Bearer " + token) // 仕様に合わせて調整可
                .build();

        try (Response response = client.newCall(request).execute()) {
            System.out.println("HTTP " + response.code());
            String resBody = response.body() != null ? response.body().string() : "";
            System.out.println(resBody);
        }
    }
}
