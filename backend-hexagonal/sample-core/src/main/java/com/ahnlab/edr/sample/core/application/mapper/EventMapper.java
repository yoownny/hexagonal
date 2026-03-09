package com.ahnlab.edr.sample.core.application.mapper;

import com.ahnlab.edr.sample.core.domain.entity.EventEntity;
import com.ahnlab.edr.sample.core.domain.vo.EventVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EventMapper {

	EventEntity toEntity(EventVO vo);

	EventVO toVO(EventEntity entity);
}
