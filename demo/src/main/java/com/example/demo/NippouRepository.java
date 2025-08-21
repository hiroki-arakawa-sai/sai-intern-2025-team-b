package com.example.demo;

import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface NippouRepository extends JpaRepository<Nippou, Long> { }
