DO $$
DECLARE
BEGIN

  IF NOT EXISTS ( SELECT 1 FROM pg_type WHERE typtype = 'e' AND typname = 'enum_rule_status' ) THEN
      CREATE TYPE enum_rule_status AS ENUM ();
  END IF;

END
$$ LANGUAGE 'plpgsql';

COMMENT ON TYPE enum_rule_status IS '규칙 상태';
ALTER TYPE enum_rule_status ADD VALUE IF NOT EXISTS 'ADD';                     -- 규칙 추가
ALTER TYPE enum_rule_status ADD VALUE IF NOT EXISTS 'MODIFY';                  -- 규칙 수정
ALTER TYPE enum_rule_status ADD VALUE IF NOT EXISTS 'DELETE';                  -- 규칙 삭제
ALTER TYPE enum_rule_status ADD VALUE IF NOT EXISTS 'APPLY';                   -- 규칙 적용
