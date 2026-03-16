package com.ahnlab.edr.sample.out.clickhouse.command;

import com.ahnlab.edr.sample.config.ClickHouseOutboundEnabled;
import com.ahnlab.edr.sample.core.application.command.port.out.EventCommandStorePort;
import com.ahnlab.edr.sample.core.domain.vo.EventVO;
import com.ahnlab.edr.sample.out.clickhouse.mapper.EventEntityMapper;
import com.ahnlab.edr.sample.out.clickhouse.store.ClickHouseEventStore;
import com.ahnlab.edr.sample.out.clickhouse.store.EventEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * ClickHouse Event Command Adapter.
 * VO를 받아 Entity로 변환 후 ClickHouse에 저장한다.
 */
@Component
@ClickHouseOutboundEnabled
@Slf4j
@RequiredArgsConstructor
public class ClickHouseEventCommandAdapter implements EventCommandStorePort {

    private final ClickHouseEventStore store;
    private final EventEntityMapper mapper;

    @Override
    public void save(EventVO eventVO) {
        try {
            EventEntity entity = mapper.toEntity(eventVO);
            store.put(entity.getId(), entity);
        } catch (RuntimeException e) {
            log.error("Failed to save event to ClickHouse: {}", eventVO.id(), e);
            throw new RuntimeException("Failed to save event: " + eventVO.id(), e);
        }
    }
}
