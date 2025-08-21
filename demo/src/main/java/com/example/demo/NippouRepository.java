package com.example.demo;

import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface NippouRepository extends JpaRepository<Nippou, NippouId> {

  @Modifying
  @Transactional
  @Query(value = """
    UPDATE public."日報メモ"
       SET "メモ" = :memo,
           "名前" = :name
     WHERE "日付" = :day
       AND "時間" = :time
       AND "場所" = :place
  """, nativeQuery = true)
  int updateMemo(
      @Param("day")   LocalDate day,
      @Param("time")  LocalTime time,
      @Param("place") String place,
      @Param("name")  String name,
      @Param("memo")  String memo
  );
}
