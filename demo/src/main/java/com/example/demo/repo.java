package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface repo extends JpaRepository<Event, Long> {
    List<Event> findAllByEventDate(LocalDate eventDate);
    // 範囲で絞るなら:
    List<Event> findAllByEventDateBetween(LocalDate start, LocalDate end);
}
