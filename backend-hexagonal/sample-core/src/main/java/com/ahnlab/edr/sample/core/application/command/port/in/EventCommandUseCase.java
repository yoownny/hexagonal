package com.ahnlab.edr.sample.core.application.command.port.in;

import com.ahnlab.edr.sample.core.domain.vo.EventVO;

/**
 * 이벤트 Command UseCase (Inbound Port).
 * Inbound Adapter(HTTP Controller, gRPC Facade)가 호출하는 인터페이스.
 */
public interface EventCommandUseCase {

    /**
     * 이벤트를 저장한다.
     *
     * @param eventVO 저장할 이벤트 VO
     */
    void saveEvent(EventVO eventVO);
}
