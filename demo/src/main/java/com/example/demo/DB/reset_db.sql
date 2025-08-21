-- reset_append_mode.sql
BEGIN;

-- 1) （あれば）ユニーク索引を削除
DROP INDEX IF EXISTS "日報メモ_uniq_日付_時間_場所";

-- 2) （あれば）既存の主キー（複合PK）を削除
DO $$
DECLARE pk_name text;
BEGIN
  SELECT c.conname INTO pk_name
  FROM pg_constraint c
  JOIN pg_class t ON t.oid = c.conrelid
  JOIN pg_namespace n ON n.oid = t.relnamespace
  WHERE n.nspname = 'public'
    AND t.relname = '日報メモ'
    AND c.contype = 'p'
  LIMIT 1;

  IF pk_name IS NOT NULL THEN
    EXECUTE format('ALTER TABLE public."日報メモ" DROP CONSTRAINT %I', pk_name);
  END IF;
END $$;

-- 3) id を追加（無ければ）し、id を主キーに
ALTER TABLE public."日報メモ"
  ADD COLUMN IF NOT EXISTS id bigserial;

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM pg_constraint c
    JOIN pg_class t ON t.oid = c.conrelid
    JOIN pg_namespace n ON n.oid = t.relnamespace
    WHERE n.nspname='public' AND t.relname='日報メモ'
      AND c.contype='p' AND c.conname='日報メモ_id_pkey'
  ) THEN
    ALTER TABLE public."日報メモ"
      ADD CONSTRAINT "日報メモ_id_pkey" PRIMARY KEY (id);
  END IF;
END $$;

-- （必要に応じてチェック制約は残す）
-- 例: 16時台以外も書きたいなら下行を有効化
-- ALTER TABLE public."日報メモ" DROP CONSTRAINT IF EXISTS ck_時間_10to16;

-- 4) データをまっさらに（ID もリセット）
TRUNCATE TABLE public."日報メモ" RESTART IDENTITY;

COMMIT;
