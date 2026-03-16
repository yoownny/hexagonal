package com.ahnlab.edr.sample.out.opensearch.query;

import com.ahnlab.edr.sample.config.OpenSearchOutboundEnabled;
import com.ahnlab.edr.sample.core.application.query.port.out.EventQueryStorePort;
import com.ahnlab.edr.sample.core.domain.vo.EventVO;
import com.ahnlab.edr.sample.out.opensearch.mapper.EventEntityMapper;
import com.ahnlab.edr.sample.out.opensearch.store.OpenSearchEventStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * OpenSearch Event Query Adapter.
 * OpenSearch에서 Entity를 조회 후 VO로 변환하여 반환한다.
 */
@Component
@OpenSearchOutboundEnabled
@Slf4j
@RequiredArgsConstructor
public class OpenSearchEventQueryAdapter implements EventQueryStorePort {

    private final OpenSearchEventStore store;
    private final EventEntityMapper mapper;

    @Override
    public Optional<EventVO> findById(String id) {
        try {
            return store.get(id).map(mapper::toVO);
        } catch (RuntimeException e) {
            log.error("Failed to query event from OpenSearch: {}", id, e);
            throw new RuntimeException("Failed to query event: " + id, e);
        }
    }
}
