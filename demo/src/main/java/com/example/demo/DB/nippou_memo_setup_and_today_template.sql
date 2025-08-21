-- 0) （必要なら）テーブルが無ければ作成
CREATE TABLE IF NOT EXISTS public."日報メモ" (
  "日付"  date    NOT NULL DEFAULT (now() AT TIME ZONE 'Asia/Tokyo')::date,
  "時間"  time(0) NOT NULL,
  "場所"  text    NOT NULL CHECK (length(btrim("場所")) > 0),
  "名前"  text    NOT NULL CHECK (length(btrim("名前")) > 0),
  "メモ"  text    NOT NULL CHECK (length(btrim("メモ")) > 0),
  PRIMARY KEY ("日付","時間","場所"),
  CONSTRAINT ck_時間_10to16 CHECK ("時間" >= TIME '10:00' AND "時間" < TIME '17:00'),
  CONSTRAINT ck_場所_allowed CHECK ("場所" IN ('食品売り場','テナント','駐車場'))
);

-- 1) "日付","時間","場所" が同じ重複行を1件に整理
WITH ranked AS (
  SELECT
    ctid,
    ROW_NUMBER() OVER (
      PARTITION BY "日付","時間","場所"
      ORDER BY ("名前" = '未設定') DESC, "名前"
    ) AS rn
  FROM public."日報メモ"
)
DELETE FROM public."日報メモ" t
USING ranked r
WHERE t.ctid = r.ctid AND r.rn > 1;

-- 2) ON CONFLICT なしでも重複防止できるよう、ユニーク索引を用意（既存なら何もしない）
CREATE UNIQUE INDEX IF NOT EXISTS "日報メモ_uniq_日付_時間_場所"
  ON public."日報メモ" ("日付","時間","場所");

-- 3) 今日分テンプレを追加（NOT EXISTS で重複回避）
WITH jst_today AS (
  SELECT (now() AT TIME ZONE 'Asia/Tokyo')::date AS d
),
hours AS (
  SELECT (TIME '10:00' + (n || ' hour')::interval)::time(0) AS t, n
  FROM generate_series(0, 6) AS n        -- 10..16時
),
one_each AS (                             -- 10..15時：1件ずつ
  SELECT h.t,
         CASE ((h.n % 3) + 1)
           WHEN 1 THEN '食品売り場'
           WHEN 2 THEN 'テナント'
           WHEN 3 THEN '駐車場'
         END AS 場所
  FROM hours h
  WHERE h.t < TIME '16:00'
),
three_at_16 AS (                          -- 16:00：3件
  SELECT TIME '16:00'::time(0) AS t, v.場所
  FROM (VALUES ('食品売り場'), ('テナント'), ('駐車場')) AS v(場所)
),
rows AS (
  SELECT * FROM one_each
  UNION ALL
  SELECT * FROM three_at_16
)
INSERT INTO public."日報メモ"("日付","時間","場所","名前","メモ")
SELECT d, t, 場所, '未設定', '（未入力）'
FROM rows, jst_today
WHERE NOT EXISTS (
  SELECT 1 FROM public."日報メモ" m
  WHERE m."日付" = d AND m."時間" = t AND m."場所" = rows.場所
);

-- 4) 確認用
WITH jst_today AS (SELECT (now() AT TIME ZONE 'Asia/Tokyo')::date AS d)
SELECT
  "日付",
  to_char("時間",'HH24:MI') AS "時間",
  "場所","名前","メモ"
FROM public."日報メモ"
WHERE "日付" = (SELECT d FROM jst_today)
ORDER BY
  "時間",
  CASE
    WHEN "時間" = TIME '16:00' THEN
      CASE "場所"
        WHEN '食品売り場' THEN 1
        WHEN 'テナント'   THEN 2
        WHEN '駐車場'     THEN 3
        ELSE 99
      END
    ELSE 0
  END,
  "場所","名前";
