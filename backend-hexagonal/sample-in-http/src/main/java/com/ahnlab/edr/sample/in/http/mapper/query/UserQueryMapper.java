package com.ahnlab.edr.sample.in.http.mapper.query;

import com.ahnlab.edr.sample.core.domain.vo.UserVO;
import com.ahnlab.edr.sample.in.http.dto.query.UserResponse;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for converting UserVO to UserResponse.
 * Used in query operations (GET).
 */
@Mapper(componentModel = "spring")
public interface UserQueryMapper {

	UserResponse toResponse(UserVO vo);
}
