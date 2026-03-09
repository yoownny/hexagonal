CREATE SEQUENCE IF NOT EXISTS tb_edr_custom_rule_apply_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE IF NOT EXISTS tb_edr_custom_rule_apply (
    apply_id                integer                 DEFAULT nextval('tb_edr_custom_rule_apply_seq'::regclass),    
    platform_type           enum_platform_type,     NOT NULL,
    rule_id_set             integer[],
    rule_snapshot           text,
    apply_status            enum_apply_status
    admin_id                text,
    admin_ip                text,
    create_time             timestamp                 DEFAULT timezone('UTC', now()),
    modified_time           timestamp                 DEFAULT timezone('UTC', now()),
    CONSTRAINT tb_edr_custom_rule_apply_pkey PRIMARY KEY (apply_id)
);

COMMENT ON TABLE tb_edr_custom_rule_apply IS '사용자 정의 규칙 적용 정보';

COMMENT ON COLUMN tb_edr_custom_rule_apply.apply_id         IS '규칙 적용 ID';

COMMENT ON COLUMN tb_edr_custom_rule_apply.platform_type    IS '플랫폼 유형';
COMMENT ON COLUMN tb_edr_custom_rule_apply.rule_id_set      IS '적용된 규칙 ID 구성';
COMMENT ON COLUMN tb_edr_custom_rule_apply.rule_snapshot    IS '규칙 스냅샷(sha256)';
COMMENT ON COLUMN tb_edr_custom_rule_apply.apply_status     IS '규칙 적용 상태';
COMMENT ON COLUMN tb_edr_custom_rule_apply.admin_id         IS '규칙 적용 관리자 ID';
COMMENT ON COLUMN tb_edr_custom_rule_apply.admin_ip         IS '규칙 적용 관리자 IP';
COMMENT ON COLUMN tb_edr_custom_rule_apply.create_time      IS '규칙 적용 시간';
COMMENT ON COLUMN tb_edr_custom_rule_apply.modified_time    IS '규칙 적용 변경 시간';