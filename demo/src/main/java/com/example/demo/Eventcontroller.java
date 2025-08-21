package com.example.demo;

import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

// 例: EventsController.java
@Controller
public class Eventcontroller {

    private final repo service; // または repository

    public Eventcontroller(repo service) {
        this.service = service;
    }

    @GetMapping("/events")
    public String list(
        @RequestParam(value = "eventDate", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        Model model
    ) {
        // 画面項目用（input type="date" は yyyy-MM-dd 文字列が安全）
        model.addAttribute("selectedDate", date != null ? date.toString() : "2025-08-21");
        System.out.println(date != null ? date.toString() : "からでした。");
        // 一覧データ（nullなら全件、など用途に合わせて）
        List<Event> events = (date != null)
            ? service.findAllByEventDate(date)
            : service.findAll();
        model.addAttribute("events", events != null ? events : List.of()); // 常に空リスト以上にする

        return "list"; // src/main/resources/templates/events.html
    }
}
