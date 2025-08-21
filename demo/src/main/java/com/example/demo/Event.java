package com.example.demo;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Column(name = "event_time")
    private LocalTime eventTime;

    private String location;

    @Column(length = 1000)
    private String memo;

    public Event() {}

    public Event(LocalDate eventDate, LocalTime eventTime, String location, String memo) {
        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.location = location;
        this.memo = memo;
    }

    public Long getId() { return id; }
    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }
    public LocalTime getEventTime() { return eventTime; }
    public void setEventTime(LocalTime eventTime) { this.eventTime = eventTime; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getMemo() { return memo; }
    public void setMemo(String memo) { this.memo = memo; }
}
