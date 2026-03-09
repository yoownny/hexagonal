package com.ahnlab.edr.sample.out.clickhouse.query;

import com.ahnlab.edr.sample.core.domain.entity.EventEntity;
import com.ahnlab.edr.sample.out.clickhouse.command.ClickHouseEventCommandAdapter;
import com.ahnlab.edr.sample.out.clickhouse.store.ClickHouseEventStore;
import java.util.Optional;
import junit.framework.TestCase;

/**
 * Unit tests for {@link ClickHouseEventQueryAdapter}.
 * Verifies that the query adapter reads from the same store written by the command adapter,
 * confirming correct CQRS separation through the shared {@link ClickHouseEventStore}.
 */
public class ClickHouseEventQueryAdapterTest extends TestCase {

	public void testFindByIdReturnsPresentWhenCommandAdapterSaved() {
		ClickHouseEventStore store = new ClickHouseEventStore();
		ClickHouseEventCommandAdapter commandAdapter = new ClickHouseEventCommandAdapter(store);
		ClickHouseEventQueryAdapter queryAdapter = new ClickHouseEventQueryAdapter(store);
		EventEntity entity = new EventEntity("evt-1", "hello");

		commandAdapter.save(entity);
		Optional<EventEntity> result = queryAdapter.findById("evt-1");

		assertTrue(result.isPresent());
		assertEquals("evt-1", result.get().getId());
		assertEquals("hello", result.get().getMessage());
	}

	public void testFindByIdReturnsEmptyWhenNotSaved() {
		ClickHouseEventStore store = new ClickHouseEventStore();
		ClickHouseEventQueryAdapter queryAdapter = new ClickHouseEventQueryAdapter(store);

		Optional<EventEntity> result = queryAdapter.findById("missing");

		assertFalse(result.isPresent());
	}
}
