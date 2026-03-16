package com.ahnlab.edr.sample.in.grpc.mapper.command;

import com.ahnlab.edr.sample.config.GrpcInboundEnabled;
import com.ahnlab.edr.sample.core.domain.vo.EventVO;
import org.springframework.stereotype.Component;

/**
 * gRPC 메시지 파라미터 → EventVO 변환 Mapper.
 */
@Component
@GrpcInboundEnabled
public class GrpcEventCommandMapper {

    /**
     * gRPC 메시지 필드로부터 이벤트 VO를 생성한다.
     *
     * @param id      이벤트 식별자
     * @param message 이벤트 메시지
     * @return 이벤트 VO
     */
    public EventVO toVO(String id, String message) {
        return new EventVO(id, message);
    }
}
