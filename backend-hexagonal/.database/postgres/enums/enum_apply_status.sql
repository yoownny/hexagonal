DO $$
DECLARE
BEGIN

  IF NOT EXISTS ( SELECT 1 FROM pg_type WHERE typtype = 'e' AND typname = 'enum_apply_status' ) THEN
      CREATE TYPE enum_apply_status AS ENUM ();
  END IF;

END
$$ LANGUAGE 'plpgsql';

COMMENT ON TYPE enum_apply_status IS '규칙 적용 상태';
ALTER TYPE enum_apply_status ADD VALUE IF NOT EXISTS 'READY';                   -- 규칙 적용 준비
ALTER TYPE enum_apply_status ADD VALUE IF NOT EXISTS 'APPLY';                   -- 규칙 적용
ALTER TYPE enum_apply_status ADD VALUE IF NOT EXISTS 'DISPOSE';                 -- 규칙 폐기
