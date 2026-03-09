package com.ahnlab.edr.sample.in.grpc.query;

import com.ahnlab.edr.sample.core.application.query.port.in.UserQueryUseCase;
import com.ahnlab.edr.sample.core.domain.vo.UserVO;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Unit tests for {@link GrpcUserQueryFacade}.
 */
public class GrpcUserQueryFacadeTest extends TestCase {

	public void testGetDelegatesToQueryUseCase() {
		RecordingUserQueryUseCase queryUseCase = new RecordingUserQueryUseCase();
		queryUseCase.toReturn = Optional.of(new UserVO("u2", "Bob"));
		GrpcUserQueryFacade facade = new GrpcUserQueryFacade(queryUseCase);

		UserVO result = facade.get("u2");

		assertNotNull(result);
		assertEquals("u2", result.id());
		assertEquals("Bob", result.name());
		assertEquals(1, queryUseCase.requestedIds.size());
		assertEquals("u2", queryUseCase.requestedIds.get(0));
	}

	public void testGetReturnsNullWhenNotFound() {
		RecordingUserQueryUseCase queryUseCase = new RecordingUserQueryUseCase();
		GrpcUserQueryFacade facade = new GrpcUserQueryFacade(queryUseCase);

		UserVO result = facade.get("missing");

		assertNull(result);
	}

	private static class RecordingUserQueryUseCase implements UserQueryUseCase {

		final List<String> requestedIds = new ArrayList<>();
		Optional<UserVO> toReturn = Optional.empty();

		@Override
		public Optional<UserVO> getUser(String id) {
			requestedIds.add(id);
			return toReturn;
		}
	}
}
