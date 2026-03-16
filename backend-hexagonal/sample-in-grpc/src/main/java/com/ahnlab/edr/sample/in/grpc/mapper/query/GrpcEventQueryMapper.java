package com.ahnlab.edr.sample.in.grpc.mapper.query;

import com.ahnlab.edr.sample.config.GrpcInboundEnabled;
import com.ahnlab.edr.sample.core.domain.vo.EventVO;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * EventVO → gRPC 응답 필드 변환 Mapper.
 */
@Component
@GrpcInboundEnabled
public class GrpcEventQueryMapper {

    /**
     * 이벤트 VO를 gRPC 응답용 Map으로 변환한다.
     * 실제 proto 생성 클래스로 교체할 것.
     *
     * @param eventVO 이벤트 VO
     * @return gRPC 응답 필드 맵
     */
    public Map<String, String> toResponse(EventVO eventVO) {
        return Map.of(
            "id", eventVO.id(),
            "message", eventVO.message()
        );
    }
}
