package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.List;
@Repository
public interface repo extends JpaRepository<Event, Event.EventId> {
    List<Event> findAllByEventDate(LocalDate eventDate);
    List<Event> findAllByEventDate(LocalDate eventDate,Sort sort);
}
