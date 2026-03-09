package com.ahnlab.edr.sample.core.application.mapper;

import com.ahnlab.edr.sample.core.domain.entity.EventEntity;
import com.ahnlab.edr.sample.core.domain.vo.EventVO;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-09T21:43:35+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.1 (Oracle Corporation)"
)
@Component
public class EventMapperImpl implements EventMapper {

    @Override
    public EventEntity toEntity(EventVO vo) {
        if ( vo == null ) {
            return null;
        }

        String id = null;
        String message = null;

        id = vo.id();
        message = vo.message();

        EventEntity eventEntity = new EventEntity( id, message );

        return eventEntity;
    }

    @Override
    public EventVO toVO(EventEntity entity) {
        if ( entity == null ) {
            return null;
        }

        String id = null;
        String message = null;

        id = entity.getId();
        message = entity.getMessage();

        EventVO eventVO = new EventVO( id, message );

        return eventVO;
    }
}
