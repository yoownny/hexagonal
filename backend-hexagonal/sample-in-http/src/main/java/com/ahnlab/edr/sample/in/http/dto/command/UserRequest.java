package com.ahnlab.edr.sample.in.http.dto.command;

import lombok.Data;

/**
 * Request DTO for creating a user via HTTP.
 * Used in command operations (POST/PUT).
 */
@Data
public class UserRequest {

	private String id;

	private String name;
}
