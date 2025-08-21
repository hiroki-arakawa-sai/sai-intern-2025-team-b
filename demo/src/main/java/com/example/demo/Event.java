package com.example.demo;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

@Entity
@Table(name = "\"日報メモ\"", schema = "public")
@IdClass(Event.EventId.class)
public class Event {

    // === 複合主キー（"日付","時間","場所"） ===
    @Id
    @Column(name = "\"日付\"", nullable = false)
    private LocalDate eventDate;

    @Id
    @Column(name = "\"時間\"", nullable = false, columnDefinition = "time(0)")
    private LocalTime eventTime;

    @Id
    @Column(name = "\"場所\"", nullable = false, columnDefinition = "text")
    // DB側は CHECK ("場所" IN (...))。アプリ側でも簡易バリデーション
    @Pattern(regexp = "食品売り場|テナント|駐車場",
             message = "場所は『食品売り場』『テナント』『駐車場』のいずれかを指定してください")
    private String location;

    // === その他カラム（NOT NULL / text） ===
    @Column(name = "\"名前\"", nullable = false, columnDefinition = "text")
    @NotBlank
    private String personName;

    @Column(name = "\"メモ\"", nullable = false, columnDefinition = "text")
    @NotBlank
    private String memo;

    protected Event() {} // JPA用

    public Event(LocalDate eventDate, LocalTime eventTime, String location,
                 String personName, String memo) {
        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.location = location;
        this.personName = personName;
        this.memo = memo;
    }

    // --- getter / setter ---
    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }

    public LocalTime getEventTime() { return eventTime; }
    public void setEventTime(LocalTime eventTime) { this.eventTime = eventTime; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getPersonName() { return personName; }
    public void setPersonName(String personName) { this.personName = personName; }

    public String getMemo() { return memo; }
    public void setMemo(String memo) { this.memo = memo; }

    // === 複合主キー用 IdClass ===
    public static class EventId implements Serializable {
        private LocalDate eventDate;
        private LocalTime eventTime;
        private String location;

        public EventId() {}
        public EventId(LocalDate eventDate, LocalTime eventTime, String location) {
            this.eventDate = eventDate;
            this.eventTime = eventTime;
            this.location = location;
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EventId that = (EventId) o;
            return Objects.equals(eventDate, that.eventDate)
                && Objects.equals(eventTime, that.eventTime)
                && Objects.equals(location, that.location);
        }
        @Override public int hashCode() {
            return Objects.hash(eventDate, eventTime, location);
        }
    }
}
