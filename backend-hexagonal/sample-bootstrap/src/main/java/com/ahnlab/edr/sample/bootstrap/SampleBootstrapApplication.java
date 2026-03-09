package com.ahnlab.edr.sample.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.Environment;

/**
 * Spring Boot entry point that wires the core and adapter modules together.
 *
 * The active combination is chosen using two properties:
 * <ul>
 *   <li>{@code sample.inbound.http-enabled}: {@code true} or {@code false}</li>
 *   <li>{@code sample.inbound.grpc-enabled}: {@code true} or {@code false}</li>
 *   <li>{@code sample.outbound}: {@code opensearch} or {@code clickhouse}</li>
 * </ul>
 *
 * This allows four combinations, for example:
 * <ul>
 *   <li>HTTP only: {@code sample.inbound.http-enabled=true}, {@code sample.inbound.grpc-enabled=false}</li>
 *   <li>gRPC only: {@code sample.inbound.http-enabled=false}, {@code sample.inbound.grpc-enabled=true}</li>
 *   <li>Both HTTP + gRPC: both flags {@code true}</li>
 * </ul>
 */
@SpringBootApplication(scanBasePackages = "com.ahnlab.edr.sample")
public class SampleBootstrapApplication implements ApplicationListener<ContextRefreshedEvent> {

	public static void main(String[] args) {
		SpringApplication.run(SampleBootstrapApplication.class, args);
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		Environment env = event.getApplicationContext().getEnvironment();
		String httpEnabled = env.getProperty("sample.inbound.http-enabled", "true");
		String grpcEnabled = env.getProperty("sample.inbound.grpc-enabled", "false");
		String outbound = env.getProperty("sample.outbound", "opensearch");
		System.out.println("[BOOT] SampleBootstrapApplication started with sample.inbound.http-enabled=" + httpEnabled
			+ ", sample.inbound.grpc-enabled=" + grpcEnabled
			+ ", sample.outbound=" + outbound);
	}
}