package com.ahnlab.edr.sample.core.application.mapper;

import com.ahnlab.edr.sample.core.domain.entity.UserEntity;
import com.ahnlab.edr.sample.core.domain.vo.UserVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

	UserEntity toEntity(UserVO vo);

	UserVO toVO(UserEntity entity);
}
