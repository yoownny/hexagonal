package com.ahnlab.edr.sample.out.opensearch.query;

import com.ahnlab.edr.sample.core.domain.entity.UserEntity;
import com.ahnlab.edr.sample.out.opensearch.command.OpenSearchUserCommandAdapter;
import com.ahnlab.edr.sample.out.opensearch.store.OpenSearchUserStore;
import java.util.Optional;
import junit.framework.TestCase;

/**
 * Unit tests for {@link OpenSearchUserQueryAdapter}.
 * Verifies that the query adapter reads from the same store written by the command adapter,
 * confirming correct CQRS separation through the shared {@link OpenSearchUserStore}.
 */
public class OpenSearchUserQueryAdapterTest extends TestCase {

	public void testFindByIdReturnsPresentWhenCommandAdapterSaved() {
		OpenSearchUserStore store = new OpenSearchUserStore();
		OpenSearchUserCommandAdapter commandAdapter = new OpenSearchUserCommandAdapter(store);
		OpenSearchUserQueryAdapter queryAdapter = new OpenSearchUserQueryAdapter(store);
		UserEntity entity = new UserEntity("user-1", "Bob");

		commandAdapter.save(entity);
		Optional<UserEntity> result = queryAdapter.findById("user-1");

		assertTrue(result.isPresent());
		assertEquals("user-1", result.get().getId());
		assertEquals("Bob", result.get().getName());
	}

	public void testFindByIdReturnsEmptyWhenNotSaved() {
		OpenSearchUserStore store = new OpenSearchUserStore();
		OpenSearchUserQueryAdapter queryAdapter = new OpenSearchUserQueryAdapter(store);

		Optional<UserEntity> result = queryAdapter.findById("missing");

		assertFalse(result.isPresent());
	}
}
