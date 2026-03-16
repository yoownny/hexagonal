package com.ahnlab.edr.sample.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * gRPC Inbound Adapter 활성화 조건.
 * application.yml: sample.inbound.grpc-enabled: true
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ConditionalOnProperty(
    name = "sample.inbound.grpc-enabled",
    havingValue = "true",
    matchIfMissing = false
)
public @interface GrpcInboundEnabled {
}
