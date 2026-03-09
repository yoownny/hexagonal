package com.ahnlab.edr.sample.core.application.command.port.out;

import com.ahnlab.edr.sample.core.domain.entity.UserEntity;

/**
 * Command-side outbound port for storing users.
 */
public interface UserCommandStorePort {

	void save(UserEntity entity);
}
