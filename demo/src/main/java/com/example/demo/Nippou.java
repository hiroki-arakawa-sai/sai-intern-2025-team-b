package com.example.demo;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.*;

@Entity
@Table(name = "日報メモ")
public class Nippou {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "日付", nullable = false, columnDefinition = "date")
  private LocalDate day;

  @Column(name = "時間", nullable = false, columnDefinition = "time")
  private LocalTime time;

  @Column(name = "場所", nullable = false)
  private String place;

  @Column(name = "名前", nullable = false)
  private String name;

  @Column(name = "メモ", nullable = false)
  private String memo;

  // --- setter / getter 追加 ---
  public String getMemo() {
    return memo;
  }

  public void setMemo(String memo) {
    this.memo = memo;
  }
}
