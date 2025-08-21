package com.example.demo;

import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
public class Eventcontroller {
    private final repo repository;

    // ★ 依存性注入（必須）
    public Eventcontroller(repo repository) {
        this.repository = repository;
    }

    @GetMapping("/events")
    public String list(
            @RequestParam(name = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model
    ) {
        List<Event> events = (date != null)
                ? repository.findAllByEventDate(date)
                : repository.findAll(
                    Sort.by("eventDate").ascending()
                        .and(Sort.by("eventTime").ascending())
                  );
        model.addAttribute("events", events);
        model.addAttribute("selectedDate", date);
        return "events/list";
    }
}
