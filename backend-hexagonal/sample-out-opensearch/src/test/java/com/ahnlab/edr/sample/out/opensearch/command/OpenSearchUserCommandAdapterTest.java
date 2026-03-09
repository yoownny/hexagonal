package com.ahnlab.edr.sample.out.opensearch.command;

import com.ahnlab.edr.sample.core.domain.entity.UserEntity;
import com.ahnlab.edr.sample.out.opensearch.store.OpenSearchUserStore;
import java.util.Optional;
import junit.framework.TestCase;

/**
 * Unit tests for {@link OpenSearchUserCommandAdapter}.
 */
public class OpenSearchUserCommandAdapterTest extends TestCase {

	public void testSavePutsEntityIntoStore() {
		OpenSearchUserStore store = new OpenSearchUserStore();
		OpenSearchUserCommandAdapter adapter = new OpenSearchUserCommandAdapter(store);
		UserEntity entity = new UserEntity("user-1", "Alice");

		adapter.save(entity);

		Optional<UserEntity> result = store.get("user-1");
		assertTrue(result.isPresent());
		assertEquals("user-1", result.get().getId());
		assertEquals("Alice", result.get().getName());
	}
}
