package com.ahnlab.edr.sample.core.application.command.service;

import com.ahnlab.edr.sample.core.application.command.port.in.UserCommandUseCase;
import com.ahnlab.edr.sample.core.application.command.port.out.UserCommandStorePort;
import com.ahnlab.edr.sample.core.application.exception.user.UserErrorCode;
import com.ahnlab.edr.sample.core.application.exception.user.UserException;
import com.ahnlab.edr.sample.core.application.mapper.UserMapper;
import com.ahnlab.edr.sample.core.domain.entity.UserEntity;
import com.ahnlab.edr.sample.core.domain.vo.UserVO;
import org.springframework.stereotype.Service;

/**
 * Command-side application service for users.
 * Converts between VO (domain) and Entity (persistence).
 */
@Service
public class UserCommandService implements UserCommandUseCase {

	private final UserCommandStorePort userStorePort;
	private final UserMapper userMapper;

	@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
	public UserCommandService(UserCommandStorePort userStorePort, UserMapper userMapper) {
		this.userStorePort = userStorePort;
		this.userMapper = userMapper;
	}

	@Override
	public void saveUser(UserVO userVO) {
		if (userVO == null) {
			throw new UserException(UserErrorCode.USER_INVALID_DATA, "user cannot be null");
		}
		
		if (userVO.id() == null || userVO.id().isBlank()) {
			throw new UserException(UserErrorCode.USER_ID_REQUIRED);
		}
		
		if (userVO.name() == null || userVO.name().isBlank()) {
			throw new UserException(UserErrorCode.USER_NAME_REQUIRED);
		}
		
		try {
			System.out.println("[CORE] UserCommandService.saveUser - id=" + userVO.id() + ", name=" + userVO.name());
			UserEntity entity = userMapper.toEntity(userVO);
			userStorePort.save(entity);
			System.out.println("[CORE] UserCommandService.saveUser - stored user with id=" + userVO.id());
		} catch (Exception e) {
			if (e instanceof UserException) {
				throw (UserException) e;
			}
			throw new UserException(UserErrorCode.USER_SAVE_FAILED, e, userVO.id());
		}
	}
}
