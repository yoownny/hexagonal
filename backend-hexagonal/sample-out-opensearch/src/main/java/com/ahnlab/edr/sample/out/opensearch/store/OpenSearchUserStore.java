package com.ahnlab.edr.sample.out.opensearch.store;

import com.ahnlab.edr.sample.config.OpenSearchOutboundEnabled;
import com.ahnlab.edr.sample.core.domain.entity.UserEntity;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Shared in-memory store for OpenSearch user data.
 * Both the command adapter and query adapter reference this single store
 * to maintain consistency between writes and reads.
 */
@Component
@OpenSearchOutboundEnabled
public class OpenSearchUserStore {

	private final Map<String, UserEntity> store = new ConcurrentHashMap<>();

	public void put(String id, UserEntity entity) {
		store.put(id, entity);
	}

	public Optional<UserEntity> get(String id) {
		return Optional.ofNullable(store.get(id));
	}
}
