package com.ahnlab.edr.sample.in.http.dto.query;

import lombok.Data;

/**
 * Response DTO for reading an event via HTTP.
 * Used in query operations (GET).
 */
@Data
public class EventResponse {

	private String id;

	private String message;
}
