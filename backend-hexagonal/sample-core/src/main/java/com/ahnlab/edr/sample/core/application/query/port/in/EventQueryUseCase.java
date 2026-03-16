package com.ahnlab.edr.sample.core.application.query.port.in;

import com.ahnlab.edr.sample.core.domain.vo.EventVO;

import java.util.Optional;

/**
 * 이벤트 Query UseCase (Inbound Port).
 * Inbound Adapter(HTTP Controller, gRPC Facade)가 호출하는 인터페이스.
 */
public interface EventQueryUseCase {

    /**
     * ID로 이벤트를 조회한다.
     *
     * @param id 이벤트 식별자
     * @return 이벤트 VO (없으면 empty)
     */
    Optional<EventVO> getEvent(String id);
}
