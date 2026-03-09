package com.ahnlab.edr.sample.out.clickhouse.query;

import com.ahnlab.edr.sample.core.domain.entity.UserEntity;
import com.ahnlab.edr.sample.out.clickhouse.command.ClickHouseUserCommandAdapter;
import com.ahnlab.edr.sample.out.clickhouse.store.ClickHouseUserStore;
import java.util.Optional;
import junit.framework.TestCase;

/**
 * Unit tests for {@link ClickHouseUserQueryAdapter}.
 * Verifies that the query adapter reads from the same store written by the command adapter,
 * confirming correct CQRS separation through the shared {@link ClickHouseUserStore}.
 */
public class ClickHouseUserQueryAdapterTest extends TestCase {

	public void testFindByIdReturnsPresentWhenCommandAdapterSaved() {
		ClickHouseUserStore store = new ClickHouseUserStore();
		ClickHouseUserCommandAdapter commandAdapter = new ClickHouseUserCommandAdapter(store);
		ClickHouseUserQueryAdapter queryAdapter = new ClickHouseUserQueryAdapter(store);
		UserEntity entity = new UserEntity("user-1", "Charlie");

		commandAdapter.save(entity);
		Optional<UserEntity> result = queryAdapter.findById("user-1");

		assertTrue(result.isPresent());
		assertEquals("user-1", result.get().getId());
		assertEquals("Charlie", result.get().getName());
	}

	public void testFindByIdReturnsEmptyWhenNotSaved() {
		ClickHouseUserStore store = new ClickHouseUserStore();
		ClickHouseUserQueryAdapter queryAdapter = new ClickHouseUserQueryAdapter(store);

		Optional<UserEntity> result = queryAdapter.findById("missing");

		assertFalse(result.isPresent());
	}
}
