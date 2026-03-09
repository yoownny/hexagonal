package com.ahnlab.edr.sample.out.clickhouse.command;

import com.ahnlab.edr.sample.core.domain.entity.EventEntity;
import com.ahnlab.edr.sample.out.clickhouse.store.ClickHouseEventStore;
import java.util.Optional;
import junit.framework.TestCase;

/**
 * Unit tests for {@link ClickHouseEventCommandAdapter}.
 */
public class ClickHouseEventCommandAdapterTest extends TestCase {

	public void testSavePutsEntityIntoStore() {
		ClickHouseEventStore store = new ClickHouseEventStore();
		ClickHouseEventCommandAdapter adapter = new ClickHouseEventCommandAdapter(store);
		EventEntity entity = new EventEntity("evt-1", "some-message");

		adapter.save(entity);

		Optional<EventEntity> result = store.get("evt-1");
		assertTrue(result.isPresent());
		assertEquals("evt-1", result.get().getId());
		assertEquals("some-message", result.get().getMessage());
	}
}
