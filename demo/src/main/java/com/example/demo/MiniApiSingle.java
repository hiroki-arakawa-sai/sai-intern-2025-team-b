package com.example.demo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.transaction.annotation.Transactional;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;


@SpringBootApplication
public class MiniApiSingle {
    private final NippouRepository repo;

    public MiniApiSingle (NippouRepository repo){this.repo=repo;}
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(MiniApiSingle.class);
        Map<String, Object> props = new HashMap<>();
        props.put("server.port", System.getenv().getOrDefault("MINI_API_PORT", "18080"));
        props.put("server.address", System.getenv().getOrDefault("MINI_API_BIND", "0.0.0.0"));
        app.setDefaultProperties(props);
        app.run(args);
        String bind = props.get("server.address").toString();
        int port = Integer.parseInt(props.get("server.port").toString());
        System.out.printf("Listening on http://%s:%d/buddybot/outbound%n", bind, port);
        System.out.printf("GET all:  http://%s:%d/buddybot/data%n", bind, port);
        System.out.printf("GET last: http://%s:%d/buddybot/last%n", bind, port);
    }

    // ========= Service =========
    @Service
    public static class DataStoreService {
        private final List<String> store = Collections.synchronizedList(new ArrayList<>());
        public void add(String data) { store.add(data); }
        public List<String> snapshot() { synchronized (store) { return new ArrayList<>(store); } }
        public String lastOrNull() { synchronized (store) { return store.isEmpty() ? null : store.get(store.size()-1); } }
    }

    // ========= Auth Filter =========
    @Component
    public static class AuthFilter extends OncePerRequestFilter {
        private final String expectedToken = System.getenv("BUDDYBOT_AUTH_TOKEN");
        private final ObjectMapper mapper = new ObjectMapper();
        @Override
        protected boolean shouldNotFilter(HttpServletRequest request) {
            return !request.getRequestURI().startsWith("/buddybot/") || expectedToken == null || expectedToken.isBlank();
        }
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                throws ServletException, IOException {
            String auth = request.getHeader("Authorization");
            String want = "Bearer " + expectedToken;
            if (!want.equals(auth)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                byte[] body = mapper.writeValueAsBytes(Map.of("status", 401, "message", "invalid token"));
                response.getOutputStream().write(body);
                return;
            }
            chain.doFilter(request, response);
        }
    }

    // ========= Controller =========
    @RestController
    @RequestMapping(path = "/buddybot", produces = MediaType.APPLICATION_JSON_VALUE)
    public static class BuddybotController {
        private static final Logger log = LoggerFactory.getLogger(BuddybotController.class);
        private final DataStoreService store;
        private final NippouRepository nippouRepository;
        private final MemoDisplayResponder memoResponder;
        private final ObjectMapper mapper = new ObjectMapper();
        // コンストラクタインジェクション
        public BuddybotController(DataStoreService store,
                            NippouRepository nippouRepository,
                            MemoDisplayResponder memoResponder) {
        this.store = store;
        this.nippouRepository = nippouRepository;
        this.memoResponder = memoResponder;   // ← 追加
    }

        // 時台→保存する場所の決定（10..16時台のみ定義、16時台は3件）
        private static List<String> placesForHour(int hour) {
            switch (hour) {
                case 10, 13: return List.of("食品売り場");
                case 11, 14: return List.of("テナント");
                case 12, 15: return List.of("駐車場");
                case 16:     return List.of("食品売り場", "テナント", "駐車場");
                default:     return List.of(); // それ以外の時台は従来挙動へフォールバック
            }
        }

        @PostMapping(path = "/outbound", consumes = MediaType.APPLICATION_JSON_VALUE)
        @Transactional
        public ResponseEntity<Map<String, Object>> outbound(@RequestBody String body,
                                                            @RequestHeader Map<String, String> headers) throws Exception {
            log.info("=== INCOMING ===");
            log.info("Content-Type: {}", headers.getOrDefault("content-type", ""));
            log.info("Authorization: {}", headers.getOrDefault("authorization", ""));
            log.info("Body:{}", body);

            JsonNode root = mapper.readTree(body);
            JsonNode dataNode = root.get("data");

            Map<String, Object> res = new HashMap<>();

            if (dataNode != null && dataNode.isTextual()) {
                String memo = dataNode.asText();

                // ★ 「日報メモを表示」なら DB を読み上げて終了（保存しない）
                String reply = memoResponder.maybeHandle(memo);
                if (reply != null && !reply.isBlank()) {
                    res.put("status", 0);
                    res.put("message", "ok");
                    res.put("reply", reply);
                    return ResponseEntity.ok(res);
                }

                // 通常フロー：保存
                store.add(memo);

                ZoneId jst = ZoneId.of("Asia/Tokyo");
                boolean allowOverride = Boolean.parseBoolean(
                        System.getenv().getOrDefault("BUDDYBOT_ALLOW_TIME_OVERRIDE", "false")
                );

                ZonedDateTime nowJst;
                String nowHdr = headers.get("x-now"); // 例: 2025-08-21T11:15:00+09:00（ISO-8601）
                if (allowOverride && nowHdr != null && !nowHdr.isBlank()) {
                    try {
                        ZonedDateTime parsed = ZonedDateTime.parse(nowHdr);
                        nowJst = parsed.withZoneSameInstant(jst);
                    } catch (DateTimeParseException e) {
                        log.warn("x-now parse failed: {}", nowHdr);
                        nowJst = ZonedDateTime.now(jst);
                    }
                } else {
                    nowJst = ZonedDateTime.now(jst);
                }

                LocalDate day  = nowJst.toLocalDate();
                LocalTime time = nowJst.toLocalTime().withNano(0);
                int hour = time.getHour();

                List<String> places = placesForHour(hour); // 10..16時台ルール
                if (places.isEmpty()) {
                    places = List.of(headers.getOrDefault("x-place", "食品売り場")); // フォールバック
                }
                String name = headers.getOrDefault("x-name", "未設定");

                int saved = 0;
                for (String place : places) {
                    Nippou n = new Nippou();
                    n.setDay(day);
                    n.setTime(time);
                    n.setPlace(place);
                    n.setName(name);
                    n.setMemo(memo);
                    nippouRepository.save(n);
                    saved++;
                }

                res.put("status", 0);
                res.put("message", "ok");
                res.put("saved", true);
                res.put("savedCount", saved);
                res.put("places", places);
                res.put("effectiveNow", nowJst.toString());
                return ResponseEntity.ok(res);
            }

            // data が無い/文字列でない場合
            res.put("status", -1);
            res.put("message", "payload must include textual 'data'");
            res.put("saved", false);
            return ResponseEntity.badRequest().body(res);
        }

        @GetMapping("/data")
        public ResponseEntity<Map<String, Object>> all() {
            // System.out.println("test");
            List<String> snapshot = store.snapshot();
            return ResponseEntity.ok(Map.of("status", 0, "count", snapshot.size(), "data", snapshot));
        }

        @GetMapping("/last")
        public ResponseEntity<Map<String, Object>> last() {
            String last = store.lastOrNull();
            Map<String, Object> res = new HashMap<>();
            res.put("status", 0);
            res.put("last", last);
            return ResponseEntity.ok(res);
        }
    }

    // ========= Exception Advice =========
    // 置き換え：MethodNotAllowedAdvice 全体
    @RestControllerAdvice
    public static class MethodNotAllowedAdvice extends ResponseEntityExceptionHandler {

        @Override
        protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
                org.springframework.web.HttpRequestMethodNotSupportedException ex,
                org.springframework.http.HttpHeaders headers,
                org.springframework.http.HttpStatusCode status,
                org.springframework.web.context.request.WebRequest request) {

            Map<String, Object> body = Map.of("status", -1, "message", "method not allowed");
            return ResponseEntity.status(org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED).body(body);
        }
    }
}
