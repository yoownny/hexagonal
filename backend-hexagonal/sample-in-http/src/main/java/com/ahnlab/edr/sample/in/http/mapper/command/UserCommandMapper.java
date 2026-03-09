package com.ahnlab.edr.sample.in.http.mapper.command;

import com.ahnlab.edr.sample.core.domain.vo.UserVO;
import com.ahnlab.edr.sample.in.http.dto.command.UserRequest;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for converting UserRequest to UserVO.
 * Used in command operations (POST/PUT).
 */
@Mapper(componentModel = "spring")
public interface UserCommandMapper {

	UserVO toVO(UserRequest request);
}
