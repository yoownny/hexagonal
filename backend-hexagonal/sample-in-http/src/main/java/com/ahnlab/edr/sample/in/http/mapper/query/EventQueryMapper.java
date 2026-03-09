package com.ahnlab.edr.sample.in.http.mapper.query;

import com.ahnlab.edr.sample.core.domain.vo.EventVO;
import com.ahnlab.edr.sample.in.http.dto.query.EventResponse;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for converting EventVO to EventResponse.
 * Used in query operations (GET).
 */
@Mapper(componentModel = "spring")
public interface EventQueryMapper {

	EventResponse toResponse(EventVO vo);
}
