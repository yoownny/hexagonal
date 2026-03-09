package com.ahnlab.edr.sample.in.grpc.mapper;

import com.ahnlab.edr.sample.core.domain.vo.UserVO;
import com.ahnlab.edr.sample.in.grpc.dto.GrpcUserRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GrpcUserMapper {

	UserVO toVO(GrpcUserRequest request);
}
