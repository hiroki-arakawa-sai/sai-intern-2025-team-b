package com.example.demo;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MiniApiServer {

    // 受信した "data" を保存（スレッドセーフ）
    private static final List<String> DATA_STORE =
            Collections.synchronizedList(new ArrayList<>());

    // "data":"...（エスケープ含む）..." を抜き出すパターン
    private static final Pattern DATA_PATTERN =
            Pattern.compile("\"data\"\\s*:\\s*\"((?:\\\\.|[^\\\\\"])*)\"");

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("MINI_API_PORT", "18080"));
        String bind = System.getenv().getOrDefault("MINI_API_BIND", "0.0.0.0");

        HttpServer server = HttpServer.create(new InetSocketAddress(bind, port), 0);
        server.createContext("/buddybot/outbound", MiniApiServer::handleOutbound); // 受信
        server.createContext("/buddybot/data", MiniApiServer::handleData);         // 一覧取得(追加)
        server.createContext("/buddybot/last", MiniApiServer::handleLast);         // 最新取得(追加)
        server.setExecutor(null);
        server.start();
        System.out.printf("Listening on http://%s:%d/buddybot/outbound%n", bind, port);
        System.out.printf("GET all:  http://%s:%d/buddybot/data%n", bind, port);
        System.out.printf("GET last: http://%s:%d/buddybot/last%n", bind, port);
    }

    // POST /buddybot/outbound
    private static void handleOutbound(HttpExchange ex) throws IOException {
        if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
            sendJson(ex, 405, "{\"status\":-1,\"message\":\"method not allowed\"}");
            return;
        }
        if (!checkAuth(ex)) return;

        byte[] raw = ex.getRequestBody().readAllBytes();
        String body = new String(raw, StandardCharsets.UTF_8);

        // "data" を抽出して保存
        String dataValue = extractDataField(body);
        if (dataValue != null) {
            DATA_STORE.add(dataValue);
            System.out.println("Saved data: " + dataValue + " (total=" + DATA_STORE.size() + ")");
        } else {
            System.out.println("No data field found in payload.");
        }

        // ログ（任意）
        Headers h = ex.getRequestHeaders();
        System.out.println("=== INCOMING ===");
        System.out.println("Content-Type: " + h.getFirst("Content-Type"));
        System.out.println("Authorization: " + h.getFirst("Authorization"));
        System.out.println("Body:\n" + body);

        sendJson(ex, 200, "{\"status\":0,\"message\":\"ok\",\"bytes\":" + raw.length + "}");
    }

    // GET /buddybot/data  →  保存済み data の配列を返す
    private static void handleData(HttpExchange ex) throws IOException {
        if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
            sendJson(ex, 405, "{\"status\":-1,\"message\":\"method not allowed\"}");
            return;
        }
        if (!checkAuth(ex)) return;

        List<String> snapshot;
        synchronized (DATA_STORE) {
            snapshot = new ArrayList<>(DATA_STORE);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("{\"status\":0,\"count\":").append(snapshot.size()).append(",\"data\":[");
        for (int i = 0; i < snapshot.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(toJsonString(snapshot.get(i)));
        }
        sb.append("]}");
        sendJson(ex, 200, sb.toString());
    }

    // GET /buddybot/last  →  最新1件を返す
    private static void handleLast(HttpExchange ex) throws IOException {
        if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
            sendJson(ex, 405, "{\"status\":-1,\"message\":\"method not allowed\"}");
            return;
        }
        if (!checkAuth(ex)) return;

        String last = null;
        synchronized (DATA_STORE) {
            if (!DATA_STORE.isEmpty()) last = DATA_STORE.get(DATA_STORE.size() - 1);
        }
        String body = (last == null)
                ? "{\"status\":0,\"last\":null}"
                : "{\"status\":0,\"last\":" + toJsonString(last) + "}";
        sendJson(ex, 200, body);
    }

    // ===== ヘルパ =====

    private static boolean checkAuth(HttpExchange ex) throws IOException {
        String expectedToken = System.getenv("BUDDYBOT_AUTH_TOKEN");
        if (expectedToken == null || expectedToken.isBlank()) return true; // 検証しない
        String auth = ex.getRequestHeaders().getFirst("Authorization");
        String want = "Bearer " + expectedToken;
        if (!want.equals(auth)) {
            sendJson(ex, 401, "{\"status\":401,\"message\":\"invalid token\"}");
            return false;
        }
        return true;
    }

    /** JSON文字列から "data" を抽出（見つからなければ null）。必要最小限のアンエスケープ付き。 */
    private static String extractDataField(String json) {
        if (json == null) return null;
        Matcher m = DATA_PATTERN.matcher(json);
        if (!m.find()) return null;
        return jsonUnescape(m.group(1));
    }

    /** JSON 文字列のアンエスケープ（最小実装） */
    private static String jsonUnescape(String s) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < s.length();) {
            char c = s.charAt(i++);
            if (c == '\\' && i < s.length()) {
                char e = s.charAt(i++);
                switch (e) {
                    case '"':  out.append('"'); break;
                    case '\\': out.append('\\'); break;
                    case '/':  out.append('/'); break;
                    case 'b':  out.append('\b'); break;
                    case 'f':  out.append('\f'); break;
                    case 'n':  out.append('\n'); break;
                    case 'r':  out.append('\r'); break;
                    case 't':  out.append('\t'); break;
                    case 'u':
                        if (i + 3 < s.length()) {
                            int code = Integer.parseInt(s.substring(i, i + 4), 16);
                            out.append((char) code);
                            i += 4;
                        }
                        break;
                    default: out.append(e);
                }
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    /** JSONの文字列値に安全にエスケープして埋め込む */
    private static String toJsonString(String s) {
        if (s == null) return "null";
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b");  break;
                case '\f': sb.append("\\f");  break;
                case '\n': sb.append("\\n");  break;
                case '\r': sb.append("\\r");  break;
                case '\t': sb.append("\\t");  break;
                default:
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int)c));
                    else sb.append(c);
            }
        }
        sb.append('"');
        return sb.toString();
    }

    private static void sendJson(HttpExchange ex, int code, String body) throws IOException {
        byte[] out = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(code, out.length);
        ex.getResponseBody().write(out);
        ex.close();
    }
}
