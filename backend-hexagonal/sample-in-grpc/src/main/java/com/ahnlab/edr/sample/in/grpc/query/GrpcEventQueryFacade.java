package com.ahnlab.edr.sample.in.grpc.query;

import com.ahnlab.edr.sample.core.application.query.port.in.EventQueryUseCase;
import com.ahnlab.edr.sample.core.domain.vo.EventVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * gRPC query-side facade for event use cases.
 * Handles read operations only (get).
 */
@Service
public class GrpcEventQueryFacade {

	private static final Logger LOGGER = LoggerFactory.getLogger(GrpcEventQueryFacade.class);

	private final EventQueryUseCase eventQueryUseCase;

	public GrpcEventQueryFacade(EventQueryUseCase eventQueryUseCase) {
		this.eventQueryUseCase = eventQueryUseCase;
	}

	public EventVO get(String id) {
		LOGGER.info("gRPC query get called. id={}", id);
		return eventQueryUseCase.getEvent(id).orElse(null);
	}
}
