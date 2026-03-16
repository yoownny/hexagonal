package com.ahnlab.edr.sample.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ClickHouse Outbound Adapter 활성화 조건.
 * application.yml: sample.outbound: clickhouse
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ConditionalOnProperty(
    name = "sample.outbound",
    havingValue = "clickhouse"
)
public @interface ClickHouseOutboundEnabled {
}
