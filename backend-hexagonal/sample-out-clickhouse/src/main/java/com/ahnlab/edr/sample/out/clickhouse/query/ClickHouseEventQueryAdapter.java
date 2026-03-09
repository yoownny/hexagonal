package com.ahnlab.edr.sample.out.clickhouse.query;

import com.ahnlab.edr.sample.config.ClickHouseOutboundEnabled;
import com.ahnlab.edr.sample.core.application.query.port.out.EventQueryStorePort;
import com.ahnlab.edr.sample.core.domain.entity.EventEntity;
import com.ahnlab.edr.sample.out.clickhouse.store.ClickHouseEventStore;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Query-side adapter that retrieves events from the shared ClickHouse event store.
 * Implements only {@link EventQueryStorePort} (read responsibility).
 */
@Component
@ClickHouseOutboundEnabled
@RequiredArgsConstructor
public class ClickHouseEventQueryAdapter implements EventQueryStorePort {

	private final ClickHouseEventStore eventStore;

	@Override
	public Optional<EventEntity> findById(String id) {
		System.out.println("[CH-Query] ClickHouseEventQueryAdapter.findById - id=" + id);
		Optional<EventEntity> result = eventStore.get(id);
		System.out.println("[CH-Query] ClickHouseEventQueryAdapter.findById - found? " + result.isPresent());
		return result;
	}
}
