package com.ahnlab.edr.sample.out.clickhouse.store;

import com.ahnlab.edr.sample.config.ClickHouseOutboundEnabled;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ClickHouse 저장소 클라이언트 래퍼.
 * Command/Query Adapter가 공유하는 인메모리 저장소 (실제 구현 시 ClickHouse 클라이언트로 교체).
 */
@Component
@ClickHouseOutboundEnabled
@Slf4j
public class ClickHouseEventStore {

    private final Map<String, EventEntity> store = new ConcurrentHashMap<>();

    /**
     * 이벤트 엔티티를 저장한다.
     *
     * @param id     이벤트 식별자
     * @param entity 이벤트 엔티티
     */
    public void put(String id, EventEntity entity) {
        log.debug("ClickHouse put: id={}", id);
        store.put(id, entity);
    }

    /**
     * ID로 이벤트 엔티티를 조회한다.
     *
     * @param id 이벤트 식별자
     * @return 이벤트 엔티티 (없으면 empty)
     */
    public Optional<EventEntity> get(String id) {
        log.debug("ClickHouse get: id={}", id);
        return Optional.ofNullable(store.get(id));
    }
}
