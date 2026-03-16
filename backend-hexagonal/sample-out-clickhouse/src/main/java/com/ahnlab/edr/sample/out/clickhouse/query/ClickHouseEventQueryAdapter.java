package com.ahnlab.edr.sample.out.clickhouse.query;

import com.ahnlab.edr.sample.config.ClickHouseOutboundEnabled;
import com.ahnlab.edr.sample.core.application.query.port.out.EventQueryStorePort;
import com.ahnlab.edr.sample.core.domain.vo.EventVO;
import com.ahnlab.edr.sample.out.clickhouse.mapper.EventEntityMapper;
import com.ahnlab.edr.sample.out.clickhouse.store.ClickHouseEventStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * ClickHouse Event Query Adapter.
 * ClickHouse에서 Entity를 조회 후 VO로 변환하여 반환한다.
 */
@Component
@ClickHouseOutboundEnabled
@Slf4j
@RequiredArgsConstructor
public class ClickHouseEventQueryAdapter implements EventQueryStorePort {

    private final ClickHouseEventStore store;
    private final EventEntityMapper mapper;

    @Override
    public Optional<EventVO> findById(String id) {
        try {
            return store.get(id).map(mapper::toVO);
        } catch (RuntimeException e) {
            log.error("Failed to query event from ClickHouse: {}", id, e);
            throw new RuntimeException("Failed to query event: " + id, e);
        }
    }
}
