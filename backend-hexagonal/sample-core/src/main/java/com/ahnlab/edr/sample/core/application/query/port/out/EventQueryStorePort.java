package com.ahnlab.edr.sample.core.application.query.port.out;

import com.ahnlab.edr.sample.core.domain.vo.EventVO;

import java.util.Optional;

/**
 * 이벤트 Query StorePort (Outbound Port).
 * Outbound Adapter(OpenSearch, ClickHouse 등)가 구현하는 인터페이스.
 */
public interface EventQueryStorePort {

    /**
     * ID로 이벤트를 조회한다.
     *
     * @param id 이벤트 식별자
     * @return 이벤트 VO (없으면 empty)
     */
    Optional<EventVO> findById(String id);
}
