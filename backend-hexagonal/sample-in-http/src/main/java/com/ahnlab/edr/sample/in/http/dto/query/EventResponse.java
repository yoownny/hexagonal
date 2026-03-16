package com.ahnlab.edr.sample.in.http.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 이벤트 Query 응답 DTO.
 */
@Schema(description = "이벤트 조회 응답")
@Data
public class EventResponse {

    @Schema(description = "이벤트 고유 식별자", example = "EVT-20240101-001")
    private String id;

    @Schema(description = "이벤트 메시지", example = "파일 변경 감지됨")
    private String message;
}
