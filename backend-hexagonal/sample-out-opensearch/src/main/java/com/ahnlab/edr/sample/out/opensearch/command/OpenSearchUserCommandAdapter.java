package com.ahnlab.edr.sample.out.opensearch.command;

import com.ahnlab.edr.sample.config.OpenSearchOutboundEnabled;
import com.ahnlab.edr.sample.core.application.command.port.out.UserCommandStorePort;
import com.ahnlab.edr.sample.core.domain.entity.UserEntity;
import com.ahnlab.edr.sample.out.opensearch.store.OpenSearchUserStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Command-side adapter that persists users to the shared OpenSearch user store.
 * Implements only {@link UserCommandStorePort} (write responsibility).
 */
@Component
@OpenSearchOutboundEnabled
@RequiredArgsConstructor
public class OpenSearchUserCommandAdapter implements UserCommandStorePort {

	private final OpenSearchUserStore userStore;

	@Override
	public void save(UserEntity entity) {
		System.out.println("[OS-Command] OpenSearchUserCommandAdapter.save - id=" + entity.getId());
		userStore.put(entity.getId(), entity);
	}
}
