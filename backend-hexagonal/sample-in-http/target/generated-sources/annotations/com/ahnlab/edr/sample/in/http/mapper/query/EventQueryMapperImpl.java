package com.ahnlab.edr.sample.in.http.mapper.query;

import com.ahnlab.edr.sample.core.domain.vo.EventVO;
import com.ahnlab.edr.sample.in.http.dto.query.EventResponse;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-09T21:43:41+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.1 (Oracle Corporation)"
)
@Component
public class EventQueryMapperImpl implements EventQueryMapper {

    @Override
    public EventResponse toResponse(EventVO vo) {
        if ( vo == null ) {
            return null;
        }

        EventResponse eventResponse = new EventResponse();

        eventResponse.setId( vo.id() );
        eventResponse.setMessage( vo.message() );

        return eventResponse;
    }
}
