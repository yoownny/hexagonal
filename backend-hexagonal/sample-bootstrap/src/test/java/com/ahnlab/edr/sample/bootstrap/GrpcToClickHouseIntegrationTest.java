package com.ahnlab.edr.sample.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;

import com.ahnlab.edr.sample.core.application.query.port.out.EventQueryStorePort;
import com.ahnlab.edr.sample.core.domain.entity.EventEntity;
import com.ahnlab.edr.sample.in.grpc.command.GrpcEventCommandFacade;
import com.ahnlab.edr.sample.in.grpc.dto.GrpcEventRequest;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Integration test that verifies the grpc + clickhouse combination:
 * <ul>
 *   <li>Inbound: {@code sample.inbound.grpc-enabled=true} (GrpcEventCommandFacade)</li>
 *   <li>Outbound: {@code sample.outbound=clickhouse} (InMemoryClickHouseEventStoreAdapter)</li>
 * </ul>
 */
@SpringBootTest(
		classes = SampleBootstrapApplication.class,
		properties = {
			"sample.inbound.http-enabled=false",
			"sample.inbound.grpc-enabled=true",
			"sample.outbound=clickhouse"
		}
)
class GrpcToClickHouseIntegrationTest {

	@Autowired
	private GrpcEventCommandFacade grpcEventCommandFacade;

	@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
	@Autowired
	private EventQueryStorePort eventStorePort;

	@Test
	void saveThroughGrpcIsStoredInClickHouseAdapter() {
		String id = "grpc-ch-1";
		String message = "hello from grpc to clickhouse";

		grpcEventCommandFacade.save(new GrpcEventRequest(id, message));

		Optional<EventEntity> found = eventStorePort.findById(id);
		assertThat(found).isPresent();
		assertThat(found.get().getMessage()).isEqualTo(message);
	}

	@Test
	void eventStoreIsClickHouseImplementation() {
		String implementationClassName = eventStorePort.getClass().getName();
		assertThat(implementationClassName)
			.contains("ClickHouseEventQueryAdapter");
	}
}
