DO $$
DECLARE
BEGIN

  IF NOT EXISTS ( SELECT 1 FROM pg_type WHERE typtype = 'e' AND typname = 'enum_severity' ) THEN
      CREATE TYPE enum_severity AS ENUM ();
  END IF;

END
$$ LANGUAGE 'plpgsql';

COMMENT ON TYPE enum_severity IS '위험도';
ALTER TYPE enum_severity ADD VALUE IF NOT EXISTS 'HIGH';             -- 상
ALTER TYPE enum_severity ADD VALUE IF NOT EXISTS 'MIDIUM';            -- 중
ALTER TYPE enum_severity ADD VALUE IF NOT EXISTS 'LOW';               -- 하
ALTER TYPE enum_severity ADD VALUE IF NOT EXISTS 'INFORMATION';       -- 정보
