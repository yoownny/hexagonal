package com.ahnlab.edr.sample.in.http.web.query;

import com.ahnlab.edr.sample.core.application.query.port.in.EventQueryUseCase;
import com.ahnlab.edr.sample.core.domain.vo.EventVO;
import com.ahnlab.edr.sample.in.http.dto.query.EventResponse;
import com.ahnlab.edr.sample.in.http.mapper.query.EventQueryMapper;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HTTP query adapter for event use cases.
 */
@RestController
@RequestMapping("/api/events")
public class EventQueryController {

	private final EventQueryUseCase eventQueryUseCase;
	private final EventQueryMapper eventQueryMapper;

	public EventQueryController(EventQueryUseCase eventQueryUseCase, EventQueryMapper eventQueryMapper) {
		this.eventQueryUseCase = eventQueryUseCase;
		this.eventQueryMapper = eventQueryMapper;
		System.out.println("[HTTP] EventQueryController bean created (sample.inbound.http-enabled=true)");
	}

	@GetMapping("/{id}")
	public ResponseEntity<EventResponse> get(@PathVariable("id") String id) {
		System.out.println("[HTTP] GET /api/events/" + id + " - start");
		Optional<EventVO> eventVO = eventQueryUseCase.getEvent(id);
		if (eventVO.isEmpty()) {
			System.out.println("[HTTP] GET /api/events/" + id + " - not found");
			return ResponseEntity.notFound().build();
		}
		EventResponse response = eventQueryMapper.toResponse(eventVO.get());
		System.out.println("[HTTP] GET /api/events/" + id + " - found event with message='" + response.getMessage() + "'");
		return ResponseEntity.ok(response);
	}
}
