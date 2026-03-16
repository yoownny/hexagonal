package com.ahnlab.edr.sample.out.opensearch.store;

import com.ahnlab.edr.sample.config.OpenSearchOutboundEnabled;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OpenSearch 저장소 클라이언트 래퍼.
 * Command/Query Adapter가 공유하는 인메모리 저장소 (실제 구현 시 OpenSearch 클라이언트로 교체).
 */
@Component
@OpenSearchOutboundEnabled
@Slf4j
public class OpenSearchEventStore {

    private final Map<String, EventEntity> store = new ConcurrentHashMap<>();

    /**
     * 이벤트 엔티티를 저장한다.
     *
     * @param id     이벤트 식별자
     * @param entity 이벤트 엔티티
     */
    public void put(String id, EventEntity entity) {
        log.debug("OpenSearch put: id={}", id);
        store.put(id, entity);
    }

    /**
     * ID로 이벤트 엔티티를 조회한다.
     *
     * @param id 이벤트 식별자
     * @return 이벤트 엔티티 (없으면 empty)
     */
    public Optional<EventEntity> get(String id) {
        log.debug("OpenSearch get: id={}", id);
        return Optional.ofNullable(store.get(id));
    }
}
