package com.ahnlab.edr.sample.in.grpc.command;

import com.ahnlab.edr.sample.core.application.command.port.in.UserCommandUseCase;
import com.ahnlab.edr.sample.core.domain.vo.UserVO;
import com.ahnlab.edr.sample.in.grpc.dto.GrpcUserRequest;
import com.ahnlab.edr.sample.in.grpc.mapper.GrpcUserMapper;
import junit.framework.TestCase;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for {@link GrpcUserCommandFacade}.
 */
public class GrpcUserCommandFacadeTest extends TestCase {

	public void testSaveDelegatesToCommandUseCase() {
		RecordingUserCommandUseCase commandUseCase = new RecordingUserCommandUseCase();
		GrpcUserMapper mapper = Mappers.getMapper(GrpcUserMapper.class);
		GrpcUserCommandFacade facade = new GrpcUserCommandFacade(commandUseCase, mapper);

		GrpcUserRequest request = new GrpcUserRequest("u1", "Alice");
		facade.save(request);

		assertEquals(1, commandUseCase.saved.size());
		assertEquals("u1", commandUseCase.saved.get(0).id());
		assertEquals("Alice", commandUseCase.saved.get(0).name());
	}

	private static class RecordingUserCommandUseCase implements UserCommandUseCase {

		final List<UserVO> saved = new ArrayList<>();

		@Override
		public void saveUser(UserVO userVO) {
			saved.add(userVO);
		}
	}
}
