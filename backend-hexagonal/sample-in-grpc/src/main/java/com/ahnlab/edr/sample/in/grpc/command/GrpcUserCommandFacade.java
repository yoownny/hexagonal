package com.ahnlab.edr.sample.in.grpc.command;

import com.ahnlab.edr.sample.core.application.command.port.in.UserCommandUseCase;
import com.ahnlab.edr.sample.core.domain.vo.UserVO;
import com.ahnlab.edr.sample.in.grpc.dto.GrpcUserRequest;
import com.ahnlab.edr.sample.in.grpc.mapper.GrpcUserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * gRPC command-side facade for user use cases.
 * Handles write operations only (save).
 */
@Service
public class GrpcUserCommandFacade {

	private static final Logger LOGGER = LoggerFactory.getLogger(GrpcUserCommandFacade.class);

	private final UserCommandUseCase userCommandUseCase;
	private final GrpcUserMapper grpcUserMapper;

	public GrpcUserCommandFacade(UserCommandUseCase userCommandUseCase, GrpcUserMapper grpcUserMapper) {
		this.userCommandUseCase = userCommandUseCase;
		this.grpcUserMapper = grpcUserMapper;
		LOGGER.info("GrpcUserCommandFacade bean created");
	}

	public void save(GrpcUserRequest request) {
		LOGGER.info("gRPC command user save called. id={}, name={}", request.getId(), request.getName());
		UserVO userVO = grpcUserMapper.toVO(request);
		userCommandUseCase.saveUser(userVO);
	}
}
