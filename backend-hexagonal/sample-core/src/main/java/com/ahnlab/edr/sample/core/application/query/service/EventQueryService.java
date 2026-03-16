package com.ahnlab.edr.sample.core.application.query.service;

import com.ahnlab.edr.sample.core.application.exception.event.EventErrorCode;
import com.ahnlab.edr.sample.core.application.exception.event.EventException;
import com.ahnlab.edr.sample.core.application.query.port.in.EventQueryUseCase;
import com.ahnlab.edr.sample.core.application.query.port.out.EventQueryStorePort;
import com.ahnlab.edr.sample.core.domain.vo.EventVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventQueryService implements EventQueryUseCase {

    private final EventQueryStorePort storePort;

    @Override
    public Optional<EventVO> getEvent(String id) {
        try {
            return storePort.findById(id);
        } catch (RuntimeException e) {
            log.error("Failed to query event: {}", id, e);
            throw new EventException(EventErrorCode.EVENT_QUERY_FAILED, id, e);
        }
    }
}
