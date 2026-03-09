package com.ahnlab.edr.sample.in.http.web.command;

import com.ahnlab.edr.sample.core.application.command.port.in.UserCommandUseCase;
import com.ahnlab.edr.sample.core.domain.vo.UserVO;
import com.ahnlab.edr.sample.in.http.dto.command.UserRequest;
import com.ahnlab.edr.sample.in.http.mapper.command.UserCommandMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HTTP command adapter for user use cases.
 */
@RestController
@RequestMapping("/api/users")
public class UserCommandController {

	private final UserCommandUseCase userCommandUseCase;
	private final UserCommandMapper userCommandMapper;

	public UserCommandController(UserCommandUseCase userCommandUseCase, UserCommandMapper userCommandMapper) {
		this.userCommandUseCase = userCommandUseCase;
		this.userCommandMapper = userCommandMapper;
		System.out.println("[HTTP] UserCommandController bean created (sample.inbound.http-enabled=true)");
	}

	@PostMapping
	public ResponseEntity<Void> save(@RequestBody UserRequest request) {
		System.out.println("[HTTP] POST /api/users - id=" + request.getId() + ", name=" + request.getName());
		UserVO userVO = userCommandMapper.toVO(request);
		userCommandUseCase.saveUser(userVO);
		System.out.println("[HTTP] POST /api/users - completed for id=" + request.getId());
		return ResponseEntity.ok().build();
	}
}
