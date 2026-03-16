package com.ahnlab.edr.sample.in.grpc.command;

import com.ahnlab.edr.sample.config.GrpcInboundEnabled;
import com.ahnlab.edr.sample.core.application.command.port.in.EventCommandUseCase;
import com.ahnlab.edr.sample.core.domain.vo.EventVO;
import com.ahnlab.edr.sample.in.grpc.mapper.command.GrpcEventCommandMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * gRPC 이벤트 Command Facade.
 * gRPC Service 구현체가 위임하는 비즈니스 호출 클래스.
 */
@Component
@GrpcInboundEnabled
@Slf4j
@RequiredArgsConstructor
public class GrpcEventCommandFacade {

    private final EventCommandUseCase eventCommandUseCase;
    private final GrpcEventCommandMapper mapper;

    /**
     * gRPC 요청 맵으로부터 이벤트를 저장한다.
     *
     * @param id      이벤트 식별자
     * @param message 이벤트 메시지
     */
    public void save(String id, String message) {
        EventVO vo = mapper.toVO(id, message);
        log.debug("gRPC save event: {}", vo.id());
        eventCommandUseCase.saveEvent(vo);
    }
}
