package com.example.demo;

import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MemoDisplayResponder {

  private final NippouRepository repo;
  private static final ZoneId JST = ZoneId.of("Asia/Tokyo");

  public MemoDisplayResponder(NippouRepository repo) {
    this.repo = repo;
  }

  /**
   * 受け取った data が「日報メモを表示」なら、最新10件を読み上げて返す。
   * それ以外は空文字を返す（＝何もしない）。
   */
  public String maybeHandle(String data) {
    if (!"日報メモを表示".equals(data)) return "";

    // 直近10件（新しい順）
    List<Nippou> rows = repo.findTop10ByOrderByIdDesc();

    String message;
    if (rows.isEmpty()) {
      message = "「日報メモはまだありません」";
    } else {
      String body = rows.stream()
          .map(n -> String.format(
              "%s %s %s %s：%s",
              // day は LocalDate, time は LocalTime
              n.getDay() != null ? n.getDay().toString() : "--",
              formatTime(n.getTime()),
              n.getPlace(),
              n.getName(),
              n.getMemo()
          ))
          .collect(Collectors.joining("。"));
      message = "「直近の記録を表示します。" + body + "」";
    }

    // BuddyBot へ送信
    BuddyBotCallApplication bot = new BuddyBotCallApplication();
    try {bot.call(message);} catch(Exception e){
      System.err.println(e.getMessage());
    }

    return message;
  }

  private static String formatTime(LocalTime t) {
    return (t == null) ? "--時--分" : String.format("%02d時%02d分", t.getHour(), t.getMinute());
  }
}
