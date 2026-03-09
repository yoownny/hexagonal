package com.ahnlab.edr.sample.in.grpc.query;

import com.ahnlab.edr.sample.core.application.query.port.in.EventQueryUseCase;
import com.ahnlab.edr.sample.core.domain.vo.EventVO;
import junit.framework.TestCase;

import java.util.Optional;

/**
 * Unit tests for {@link GrpcEventQueryFacade}.
 */
public class GrpcEventQueryFacadeTest extends TestCase {

	public void testGetDelegatesToQueryUseCase() {
		RecordingEventQueryUseCase queryUseCase = new RecordingEventQueryUseCase();
		EventVO expected = new EventVO("id-1", "hello");
		queryUseCase.nextGetResult = Optional.of(expected);

		GrpcEventQueryFacade facade = new GrpcEventQueryFacade(queryUseCase);
		EventVO result = facade.get("id-1");

		assertNotNull(result);
		assertEquals("id-1", result.id());
		assertEquals("hello", result.message());
	}

	public void testGetReturnsNullWhenNotFound() {
		RecordingEventQueryUseCase queryUseCase = new RecordingEventQueryUseCase();
		GrpcEventQueryFacade facade = new GrpcEventQueryFacade(queryUseCase);

		EventVO result = facade.get("missing");

		assertNull(result);
	}

	private static class RecordingEventQueryUseCase implements EventQueryUseCase {

		Optional<EventVO> nextGetResult = Optional.empty();

		@Override
		public Optional<EventVO> getEvent(String id) {
			return nextGetResult;
		}
	}
}
