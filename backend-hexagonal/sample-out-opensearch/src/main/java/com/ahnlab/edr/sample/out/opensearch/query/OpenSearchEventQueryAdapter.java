package com.ahnlab.edr.sample.out.opensearch.query;

import com.ahnlab.edr.sample.config.OpenSearchOutboundEnabled;
import com.ahnlab.edr.sample.core.application.query.port.out.EventQueryStorePort;
import com.ahnlab.edr.sample.core.domain.entity.EventEntity;
import com.ahnlab.edr.sample.out.opensearch.store.OpenSearchEventStore;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Query-side adapter that retrieves events from the shared OpenSearch event store.
 * Implements only {@link EventQueryStorePort} (read responsibility).
 */
@Component
@OpenSearchOutboundEnabled
@RequiredArgsConstructor
public class OpenSearchEventQueryAdapter implements EventQueryStorePort {

	private final OpenSearchEventStore eventStore;

	@Override
	public Optional<EventEntity> findById(String id) {
		System.out.println("[OS-Query] OpenSearchEventQueryAdapter.findById - id=" + id);
		Optional<EventEntity> result = eventStore.get(id);
		System.out.println("[OS-Query] OpenSearchEventQueryAdapter.findById - found? " + result.isPresent());
		return result;
	}
}
