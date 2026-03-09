package com.ahnlab.edr.sample.out.clickhouse.query;

import com.ahnlab.edr.sample.config.ClickHouseOutboundEnabled;
import com.ahnlab.edr.sample.core.application.query.port.out.UserQueryStorePort;
import com.ahnlab.edr.sample.core.domain.entity.UserEntity;
import com.ahnlab.edr.sample.out.clickhouse.store.ClickHouseUserStore;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Query-side adapter that retrieves users from the shared ClickHouse user store.
 * Implements only {@link UserQueryStorePort} (read responsibility).
 */
@Component
@ClickHouseOutboundEnabled
@RequiredArgsConstructor
public class ClickHouseUserQueryAdapter implements UserQueryStorePort {

	private final ClickHouseUserStore userStore;

	@Override
	public Optional<UserEntity> findById(String id) {
		System.out.println("[CH-Query] ClickHouseUserQueryAdapter.findById - id=" + id);
		Optional<UserEntity> result = userStore.get(id);
		System.out.println("[CH-Query] ClickHouseUserQueryAdapter.findById - found? " + result.isPresent());
		return result;
	}
}
