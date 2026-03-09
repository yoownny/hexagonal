package com.ahnlab.edr.sample.out.opensearch.query;

import com.ahnlab.edr.sample.core.domain.entity.EventEntity;
import com.ahnlab.edr.sample.out.opensearch.command.OpenSearchEventCommandAdapter;
import com.ahnlab.edr.sample.out.opensearch.store.OpenSearchEventStore;
import java.util.Optional;
import junit.framework.TestCase;

/**
 * Unit tests for {@link OpenSearchEventQueryAdapter}.
 * Verifies that the query adapter reads from the same store written by the command adapter,
 * confirming correct CQRS separation through the shared {@link OpenSearchEventStore}.
 */
public class OpenSearchEventQueryAdapterTest extends TestCase {

	public void testFindByIdReturnsPresentWhenCommandAdapterSaved() {
		OpenSearchEventStore store = new OpenSearchEventStore();
		OpenSearchEventCommandAdapter commandAdapter = new OpenSearchEventCommandAdapter(store);
		OpenSearchEventQueryAdapter queryAdapter = new OpenSearchEventQueryAdapter(store);
		EventEntity entity = new EventEntity("evt-1", "hello");

		commandAdapter.save(entity);
		Optional<EventEntity> result = queryAdapter.findById("evt-1");

		assertTrue(result.isPresent());
		assertEquals("evt-1", result.get().getId());
		assertEquals("hello", result.get().getMessage());
	}

	public void testFindByIdReturnsEmptyWhenNotSaved() {
		OpenSearchEventStore store = new OpenSearchEventStore();
		OpenSearchEventQueryAdapter queryAdapter = new OpenSearchEventQueryAdapter(store);

		Optional<EventEntity> result = queryAdapter.findById("missing");

		assertFalse(result.isPresent());
	}
}
