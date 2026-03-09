package com.ahnlab.edr.sample.core.application.query.port.in;

import com.ahnlab.edr.sample.core.domain.vo.EventVO;
import java.util.Optional;

/**
 * Query-side inbound port for loading events.
 */
public interface EventQueryUseCase {

	Optional<EventVO> getEvent(String id);
}
