package com.ahnlab.edr.sample.in.http.mapper.command;

import com.ahnlab.edr.sample.core.domain.vo.EventVO;
import com.ahnlab.edr.sample.in.http.dto.command.EventRequest;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for converting EventRequest to EventVO.
 * Used in command operations (POST/PUT).
 *
 * <pre>
 * \@Mapping(source = "eventId", target = "id")
 * \@Mapping(source = "eventMessage", target = "message")
 * EventVO toVO(EventRequest request);
 * </pre>
 */
@Mapper(componentModel = "spring")
public interface EventCommandMapper {

	EventVO toVO(EventRequest request);
}
