package com.example.demo.nippo;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import java.time.LocalTime;

public record FillSlotRequest(
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
        @JsonFormat(pattern = "HH:mm[:ss]") LocalTime time,
        @Pattern(regexp = "^(食品|テナント|駐車場)$",
                 message = "場所は 食品/テナント/駐車場 を指定してください")
        String place,
        @NotBlank String newName,   // '未設定' を置換
        @NotBlank String memo
) {}
