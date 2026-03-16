package com.ahnlab.edr.sample.core.domain.vo;

/**
 * 이벤트 도메인 Value Object.
 * 불변 객체이며, 생성 시점에 비즈니스 검증을 수행한다.
 *
 * @param id      이벤트 식별자
 * @param message 이벤트 메시지
 */
public record EventVO(String id, String message) {

    public EventVO {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Event ID cannot be null or blank");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Event message cannot be null or blank");
        }
    }
}
