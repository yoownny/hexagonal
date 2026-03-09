package com.ahnlab.edr.sample.core.application.query.port.out;

import com.ahnlab.edr.sample.core.domain.entity.EventEntity;
import java.util.Optional;

/**
 * Query-side outbound port for loading events.
 */
public interface EventQueryStorePort {

	Optional<EventEntity> findById(String id);
}
