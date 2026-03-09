package com.ahnlab.edr.sample.in.grpc.dto;

/**
 * DTO representing a gRPC-style user request.
 */
public class GrpcUserRequest {

	private final String id;
	private final String name;

	public GrpcUserRequest(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}
}
