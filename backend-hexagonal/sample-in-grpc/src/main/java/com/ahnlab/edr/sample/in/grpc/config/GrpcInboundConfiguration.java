package com.ahnlab.edr.sample.in.grpc.config;

import com.ahnlab.edr.sample.config.GrpcInboundEnabled;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Service;

/**
 * gRPC 인바운드 파사드를 조건부로 활성화하는 설정.
 *
 * sample.inbound.grpc-enabled=true 일 때만
 * com.ahnlab.edr.sample.in.grpc 패키지의 @Service 들을 스캔한다.
 */
@Configuration
@GrpcInboundEnabled
@ComponentScan(
	basePackages = "com.ahnlab.edr.sample.in.grpc",
	includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Service.class),
	useDefaultFilters = false
)
public class GrpcInboundConfiguration {
}
