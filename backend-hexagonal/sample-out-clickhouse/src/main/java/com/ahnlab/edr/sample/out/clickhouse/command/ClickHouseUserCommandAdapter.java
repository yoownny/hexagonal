package com.ahnlab.edr.sample.out.clickhouse.command;

import com.ahnlab.edr.sample.config.ClickHouseOutboundEnabled;
import com.ahnlab.edr.sample.core.application.command.port.out.UserCommandStorePort;
import com.ahnlab.edr.sample.core.domain.entity.UserEntity;
import com.ahnlab.edr.sample.out.clickhouse.store.ClickHouseUserStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Command-side adapter that persists users to the shared ClickHouse user store.
 * Implements only {@link UserCommandStorePort} (write responsibility).
 */
@Component
@ClickHouseOutboundEnabled
@RequiredArgsConstructor
public class ClickHouseUserCommandAdapter implements UserCommandStorePort {

	private final ClickHouseUserStore userStore;

	@Override
	public void save(UserEntity entity) {
		System.out.println("[CH-Command] ClickHouseUserCommandAdapter.save - id=" + entity.getId());
		userStore.put(entity.getId(), entity);
	}
}
