package com.example.demo;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BuddyBotCallApplication{
    private final OkHttpClient client = new OkHttpClient();

    public void call(String message) throws Exception {
        String token = System.getenv("BUDDYBOT_AUTH_TOKEN");
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("環境変数 BUDDYBOT_AUTH_TOKEN を設定してください。");
        }

        String jsonParam =
            "{\n" +
            "  \"chatBotName\":\"BuddyBot9f294894b74a5d54@team.b\",\n" +
            "  \"targetUserName\":\"b1@team.b\",\n" +
            "  \"language\":1,\n" +
            "  \"type\":4,\n" +
            "  \"text\":"+message+"\n" +
            "}";

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
