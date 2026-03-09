package com.ahnlab.edr.sample.in.grpc.mapper;

import com.ahnlab.edr.sample.core.domain.vo.EventVO;
import com.ahnlab.edr.sample.in.grpc.dto.GrpcEventRequest;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-09T21:43:42+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.1 (Oracle Corporation)"
)
@Component
public class GrpcEventMapperImpl implements GrpcEventMapper {

    @Override
    public EventVO toVO(GrpcEventRequest request) {
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
}
