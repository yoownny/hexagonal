package com.ahnlab.edr.sample.core.application.command.port.out;

import com.ahnlab.edr.sample.core.domain.vo.EventVO;

/**
 * 이벤트 Command StorePort (Outbound Port).
 * Outbound Adapter(OpenSearch, ClickHouse 등)가 구현하는 인터페이스.
 * VO를 받으며, Entity 변환은 Adapter 책임이다.
 */
public interface EventCommandStorePort {

    /**
     * 이벤트를 저장한다.
     *
     * @param eventVO 저장할 이벤트 VO
     */
    void save(EventVO eventVO);
}
