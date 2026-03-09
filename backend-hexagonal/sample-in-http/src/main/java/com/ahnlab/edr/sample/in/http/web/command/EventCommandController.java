package com.ahnlab.edr.sample.in.http.web.command;

import com.ahnlab.edr.sample.core.application.command.port.in.EventCommandUseCase;
import com.ahnlab.edr.sample.core.domain.vo.EventVO;
import com.ahnlab.edr.sample.in.http.dto.command.EventRequest;
import com.ahnlab.edr.sample.in.http.mapper.command.EventCommandMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HTTP command adapter for event use cases.
 */
@RestController
@RequestMapping("/api/events")
public class EventCommandController {

	private final EventCommandUseCase eventCommandUseCase;
	private final EventCommandMapper eventCommandMapper;

	public EventCommandController(EventCommandUseCase eventCommandUseCase, EventCommandMapper eventCommandMapper) {
		this.eventCommandUseCase = eventCommandUseCase;
		this.eventCommandMapper = eventCommandMapper;
		System.out.println("[HTTP] EventCommandController bean created (sample.inbound.http-enabled=true)");
	}

	@PostMapping
	public ResponseEntity<Void> save(@RequestBody EventRequest request) {
		System.out.println("[HTTP] POST /api/events - id=" + request.getId() + ", message=" + request.getMessage());
		EventVO eventVO = eventCommandMapper.toVO(request);
		eventCommandUseCase.saveEvent(eventVO);
		System.out.println("[HTTP] POST /api/events - completed for id=" + request.getId());
		return ResponseEntity.ok().build();
	}
}
