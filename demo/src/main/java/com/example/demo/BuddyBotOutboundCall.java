package com.example.demo;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.HttpUrl;
import okhttp3.Response;

public class BuddyBotOutboundCall {
    public static void main(String[] args) throws Exception {
        OkHttpClient client = new OkHttpClient();

        // 認証トークン（MiniApiServer 側が検証するなら必要）
        String token = System.getenv("BUDDYBOT_AUTH_TOKEN");
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("環境変数 BUDDYBOT_AUTH_TOKEN を設定してください。");
        }

        // 送信先（デフォルトはローカルの最小サーバ）
        String host = System.getenv().getOrDefault("MINI_API_HOST", "127.0.0.1");
        int port = Integer.parseInt(System.getenv().getOrDefault("MINI_API_PORT", "18080"));

        HttpUrl url = new HttpUrl.Builder()
                .scheme("http")
                .host(host)
                .port(port)
                .addPathSegment("buddybot")
                .addPathSegment("outbound")
                .build();

        // JSON ボディ
        String json = """
        {
          "chatBotName": "BuddyBot9f294894b74a5d54@team.b",
          "senderUserName": "b1@team.b",
          "languageIndex": 1,
          "data": "こんにちは！これはOutboundのテストです。",
          "customParams": { "keyA": "valueA", "keyB": "valueB" }
        }
        """;
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Authorization", "Bearer " + token)
                .build();

        try (Response response = client.newCall(request).execute()) {
            System.out.println("HTTP " + response.code());
            String resBody = response.body() != null ? response.body().string() : "";
            System.out.println(resBody);
        }
    }
}
