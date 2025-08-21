package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
@Repository
public interface repo extends JpaRepository<Event, Event.EventId> {
    List<Event> findAllByEventDate(LocalDate eventDate);
    // 範囲で絞るなら:
    List<Event> findAllByEventDateBetween(LocalDate start, LocalDate end);
}
