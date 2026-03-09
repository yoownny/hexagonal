package com.ahnlab.edr.sample.in.grpc.mapper;

import com.ahnlab.edr.sample.core.domain.vo.EventVO;
import com.ahnlab.edr.sample.in.grpc.dto.GrpcEventRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GrpcEventMapper {

	EventVO toVO(GrpcEventRequest request);
}
