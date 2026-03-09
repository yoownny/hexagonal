DO $$
DECLARE
BEGIN

  IF NOT EXISTS ( SELECT 1 FROM pg_type WHERE typtype = 'e' AND typname = 'enum_platform_type' ) THEN
      CREATE TYPE en_platform_type AS ENUM ();
  END IF;

END
$$ LANGUAGE 'plpgsql';

COMMENT ON TYPE en_platform_type IS '플랫폼 유형';
ALTER TYPE en_platform_type ADD VALUE IF NOT EXISTS ' WINDOWS';             -- 윈도우
ALTER TYPE en_platform_type ADD VALUE IF NOT EXISTS 'MAC';                  -- 맥
ALTER TYPE en_platform_type ADD VALUE IF NOT EXISTS 'LINUX';                -- 리눅스
