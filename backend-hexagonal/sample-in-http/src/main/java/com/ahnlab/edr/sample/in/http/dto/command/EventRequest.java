package com.ahnlab.edr.sample.in.http.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 이벤트 Command 요청 DTO.
 */
@Schema(description = "이벤트 저장 요청")
@Data
public class EventRequest {

    @Schema(description = "이벤트 고유 식별자", example = "EVT-20240101-001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Event ID is required")
    private String id;

    @Schema(description = "이벤트 메시지", example = "파일 변경 감지됨", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Event message is required")
    private String message;
}
