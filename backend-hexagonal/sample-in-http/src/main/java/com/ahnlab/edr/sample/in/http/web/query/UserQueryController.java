package com.ahnlab.edr.sample.in.http.web.query;

import com.ahnlab.edr.sample.core.application.query.port.in.UserQueryUseCase;
import com.ahnlab.edr.sample.core.domain.vo.UserVO;
import com.ahnlab.edr.sample.in.http.dto.query.UserResponse;
import com.ahnlab.edr.sample.in.http.mapper.query.UserQueryMapper;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HTTP query adapter for user use cases.
 */
@RestController
@RequestMapping("/api/users")
public class UserQueryController {

	private final UserQueryUseCase userQueryUseCase;
	private final UserQueryMapper userQueryMapper;

	public UserQueryController(UserQueryUseCase userQueryUseCase, UserQueryMapper userQueryMapper) {
		this.userQueryUseCase = userQueryUseCase;
		this.userQueryMapper = userQueryMapper;
		System.out.println("[HTTP] UserQueryController bean created (sample.inbound.http-enabled=true)");
	}

	@GetMapping("/{id}")
	public ResponseEntity<UserResponse> get(@PathVariable("id") String id) {
		System.out.println("[HTTP] GET /api/users/" + id + " - start");
		Optional<UserVO> userVO = userQueryUseCase.getUser(id);
		if (userVO.isEmpty()) {
			System.out.println("[HTTP] GET /api/users/" + id + " - not found");
			return ResponseEntity.notFound().build();
		}
		UserResponse response = userQueryMapper.toResponse(userVO.get());
		System.out.println("[HTTP] GET /api/users/" + id + " - found user with name='" + response.getName() + "'");
		return ResponseEntity.ok(response);
	}
}
