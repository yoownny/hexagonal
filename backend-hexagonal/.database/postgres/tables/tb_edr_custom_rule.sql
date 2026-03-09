CREATE SEQUENCE IF NOT EXISTS tb_edr_custom_rule_id_seq
    START WITH 269484032
    INCREMENT BY 270532608
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE IF NOT EXISTS tb_edr_custom_rule (
    rule_id                 integer                 DEFAULT nextval('tb_edr_custom_rule_id_seq'::regclass),
    rule_enabled            boolean                 DEFAULT true,    
    platform_type           enum_platform_type,     NOT NULL,
    rule_name               text                    NOT NULL,
    severity                enum_severity           NOT NULL,
    diagnosis_name          text                    NOT NULL,
    diagnosis_message            text,
    rule_dsl                text,
    rule_status             enum_rule_status,    
    admin_id                text,
    admin_ip                text,
    create_time             timestamp               DEFAULT timezone('UTC', now()),
    modified_time           timestamp               DEFAULT timezone('UTC', now()),
    CONSTRAINT tb_edr_custom_rule_pkey PRIMARY KEY (rule_id)
);

COMMENT ON TABLE tb_edr_custom_rule IS '사용자 정의 규칙 정보';

COMMENT ON COLUMN tb_edr_custom_rule.rule_id IS '규칙 ID';
COMMENT ON COLUMN tb_edr_custom_rule.rule_enabled IS '규칙 사용 여부';
COMMENT ON COLUMN tb_edr_custom_rule.platform_type IS '플랫폼 유형';
COMMENT ON COLUMN tb_edr_custom_rule.rule_name IS '규칙 이름';
COMMENT ON COLUMN tb_edr_custom_rule.severity IS '위험도';
COMMENT ON COLUMN tb_edr_custom_rule.diagnosis_name IS '진단명';
COMMENT ON COLUMN tb_edr_custom_rule.diagnosis_message IS '진단 메시지';
COMMENT ON COLUMN tb_edr_custom_rule.rule_status IS '규칙 상태';
COMMENT ON COLUMN tb_edr_custom_rule.admin_id IS '규칙 작성 관리자 ID';
COMMENT ON COLUMN tb_edr_custom_rule.admin_ip IS '규칙 작성 관리자 IP';
COMMENT ON COLUMN tb_edr_custom_rule.create_time IS '규칙 등록 날짜';
COMMENT ON COLUMN tb_edr_custom_rule.modified_time IS '규칙 수정 날짜';