package com.ahnlab.edr.sample.out.clickhouse.mapper;

import com.ahnlab.edr.sample.config.ClickHouseOutboundEnabled;
import com.ahnlab.edr.sample.core.domain.vo.EventVO;
import com.ahnlab.edr.sample.out.clickhouse.store.EventEntity;
import org.springframework.stereotype.Component;

/**
 * EventVO ↔ EventEntity 변환 Mapper (ClickHouse 전용).
 */
@Component
@ClickHouseOutboundEnabled
public class EventEntityMapper {

    /**
     * 이벤트 VO를 ClickHouse 엔티티로 변환한다.
     *
     * @param vo 이벤트 VO
     * @return ClickHouse 이벤트 엔티티
     */
    public EventEntity toEntity(EventVO vo) {
        EventEntity entity = new EventEntity();
        entity.setId(vo.id());
        entity.setMessage(vo.message());
        return entity;
    }

    /**
     * ClickHouse 엔티티를 이벤트 VO로 변환한다.
     *
     * @param entity ClickHouse 이벤트 엔티티
     * @return 이벤트 VO
     */
    public EventVO toVO(EventEntity entity) {
        return new EventVO(entity.getId(), entity.getMessage());
    }
}
