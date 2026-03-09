package com.ahnlab.edr.sample.core.application.query.service;

import com.ahnlab.edr.sample.core.application.exception.user.UserErrorCode;
import com.ahnlab.edr.sample.core.application.exception.user.UserException;
import com.ahnlab.edr.sample.core.application.mapper.UserMapper;
import com.ahnlab.edr.sample.core.application.query.port.in.UserQueryUseCase;
import com.ahnlab.edr.sample.core.application.query.port.out.UserQueryStorePort;
import com.ahnlab.edr.sample.core.domain.vo.UserVO;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Query-side application service for users.
 * Converts between VO (domain) and Entity (persistence).
 */
@Service
public class UserQueryService implements UserQueryUseCase {

	private final UserQueryStorePort userStorePort;
	private final UserMapper userMapper;

	@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
	public UserQueryService(UserQueryStorePort userStorePort, UserMapper userMapper) {
		this.userStorePort = userStorePort;
		this.userMapper = userMapper;
	}

	@Override
	public Optional<UserVO> getUser(String id) {
		if (id == null || id.isBlank()) {
			throw new UserException(UserErrorCode.USER_ID_REQUIRED);
		}
		
		try {
			System.out.println("[CORE] UserQueryService.getUser - id=" + id);
			Optional<UserVO> result = userStorePort.findById(id)
					.map(userMapper::toVO);
			System.out.println("[CORE] UserQueryService.getUser - found? " + result.isPresent());
			
			if (result.isEmpty()) {
				throw new UserException(UserErrorCode.USER_NOT_FOUND, id);
			}
			
			return result;
		} catch (UserException e) {
			throw e;
		} catch (Exception e) {
			throw new UserException(UserErrorCode.USER_QUERY_FAILED, e, id);
		}
	}
}
