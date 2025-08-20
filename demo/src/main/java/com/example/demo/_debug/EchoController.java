package com.example.demo._debug;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/_debug")
public class EchoController {
    @PostMapping(value = "/echo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> echo(
            @RequestParam(value = "parameter", required = false) String parameter,
            HttpServletRequest req
    ) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("method", req.getMethod());
        out.put("contentType", req.getContentType());
        out.put("parameter", parameter); // ← ここに送ったJSON文字列がそのまま入る
        // ヘッダーも確認したい場合
        Map<String, String> headers = new LinkedHashMap<>();
        Enumeration<String> names = req.getHeaderNames();
        while (names.hasMoreElements()) {
            String n = names.nextElement();
            headers.put(n, req.getHeader(n));
        }
        out.put("headers", headers);
        return out;
    }
}
