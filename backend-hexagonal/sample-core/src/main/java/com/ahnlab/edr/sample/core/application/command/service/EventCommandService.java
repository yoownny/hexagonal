package com.ahnlab.edr.sample.core.application.command.service;

import com.ahnlab.edr.sample.core.application.command.port.in.EventCommandUseCase;
import com.ahnlab.edr.sample.core.application.command.port.out.EventCommandStorePort;
import com.ahnlab.edr.sample.core.application.exception.event.EventErrorCode;
import com.ahnlab.edr.sample.core.application.exception.event.EventException;
import com.ahnlab.edr.sample.core.domain.vo.EventVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventCommandService implements EventCommandUseCase {

    private final EventCommandStorePort storePort;

    @Override
    public void saveEvent(EventVO eventVO) {
        try {
            storePort.save(eventVO);
        } catch (RuntimeException e) {
            log.error("Failed to save event: {}", eventVO.id(), e);
            throw new EventException(EventErrorCode.EVENT_SAVE_FAILED, eventVO.id(), e);
        }
    }
}
