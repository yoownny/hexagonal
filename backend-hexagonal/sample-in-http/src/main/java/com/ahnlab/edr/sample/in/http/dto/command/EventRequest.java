package com.ahnlab.edr.sample.in.http.dto.command;

import lombok.Data;

/**
 * Request DTO for creating an event via HTTP.
 * Used in command operations (POST/PUT).
 */
@Data
public class EventRequest {

	private String id;

	private String message;
}
