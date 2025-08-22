package com.example.demo;

import java.time.LocalDate;
import java.time.LocalTime;
import jakarta.persistence.*;

@Entity
@Table(name="\"日報メモ\"", schema="public")
public class Nippou {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name="\"日付\"", nullable=false, columnDefinition="date")
  private LocalDate day;

  @Column(name="\"時間\"", nullable=false, columnDefinition="time")
  private LocalTime time;

  @Column(name="\"場所\"", nullable=false)
  private String place;

  @Column(name="\"名前\"", nullable=false)
  private String name;

  @Column(name="\"メモ\"", nullable=false)
  private String memo;

  public Nippou() {}

  // ---- getters / setters ----
  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public LocalDate getDay() { return day; }
  public void setDay(LocalDate day) { this.day = day; }

  public LocalTime getTime() { return time; }
  public void setTime(LocalTime time) { this.time = time; }

  public String getPlace() { return place; }
  public void setPlace(String place) { this.place = place; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public String getMemo() { return memo; }
  public void setMemo(String memo) { this.memo = memo; }
}
