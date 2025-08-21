package com.example.demo;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import jakarta.persistence.*;
import java.util.Objects;

@Embeddable
public class NippouId implements Serializable {
  @Column(name="\"日付\"", nullable=false, columnDefinition="date")
  private LocalDate day;

  @Column(name="\"時間\"", nullable=false, columnDefinition="time")
  private LocalTime time;

  @Column(name="\"場所\"", nullable=false)
  private String place;

  public NippouId() {}
  public NippouId(LocalDate day, LocalTime time, String place) {
    this.day = day; this.time = time; this.place = place;
  }

  // getters/setters
  public LocalDate getDay() { return day; }
  public void setDay(LocalDate day) { this.day = day; }
  public LocalTime getTime() { return time; }
  public void setTime(LocalTime time) { this.time = time; }
  public String getPlace() { return place; }
  public void setPlace(String place) { this.place = place; }

  @Override public boolean equals(Object o){
    if(this==o) return true;
    if(!(o instanceof NippouId other)) return false;
    return Objects.equals(day, other.day) &&
           Objects.equals(time, other.time) &&
           Objects.equals(place, other.place);
  }
  @Override public int hashCode(){ return Objects.hash(day, time, place); }
}
