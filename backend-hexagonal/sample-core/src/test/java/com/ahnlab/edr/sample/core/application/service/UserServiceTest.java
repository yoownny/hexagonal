package com.ahnlab.edr.sample.core.application.service;

import java.util.Optional;

import com.ahnlab.edr.sample.core.application.command.port.out.UserCommandStorePort;
import com.ahnlab.edr.sample.core.application.command.service.UserCommandService;
import com.ahnlab.edr.sample.core.application.mapper.UserMapper;
import com.ahnlab.edr.sample.core.application.query.port.out.UserQueryStorePort;
import com.ahnlab.edr.sample.core.application.query.service.UserQueryService;
import com.ahnlab.edr.sample.core.domain.entity.UserEntity;
import com.ahnlab.edr.sample.core.domain.vo.UserVO;
import junit.framework.TestCase;
import org.mapstruct.factory.Mappers;

/**
 * Unit tests for CQRS user services using the existing JUnit 3
 * dependency configured in the module.
 */
public class UserServiceTest extends TestCase {

	public void testSaveAndGetUser() {
		InMemoryUserStorePort userStorePort = new InMemoryUserStorePort();
		UserMapper mapper = Mappers.getMapper(UserMapper.class);
		UserCommandService commandService = new UserCommandService(userStorePort, mapper);
		UserQueryService queryService = new UserQueryService(userStorePort, mapper);

		UserVO userVO = new UserVO("user-1", "Alice");
		commandService.saveUser(userVO);
		Optional<UserVO> result = queryService.getUser("user-1");

		assertTrue(result.isPresent());
		assertEquals("user-1", result.get().id());
		assertEquals("Alice", result.get().name());
	}

	public void testGetUserNotFound() {
		InMemoryUserStorePort userStorePort = new InMemoryUserStorePort();
		UserMapper mapper = Mappers.getMapper(UserMapper.class);
		UserQueryService queryService = new UserQueryService(userStorePort, mapper);

		Optional<UserVO> result = queryService.getUser("missing");
		assertFalse(result.isPresent());
	}

	private static class InMemoryUserStorePort implements UserCommandStorePort, UserQueryStorePort {

		private UserEntity stored;

		@Override
		public void save(UserEntity entity) {
			stored = entity;
		}

		@Override
		public Optional<UserEntity> findById(String id) {
			if (stored != null && stored.getId().equals(id)) {
				return Optional.of(stored);
			}
			return Optional.empty();
		}
	}
}
