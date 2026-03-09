package com.ahnlab.edr.sample.in.grpc.command;

import com.ahnlab.edr.sample.core.application.command.port.in.EventCommandUseCase;
import com.ahnlab.edr.sample.core.domain.vo.EventVO;
import com.ahnlab.edr.sample.in.grpc.dto.GrpcEventRequest;
import com.ahnlab.edr.sample.in.grpc.mapper.GrpcEventMapper;
import junit.framework.TestCase;
import org.mapstruct.factory.Mappers;

/**
 * Unit tests for {@link GrpcEventCommandFacade}.
 */
public class GrpcEventCommandFacadeTest extends TestCase {

	public void testSaveDelegatesToCommandUseCase() {
		RecordingEventCommandUseCase commandUseCase = new RecordingEventCommandUseCase();
		GrpcEventMapper mapper = Mappers.getMapper(GrpcEventMapper.class);
		GrpcEventCommandFacade facade = new GrpcEventCommandFacade(commandUseCase, mapper);

		GrpcEventRequest request = new GrpcEventRequest("id-1", "hello");
		facade.save(request);

		assertNotNull(commandUseCase.lastSavedVO);
		assertEquals("id-1", commandUseCase.lastSavedVO.id());
		assertEquals("hello", commandUseCase.lastSavedVO.message());
	}

	private static class RecordingEventCommandUseCase implements EventCommandUseCase {

		EventVO lastSavedVO;

		@Override
		public void saveEvent(EventVO eventVO) {
			this.lastSavedVO = eventVO;
		}
	}
}
