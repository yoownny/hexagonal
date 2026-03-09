package com.ahnlab.edr.sample.core.application.command.port.out;

import com.ahnlab.edr.sample.core.domain.entity.EventEntity;

/**
 * Command-side outbound port for storing events.
 */
public interface EventCommandStorePort {

	void save(EventEntity entity);
}
