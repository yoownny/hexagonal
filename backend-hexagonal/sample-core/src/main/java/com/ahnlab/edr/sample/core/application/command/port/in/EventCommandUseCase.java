package com.ahnlab.edr.sample.core.application.command.port.in;

import com.ahnlab.edr.sample.core.domain.vo.EventVO;

/**
 * Command-side inbound port for saving events.
 */
public interface EventCommandUseCase {

	void saveEvent(EventVO eventVO);
}
