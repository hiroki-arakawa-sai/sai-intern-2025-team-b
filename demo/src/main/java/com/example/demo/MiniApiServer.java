package com.example.demo;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class MiniApiServer {
    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("MINI_API_PORT", "18080"));
        String bind = System.getenv().getOrDefault("MINI_API_BIND", "0.0.0.0"); // LANから受けたいなら 0.0.0.0

        HttpServer server = HttpServer.create(new InetSocketAddress(bind, port), 0);
        server.createContext("/buddybot/outbound", MiniApiServer::handleOutbound);
        server.setExecutor(null);
        server.start();
        System.out.printf("Listening on http://%s:%d/buddybot/outbound%n", bind, port);
    }

    private static void handleOutbound(HttpExchange ex) throws IOException {
        if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
            send(ex, 405, "{\"status\":-1,\"message\":\"method not allowed\"}");
            return;
        }
        byte[] raw = ex.getRequestBody().readAllBytes();
        String body = new String(raw, StandardCharsets.UTF_8);
        System.out.println("=== INCOMING ===");
        System.out.println("Content-Type: " + ex.getRequestHeaders().getFirst("Content-Type"));
        System.out.println("Authorization: " + ex.getRequestHeaders().getFirst("Authorization"));
        System.out.println("Body:\n" + body);

        String memo = new String();
        Switch sw = new Switch();
        try {
            System.out.println(sw.getBody(body));
            memo = sw.getBody(body);
        } catch (Exception e) {
            throw new IOException("failed to handle outbound", e);
        }
        send(ex, 200, "{\"status\":0,\"message\":\"ok\",\"bytes\":" + raw.length + "}");
        sw.setIsMemo(memo=="メモ");
        
    }

    private static void send(HttpExchange ex, int code, String body) throws IOException {
        byte[] out = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(code, out.length);
        ex.getResponseBody().write(out);
        ex.close();
    }
}
