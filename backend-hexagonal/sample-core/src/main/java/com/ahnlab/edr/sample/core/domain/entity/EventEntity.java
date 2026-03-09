package com.ahnlab.edr.sample.core.domain.entity;

import com.ahnlab.edr.sample.core.domain.vo.EventVO;

/**
 * Entity representing an event for persistence layer.
 * Used by outbound ports for database mapping.
 */
public class EventEntity {

	private final String id;

	private final String message;

	public EventEntity(String id, String message) {
		this.id = id;
		this.message = message;
	}

	/**
	 * Creates an EventEntity from a Value Object.
	 *
	 * @param vo the EventVO to convert
	 * @return a new EventEntity instance
	 */
	public static EventEntity fromVO(EventVO vo) {
		return new EventEntity(vo.id(), vo.message());
	}

	/**
	 * Converts this entity to a Value Object.
	 *
	 * @return a new EventVO instance
	 */
	public EventVO toVO() {
		return new EventVO(id, message);
	}

	public String getId() {
		return id;
	}

	public String getMessage() {
		return message;
	}
}
