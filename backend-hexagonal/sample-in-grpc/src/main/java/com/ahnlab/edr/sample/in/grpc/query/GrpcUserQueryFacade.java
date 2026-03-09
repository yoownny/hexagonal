package com.ahnlab.edr.sample.in.grpc.query;

import com.ahnlab.edr.sample.core.application.query.port.in.UserQueryUseCase;
import com.ahnlab.edr.sample.core.domain.vo.UserVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * gRPC query-side facade for user use cases.
 * Handles read operations only (get).
 */
@Service
public class GrpcUserQueryFacade {

	private static final Logger LOGGER = LoggerFactory.getLogger(GrpcUserQueryFacade.class);

	private final UserQueryUseCase userQueryUseCase;

	public GrpcUserQueryFacade(UserQueryUseCase userQueryUseCase) {
		this.userQueryUseCase = userQueryUseCase;
		LOGGER.info("GrpcUserQueryFacade bean created");
	}

	public UserVO get(String id) {
		LOGGER.info("gRPC query user get called. id={}", id);
		return userQueryUseCase.getUser(id).orElse(null);
	}
}
