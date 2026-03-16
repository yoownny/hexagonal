package com.ahnlab.edr.sample.in.http.web.query;

import com.ahnlab.edr.sample.config.HttpInboundEnabled;
import com.ahnlab.edr.sample.core.application.exception.event.EventErrorCode;
import com.ahnlab.edr.sample.core.application.exception.event.EventException;
import com.ahnlab.edr.sample.core.application.query.port.in.EventQueryUseCase;
import com.ahnlab.edr.sample.in.http.dto.query.EventResponse;
import com.ahnlab.edr.sample.in.http.mapper.query.EventQueryMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 이벤트 Query Controller.
 * GET 요청만 처리한다.
 * 
 * 응답 형식:
 * - 성공 (200): ResponseEntity<EventResponse> (GlobalExceptionHandler에서 ApiResponse로 변환)
 * - 실패 (4xx, 5xx): GlobalExceptionHandler가 ApplicationException을 ApiResponse로 변환
 */
@Tag(name = "Event Query", description = "이벤트 조회 API")
@RestController
@RequestMapping("/api/events")
@HttpInboundEnabled
@RequiredArgsConstructor
public class EventQueryController {

    private final EventQueryUseCase eventQueryUseCase;
    private final EventQueryMapper eventQueryMapper;

    /**
     * ID로 이벤트를 조회한다.
     * 이벤트가 없으면 EventException을 던져 GlobalExceptionHandler에서 404로 처리한다.
     *
     * @param id 이벤트 식별자
     * @return 이벤트 응답 또는 404 (예외로 처리)
     */
    @Operation(summary = "이벤트 단건 조회", description = "ID로 이벤트를 조회합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = EventResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "이벤트를 찾을 수 없음")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> get(
            @Parameter(description = "이벤트 ID", example = "EVT-20240101-001", required = true)
            @PathVariable("id") String id) {
        return eventQueryUseCase.getEvent(id)
                .map(eventQueryMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new EventException(EventErrorCode.EVENT_NOT_FOUND, id));
    }
}
