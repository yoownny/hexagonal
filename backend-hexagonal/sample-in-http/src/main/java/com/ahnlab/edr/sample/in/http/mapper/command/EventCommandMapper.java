package com.ahnlab.edr.sample.in.http.mapper.command;

import com.ahnlab.edr.sample.config.HttpInboundEnabled;
import com.ahnlab.edr.sample.core.domain.vo.EventVO;
import com.ahnlab.edr.sample.in.http.dto.command.EventRequest;
import org.springframework.stereotype.Component;

/**
 * EventRequest → EventVO 변환 Mapper.
 */
@Component
@HttpInboundEnabled
public class EventCommandMapper {

    /**
     * HTTP 요청 DTO를 이벤트 VO로 변환한다.
     *
     * @param request HTTP 요청 DTO
     * @return 이벤트 VO
     */
    public EventVO toVO(EventRequest request) {
        return new EventVO(request.getId(), request.getMessage());
    }
}
