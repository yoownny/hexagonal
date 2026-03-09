package com.ahnlab.edr.sample.core.domain.vo;

/**
 * Value Object representing an event in the domain layer.
 * Immutable and contains business validation logic.
 */
public record EventVO(String id, String message) {

	public EventVO {
		if (id == null || id.isBlank()) {
			throw new IllegalArgumentException("Event ID cannot be null or empty");
		}
	}
}
