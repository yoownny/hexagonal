package com.ahnlab.edr.sample.out.opensearch.command;

import com.ahnlab.edr.sample.core.domain.entity.EventEntity;
import com.ahnlab.edr.sample.out.opensearch.store.OpenSearchEventStore;
import java.util.Optional;
import junit.framework.TestCase;

/**
 * Unit tests for {@link OpenSearchEventCommandAdapter}.
 */
public class OpenSearchEventCommandAdapterTest extends TestCase {

	public void testSavePutsEntityIntoStore() {
		OpenSearchEventStore store = new OpenSearchEventStore();
		OpenSearchEventCommandAdapter adapter = new OpenSearchEventCommandAdapter(store);
		EventEntity entity = new EventEntity("evt-1", "some-message");

		adapter.save(entity);

		Optional<EventEntity> result = store.get("evt-1");
		assertTrue(result.isPresent());
		assertEquals("evt-1", result.get().getId());
		assertEquals("some-message", result.get().getMessage());
	}
}
