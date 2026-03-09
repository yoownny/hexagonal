package com.ahnlab.edr.sample.in.http.mapper;

import com.ahnlab.edr.sample.core.domain.vo.EventVO;
import com.ahnlab.edr.sample.in.http.dto.EventRequest;
import com.ahnlab.edr.sample.in.http.dto.EventResponse;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-26T17:05:42+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.9 (Amazon.com Inc.)"
)
@Component
public class EventHttpMapperImpl implements EventHttpMapper {

    @Override
    public EventVO toVO(EventRequest request) {
        if ( request == null ) {
            return null;
        }

        String id = null;
        String message = null;

        id = request.getId();
        message = request.getMessage();

        EventVO eventVO = new EventVO( id, message );

        return eventVO;
    }

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
