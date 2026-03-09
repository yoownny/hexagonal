package com.ahnlab.edr.sample.out.opensearch.command;

import com.ahnlab.edr.sample.config.OpenSearchOutboundEnabled;
import com.ahnlab.edr.sample.core.application.command.port.out.EventCommandStorePort;
import com.ahnlab.edr.sample.core.domain.entity.EventEntity;
import com.ahnlab.edr.sample.out.opensearch.store.OpenSearchEventStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Command-side adapter that persists events to the shared OpenSearch event store.
 * Implements only {@link EventCommandStorePort} (write responsibility).
 */
@Component
@OpenSearchOutboundEnabled
@RequiredArgsConstructor
public class OpenSearchEventCommandAdapter implements EventCommandStorePort {

	private final OpenSearchEventStore eventStore;

	@Override
	public void save(EventEntity entity) {
		System.out.println("[OS-Command] OpenSearchEventCommandAdapter.save - id=" + entity.getId());
		eventStore.put(entity.getId(), entity);
	}
}
