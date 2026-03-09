package com.ahnlab.edr.sample.in.http.config;

import com.ahnlab.edr.sample.config.HttpInboundEnabled;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.web.bind.annotation.RestController;

/**
 * HTTP 인바운드 컨트롤러를 조건부로 활성화하는 설정.
 *
 * sample.inbound.http-enabled=true 일 때만
 * com.ahnlab.edr.sample.in.http.web 패키지의 @RestController 들을 스캔한다.
 */
@Configuration
@HttpInboundEnabled
@ComponentScan(
	basePackages = "com.ahnlab.edr.sample.in.http.web",
	includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = RestController.class),
	useDefaultFilters = false
)
public class HttpInboundConfiguration {
}
