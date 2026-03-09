package com.ahnlab.edr.sample.core.application.service;

import java.util.Optional;

import com.ahnlab.edr.sample.core.application.command.port.out.EventCommandStorePort;
import com.ahnlab.edr.sample.core.application.command.service.EventCommandService;
import com.ahnlab.edr.sample.core.application.mapper.EventMapper;
import com.ahnlab.edr.sample.core.application.query.port.out.EventQueryStorePort;
import com.ahnlab.edr.sample.core.application.query.service.EventQueryService;
import com.ahnlab.edr.sample.core.domain.entity.EventEntity;
import com.ahnlab.edr.sample.core.domain.vo.EventVO;
import junit.framework.TestCase;
import org.mapstruct.factory.Mappers;

/**
 * Unit tests for CQRS event services using the existing JUnit 3
 * dependency configured in the module.
 */
public class EventServiceTest extends TestCase {

	public void testSaveAndGetEvent() {
		InMemoryEventStorePort eventStorePort = new InMemoryEventStorePort();
		EventMapper mapper = Mappers.getMapper(EventMapper.class);
		EventCommandService commandService = new EventCommandService(eventStorePort, mapper);
		EventQueryService queryService = new EventQueryService(eventStorePort, mapper);

		EventVO eventVO = new EventVO("id-1", "hello");
		commandService.saveEvent(eventVO);
		Optional<EventVO> result = queryService.getEvent("id-1");

		assertTrue(result.isPresent());
		assertEquals("id-1", result.get().id());
		assertEquals("hello", result.get().message());
	}

	public void testGetEventNotFound() {
		InMemoryEventStorePort eventStorePort = new InMemoryEventStorePort();
		EventMapper mapper = Mappers.getMapper(EventMapper.class);
		EventQueryService queryService = new EventQueryService(eventStorePort, mapper);

		Optional<EventVO> result = queryService.getEvent("missing");
		assertFalse(result.isPresent());
	}

	private static class InMemoryEventStorePort implements EventCommandStorePort, EventQueryStorePort {

		private EventEntity stored;

		@Override
		public void save(EventEntity entity) {
			stored = entity;
		}

		@Override
		public Optional<EventEntity> findById(String id) {
			if (stored != null && stored.getId().equals(id)) {
				return Optional.of(stored);
			}
			return Optional.empty();
		}
	}
}
