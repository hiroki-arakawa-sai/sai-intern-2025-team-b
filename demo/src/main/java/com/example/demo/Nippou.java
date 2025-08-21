package com.example.demo;

import jakarta.persistence.*;

@Entity
@Table(name="\"日報メモ\"", schema="public")
public class Nippou {

  @EmbeddedId
  private NippouId id;

  @Column(name="\"名前\"", nullable=false)
  private String name;

  @Column(name="\"メモ\"", nullable=false)
  private String memo;

  public Nippou() {}

  // getters/setters
  public NippouId getId() { return id; }
  public void setId(NippouId id) { this.id = id; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public String getMemo() { return memo; }
  public void setMemo(String memo) { this.memo = memo; }
}
