package com.example.demo.nippo;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

@Service
public class NippoUpdateOnlyService {

    private final JdbcTemplate jdbc;
    public NippoUpdateOnlyService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    private void assertTimeRange(LocalTime t) {
        if (t.isBefore(LocalTime.of(10,0)) || !t.isBefore(LocalTime.of(17,0))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "時間は 10:00～16:59:59 にしてください");
        }
    }

    /** テンプレ（名前='未設定'）を、指定の newName と memo に置き換える。更新0件なら404。 */
    @Transactional
    public void fillSlot(FillSlotRequest req) {
        LocalDate d = (req.date() != null) ? req.date() : LocalDate.now();
        LocalTime t = (req.time() != null) ? req.time() : LocalTime.now();
        t = t.truncatedTo(ChronoUnit.SECONDS);
        assertTimeRange(t);

        int n = jdbc.update("""
            UPDATE public."日報メモ"
               SET "名前" = ?, "メモ" = ?
             WHERE "日付" = ? AND "時間" = ? AND "場所" = ?
               AND "名前" = '未設定'
        """, req.newName(), req.memo(), Date.valueOf(d), Time.valueOf(t), req.place());

        if (n == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "対象のテンプレ（名前=未設定）が見つかりません");
        }
    }

    /** 既存行のメモだけ差し替える。更新0件なら404。 */
    @Transactional
    public void updateMemo(UpdateMemoRequest req) {
        LocalDate d = (req.date() != null) ? req.date() : LocalDate.now();
        LocalTime t = (req.time() != null) ? req.time() : LocalTime.now();
        t = t.truncatedTo(ChronoUnit.SECONDS);
        assertTimeRange(t);

        int n = jdbc.update("""
            UPDATE public."日報メモ"
               SET "メモ" = ?
             WHERE "日付" = ? AND "時間" = ? AND "場所" = ? AND "名前" = ?
        """, req.memo(), Date.valueOf(d), Time.valueOf(t), req.place(), req.name());

        if (n == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "指定の行が見つかりません");
        }
    }
}
