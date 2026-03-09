package com.ahnlab.edr.sample.in.grpc.command;

import com.ahnlab.edr.sample.core.application.command.port.in.EventCommandUseCase;
import com.ahnlab.edr.sample.core.domain.vo.EventVO;
import com.ahnlab.edr.sample.in.grpc.dto.GrpcEventRequest;
import com.ahnlab.edr.sample.in.grpc.mapper.GrpcEventMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * gRPC command-side facade for event use cases.
 * Handles write operations only (save).
 */
@Service
public class GrpcEventCommandFacade {

	private static final Logger LOGGER = LoggerFactory.getLogger(GrpcEventCommandFacade.class);

	private final EventCommandUseCase eventCommandUseCase;
	private final GrpcEventMapper grpcEventMapper;

	public GrpcEventCommandFacade(EventCommandUseCase eventCommandUseCase, GrpcEventMapper grpcEventMapper) {
		this.eventCommandUseCase = eventCommandUseCase;
		this.grpcEventMapper = grpcEventMapper;
	}

	public void save(GrpcEventRequest request) {
		LOGGER.info("gRPC command save called. id={}, message={}", request.getId(), request.getMessage());
		EventVO eventVO = grpcEventMapper.toVO(request);
		eventCommandUseCase.saveEvent(eventVO);
	}
}
