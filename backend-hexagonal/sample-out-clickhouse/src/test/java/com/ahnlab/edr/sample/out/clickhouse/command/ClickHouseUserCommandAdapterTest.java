package com.ahnlab.edr.sample.out.clickhouse.command;

import com.ahnlab.edr.sample.core.domain.entity.UserEntity;
import com.ahnlab.edr.sample.out.clickhouse.store.ClickHouseUserStore;
import java.util.Optional;
import junit.framework.TestCase;

/**
 * Unit tests for {@link ClickHouseUserCommandAdapter}.
 */
public class ClickHouseUserCommandAdapterTest extends TestCase {

	public void testSavePutsEntityIntoStore() {
		ClickHouseUserStore store = new ClickHouseUserStore();
		ClickHouseUserCommandAdapter adapter = new ClickHouseUserCommandAdapter(store);
		UserEntity entity = new UserEntity("user-1", "Alice");

		adapter.save(entity);

		Optional<UserEntity> result = store.get("user-1");
		assertTrue(result.isPresent());
		assertEquals("user-1", result.get().getId());
		assertEquals("Alice", result.get().getName());
	}
}
