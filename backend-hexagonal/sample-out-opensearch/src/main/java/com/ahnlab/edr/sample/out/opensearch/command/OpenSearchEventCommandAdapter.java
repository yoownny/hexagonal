package com.ahnlab.edr.sample.out.opensearch.command;

import com.ahnlab.edr.sample.config.OpenSearchOutboundEnabled;
import com.ahnlab.edr.sample.core.application.command.port.out.EventCommandStorePort;
import com.ahnlab.edr.sample.core.domain.vo.EventVO;
import com.ahnlab.edr.sample.out.opensearch.mapper.EventEntityMapper;
import com.ahnlab.edr.sample.out.opensearch.store.EventEntity;
import com.ahnlab.edr.sample.out.opensearch.store.OpenSearchEventStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * OpenSearch Event Command Adapter.
 * VO를 받아 Entity로 변환 후 OpenSearch에 저장한다.
 */
@Component
@OpenSearchOutboundEnabled
@Slf4j
@RequiredArgsConstructor
public class OpenSearchEventCommandAdapter implements EventCommandStorePort {

    private final OpenSearchEventStore store;
    private final EventEntityMapper mapper;

    @Override
    public void save(EventVO eventVO) {
        try {
            EventEntity entity = mapper.toEntity(eventVO);
            store.put(entity.getId(), entity);
        } catch (RuntimeException e) {
            log.error("Failed to save event to OpenSearch: {}", eventVO.id(), e);
            throw new RuntimeException("Failed to save event: " + eventVO.id(), e);
        }
    }
}
