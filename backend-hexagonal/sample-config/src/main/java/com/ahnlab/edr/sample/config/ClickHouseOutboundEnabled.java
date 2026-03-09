package com.ahnlab.edr.sample.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Meta-annotation for enabling an outbound adapter when ClickHouse is selected.
 * Equivalent to:
 * {@code @ConditionalOnProperty(name = "sample.outbound", havingValue = "clickhouse")}
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ConditionalOnProperty(name = "sample.outbound", havingValue = "clickhouse")
public @interface ClickHouseOutboundEnabled {
}
