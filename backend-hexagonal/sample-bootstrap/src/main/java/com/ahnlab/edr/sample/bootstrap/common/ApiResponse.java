package com.ahnlab.edr.sample.bootstrap.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * API 응답 표준 래퍼.
 * 성공: {@code ApiResponse.ok(data)}
 * 실패: {@code ApiResponse.error(code, message)}
 *
 * @param <T> 응답 데이터 타입
 */
@Schema(description = "API 공통 응답 래퍼")
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    @Schema(description = "요청 성공 여부", example = "true")
    private final boolean success;

    @Schema(description = "응답 데이터 (성공 시에만 존재)")
    private final T data;

    @Schema(description = "에러 코드 (실패 시에만 존재)", example = "EVT-101")
    private final String errorCode;

    @Schema(description = "에러 메시지 (실패 시에만 존재)", example = "Event with id 'X' not found")
    private final String message;

    /**
     * 성공 응답 (데이터 있음)
     *
     * @param data 응답 데이터
     * @param <T>  데이터 타입
     * @return 성공 응답
     */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null, null);
    }

    /**
     * 성공 응답 (데이터 없음)
     *
     * @param <T> 데이터 타입
     * @return 성공 응답
     */
    public static <T> ApiResponse<T> ok() {
        return new ApiResponse<>(true, null, null, null);
    }

    /**
     * 실패 응답
     *
     * @param errorCode 에러 코드
     * @param message   에러 메시지
     * @param <T>       데이터 타입
     * @return 실패 응답
     */
    public static <T> ApiResponse<T> error(String errorCode, String message) {
        return new ApiResponse<>(false, null, errorCode, message);
    }
}
