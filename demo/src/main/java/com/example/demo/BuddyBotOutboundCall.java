package com.example.demo;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BuddyBotOutboundCall {
    public static void main(String[] args) throws Exception {
        OkHttpClient client = new OkHttpClient();

        // 認証トークンは環境変数から取得（推奨）
        String token = System.getenv("BUDDYBOT_AUTH_TOKEN");
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("環境変数 BUDDYBOT_AUTH_TOKEN を設定してください。");
        }

        // // エンドポイントURL（環境変数 or 直書き）
        // String url = System.getenv("BUDDYBOT_OUTBOUND_URL");
        // if (url == null || url.isBlank()) {
        //     // 例: "https://<host>/buddybot/outbound"
        //     throw new IllegalStateException("環境変数 BUDDYBOT_OUTBOUND_URL を設定してください。");
        // }

        // customParams は JSON 文字列でフォームに載せる
        String customParamsJson = "{"
                + "\"keyA\":\"valueA\","
                + "\"keyB\":\"valueB\""
                + "}";

        MultipartBody.Builder form = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("chatBotName", "BuddyBot9f294894b74a5d54@team.b")
                .addFormDataPart("senderUserName", "b1@team.b")
                .addFormDataPart("languageIndex", "1")
                .addFormDataPart("data", "こんにちは！これはOutboundのテストです。")
                .addFormDataPart("customParams", customParamsJson);

        Request request = new Request.Builder()
                .url("172.16.1.68")
                .post(form.build())
                .header("Authorization", "Bearer " + token) // Python例と同じ "bearer" 方式
                .build();

        try (Response response = client.newCall(request).execute()) {
            System.out.println("HTTP " + response.code());
            String resBody = response.body() != null ? response.body().string() : "";
            System.out.println(resBody);
        }
    }
}
