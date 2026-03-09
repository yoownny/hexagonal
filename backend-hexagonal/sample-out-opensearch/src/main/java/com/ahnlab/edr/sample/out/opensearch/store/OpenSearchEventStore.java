package com.ahnlab.edr.sample.out.opensearch.store;

import com.ahnlab.edr.sample.config.OpenSearchOutboundEnabled;
import com.ahnlab.edr.sample.core.domain.entity.EventEntity;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Shared in-memory store for OpenSearch event data.
 * Both the command adapter and query adapter reference this single store
 * to maintain consistency between writes and reads.
 */
@Component
@OpenSearchOutboundEnabled
public class OpenSearchEventStore {

	private final Map<String, EventEntity> store = new ConcurrentHashMap<>();

	public void put(String id, EventEntity entity) {
		store.put(id, entity);
	}

	public Optional<EventEntity> get(String id) {
		return Optional.ofNullable(store.get(id));
	}
}
