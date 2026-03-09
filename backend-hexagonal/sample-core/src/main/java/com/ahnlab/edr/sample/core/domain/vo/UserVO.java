package com.ahnlab.edr.sample.core.domain.vo;

/**
 * Value Object representing a user in the domain layer.
 * Immutable and contains business validation logic.
 */
public record UserVO(String id, String name) {

	public UserVO {
		if (id == null || id.isBlank()) {
			throw new IllegalArgumentException("User ID cannot be null or empty");
		}
	}
}
