package com.ahnlab.edr.sample.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Meta-annotation that enables a component when HTTP inbound is turned on.
 * Equivalent to:
 * {@code @ConditionalOnProperty(name = "sample.inbound.http-enabled", havingValue = "true", matchIfMissing = true)}
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ConditionalOnProperty(name = "sample.inbound.http-enabled", havingValue = "true", matchIfMissing = true)
public @interface HttpInboundEnabled {
}
