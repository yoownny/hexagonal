package com.ahnlab.edr.sample.in.http.web.command;

import com.ahnlab.edr.sample.config.HttpInboundEnabled;
import com.ahnlab.edr.sample.core.application.command.port.in.EventCommandUseCase;
import com.ahnlab.edr.sample.in.http.dto.command.EventRequest;
import com.ahnlab.edr.sample.in.http.mapper.command.EventCommandMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 이벤트 Command Controller.
 * POST/PUT/DELETE 요청만 처리한다.
 * 
 * 응답 형식:
 * - 성공 (200): ResponseEntity<Void> (GlobalExceptionHandler에서 ApiResponse로 변환)
 * - 실패 (4xx, 5xx): GlobalExceptionHandler가 ApplicationException을 ApiResponse로 변환
 */
@Tag(name = "Event Command", description = "이벤트 생성·수정·삭제 API")
@RestController
@RequestMapping("/api/events")
@HttpInboundEnabled
@RequiredArgsConstructor
public class EventCommandController {

    private final EventCommandUseCase eventCommandUseCase;
    private final EventCommandMapper eventCommandMapper;

    /**
     * 이벤트를 저장한다.
     *
     * @param request 이벤트 생성 요청
     * @return 200 OK (응답은 GlobalExceptionHandler에서 ApiResponse로 변환)
     */
    @Operation(summary = "이벤트 저장", description = "새로운 이벤트를 저장합니다. 동일 ID가 존재하면 덮어씁니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "저장 성공"),
            @ApiResponse(responseCode = "400", description = "요청 파라미터 오류"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping
    public ResponseEntity<Void> save(@RequestBody @Valid EventRequest request) {
        eventCommandUseCase.saveEvent(eventCommandMapper.toVO(request));
        return ResponseEntity.ok().build();
    }
}
