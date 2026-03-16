package com.ahnlab.edr.sample.in.http.mapper.query;

import com.ahnlab.edr.sample.config.HttpInboundEnabled;
import com.ahnlab.edr.sample.core.domain.vo.EventVO;
import com.ahnlab.edr.sample.in.http.dto.query.EventResponse;
import org.springframework.stereotype.Component;

/**
 * EventVO → EventResponse 변환 Mapper.
 */
@Component
@HttpInboundEnabled
public class EventQueryMapper {

    /**
     * 이벤트 VO를 HTTP 응답 DTO로 변환한다.
     *
     * @param eventVO 이벤트 VO
     * @return HTTP 응답 DTO
     */
    public EventResponse toResponse(EventVO eventVO) {
        EventResponse response = new EventResponse();
        response.setId(eventVO.id());
        response.setMessage(eventVO.message());
        return response;
    }
}
