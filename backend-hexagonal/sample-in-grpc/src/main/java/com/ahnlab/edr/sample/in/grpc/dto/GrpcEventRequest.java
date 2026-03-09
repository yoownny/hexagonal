package com.ahnlab.edr.sample.in.grpc.dto;

/**
 * DTO representing a gRPC-style event request.
 */
public class GrpcEventRequest {

	private final String id;
	private final String message;

	public GrpcEventRequest(String id, String message) {
		this.id = id;
		this.message = message;
	}

	public String getId() {
		return id;
	}

	public String getMessage() {
		return message;
	}
}
