package com.ahnlab.edr.sample.in.grpc.query;

import com.ahnlab.edr.sample.config.GrpcInboundEnabled;
import com.ahnlab.edr.sample.core.application.query.port.in.EventQueryUseCase;
import com.ahnlab.edr.sample.core.domain.vo.EventVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * gRPC 이벤트 Query Facade.
 * gRPC Service 구현체가 위임하는 비즈니스 호출 클래스.
 */
@Component
@GrpcInboundEnabled
@Slf4j
@RequiredArgsConstructor
public class GrpcEventQueryFacade {

    private final EventQueryUseCase eventQueryUseCase;

    /**
     * ID로 이벤트를 조회한다.
     *
     * @param id 이벤트 식별자
     * @return 이벤트 VO (없으면 empty)
     */
    public Optional<EventVO> getEvent(String id) {
        log.debug("gRPC get event: {}", id);
        return eventQueryUseCase.getEvent(id);
    }
}
