package com.example.demo;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NippouRepository extends JpaRepository<Nippou, Long> {

  // 新しい順に直近10件
  List<Nippou> findTop10ByOrderByIdDesc();

  // （任意）JSTの今日だけ取りたい場合に使える
  List<Nippou> findByDayOrderByTimeAscIdAsc(LocalDate day);
}
