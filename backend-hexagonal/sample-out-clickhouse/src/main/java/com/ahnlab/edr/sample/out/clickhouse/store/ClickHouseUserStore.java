package com.ahnlab.edr.sample.out.clickhouse.store;

import com.ahnlab.edr.sample.config.ClickHouseOutboundEnabled;
import com.ahnlab.edr.sample.core.domain.entity.UserEntity;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Shared in-memory store for ClickHouse user data.
 * Both the command adapter and query adapter reference this single store
 * to maintain consistency between writes and reads.
 */
@Component
@ClickHouseOutboundEnabled
public class ClickHouseUserStore {

	private final Map<String, UserEntity> store = new ConcurrentHashMap<>();

	public void put(String id, UserEntity entity) {
		store.put(id, entity);
	}

	public Optional<UserEntity> get(String id) {
		return Optional.ofNullable(store.get(id));
	}
}
