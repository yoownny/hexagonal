package com.ahnlab.edr.sample.core.application.query.port.out;

import com.ahnlab.edr.sample.core.domain.entity.UserEntity;
import java.util.Optional;

/**
 * Query-side outbound port for loading users.
 */
public interface UserQueryStorePort {

	Optional<UserEntity> findById(String id);
}
