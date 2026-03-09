package com.ahnlab.edr.sample.out.opensearch.query;

import com.ahnlab.edr.sample.config.OpenSearchOutboundEnabled;
import com.ahnlab.edr.sample.core.application.query.port.out.UserQueryStorePort;
import com.ahnlab.edr.sample.core.domain.entity.UserEntity;
import com.ahnlab.edr.sample.out.opensearch.store.OpenSearchUserStore;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Query-side adapter that retrieves users from the shared OpenSearch user store.
 * Implements only {@link UserQueryStorePort} (read responsibility).
 */
@Component
@OpenSearchOutboundEnabled
@RequiredArgsConstructor
public class OpenSearchUserQueryAdapter implements UserQueryStorePort {

	private final OpenSearchUserStore userStore;

	@Override
	public Optional<UserEntity> findById(String id) {
		System.out.println("[OS-Query] OpenSearchUserQueryAdapter.findById - id=" + id);
		Optional<UserEntity> result = userStore.get(id);
		System.out.println("[OS-Query] OpenSearchUserQueryAdapter.findById - found? " + result.isPresent());
		return result;
	}
}
