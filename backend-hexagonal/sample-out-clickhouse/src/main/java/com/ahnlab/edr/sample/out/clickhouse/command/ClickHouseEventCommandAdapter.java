package com.ahnlab.edr.sample.out.clickhouse.command;

import com.ahnlab.edr.sample.config.ClickHouseOutboundEnabled;
import com.ahnlab.edr.sample.core.application.command.port.out.EventCommandStorePort;
import com.ahnlab.edr.sample.core.domain.entity.EventEntity;
import com.ahnlab.edr.sample.out.clickhouse.store.ClickHouseEventStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Command-side adapter that persists events to the shared ClickHouse event store.
 * Implements only {@link EventCommandStorePort} (write responsibility).
 */
@Component
@ClickHouseOutboundEnabled
@RequiredArgsConstructor
public class ClickHouseEventCommandAdapter implements EventCommandStorePort {

	private final ClickHouseEventStore eventStore;

	@Override
	public void save(EventEntity entity) {
		System.out.println("[CH-Command] ClickHouseEventCommandAdapter.save - id=" + entity.getId());
		eventStore.put(entity.getId(), entity);
	}
}
