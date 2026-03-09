package com.ahnlab.edr.sample.core.application.command.service;

import com.ahnlab.edr.sample.core.application.command.port.in.EventCommandUseCase;
import com.ahnlab.edr.sample.core.application.command.port.out.EventCommandStorePort;
import com.ahnlab.edr.sample.core.application.exception.event.EventErrorCode;
import com.ahnlab.edr.sample.core.application.exception.event.EventException;
import com.ahnlab.edr.sample.core.application.mapper.EventMapper;
import com.ahnlab.edr.sample.core.domain.entity.EventEntity;
import com.ahnlab.edr.sample.core.domain.vo.EventVO;
import org.springframework.stereotype.Service;

/**
 * Command-side application service for events.
 * Converts between VO (domain) and Entity (persistence).
 */
@Service
public class EventCommandService implements EventCommandUseCase {

	private final EventCommandStorePort eventStorePort;
	private final EventMapper eventMapper;

	@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
	public EventCommandService(EventCommandStorePort eventStorePort, EventMapper eventMapper) {
		this.eventStorePort = eventStorePort;
		this.eventMapper = eventMapper;
	}

	@Override
	public void saveEvent(EventVO eventVO) {
		if (eventVO == null) {
			throw new EventException(EventErrorCode.EVENT_INVALID_DATA, "event cannot be null");
		}
		
		if (eventVO.id() == null || eventVO.id().isBlank()) {
			throw new EventException(EventErrorCode.EVENT_ID_REQUIRED);
		}
		
		if (eventVO.message() == null || eventVO.message().isBlank()) {
			throw new EventException(EventErrorCode.EVENT_MESSAGE_REQUIRED);
		}
		
		try {
			System.out.println("[CORE] EventCommandService.saveEvent - id=" + eventVO.id() + ", message=" + eventVO.message());
			EventEntity entity = eventMapper.toEntity(eventVO);
			eventStorePort.save(entity);
			System.out.println("[CORE] EventCommandService.saveEvent - stored event with id=" + eventVO.id());
		} catch (Exception e) {
			if (e instanceof EventException) {
				throw (EventException) e;
			}
			throw new EventException(EventErrorCode.EVENT_SAVE_FAILED, e, eventVO.id());
		}
	}
}
