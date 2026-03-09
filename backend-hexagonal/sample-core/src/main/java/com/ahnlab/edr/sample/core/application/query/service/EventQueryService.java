package com.ahnlab.edr.sample.core.application.query.service;

import com.ahnlab.edr.sample.core.application.exception.event.EventErrorCode;
import com.ahnlab.edr.sample.core.application.exception.event.EventException;
import com.ahnlab.edr.sample.core.application.mapper.EventMapper;
import com.ahnlab.edr.sample.core.application.query.port.in.EventQueryUseCase;
import com.ahnlab.edr.sample.core.application.query.port.out.EventQueryStorePort;
import com.ahnlab.edr.sample.core.domain.vo.EventVO;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Query-side application service for events.
 * Converts between VO (domain) and Entity (persistence).
 */
@Service
public class EventQueryService implements EventQueryUseCase {

	private final EventQueryStorePort eventStorePort;
	private final EventMapper eventMapper;

	@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
	public EventQueryService(EventQueryStorePort eventStorePort, EventMapper eventMapper) {
		this.eventStorePort = eventStorePort;
		this.eventMapper = eventMapper;
	}

	@Override
	public Optional<EventVO> getEvent(String id) {
		if (id == null || id.isBlank()) {
			throw new EventException(EventErrorCode.EVENT_ID_REQUIRED);
		}
		
		try {
			System.out.println("[CORE] EventQueryService.getEvent - id=" + id);
			Optional<EventVO> result = eventStorePort.findById(id)
					.map(eventMapper::toVO);
			System.out.println("[CORE] EventQueryService.getEvent - found? " + result.isPresent());
			
			if (result.isEmpty()) {
				throw new EventException(EventErrorCode.EVENT_NOT_FOUND, id);
			}
			
			return result;
		} catch (EventException e) {
			throw e;
		} catch (Exception e) {
			throw new EventException(EventErrorCode.EVENT_QUERY_FAILED, e, id);
		}
	}
}
