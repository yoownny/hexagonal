package com.ahnlab.edr.sample.in.http.mapper;

import com.ahnlab.edr.sample.core.domain.vo.UserVO;
import com.ahnlab.edr.sample.in.http.dto.UserRequest;
import com.ahnlab.edr.sample.in.http.dto.UserResponse;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-26T17:05:42+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.9 (Amazon.com Inc.)"
)
@Component
public class UserHttpMapperImpl implements UserHttpMapper {

    @Override
    public UserVO toVO(UserRequest request) {
        if ( request == null ) {
            return null;
        }

        String id = null;
        String name = null;

        id = request.getId();
        name = request.getName();

        UserVO userVO = new UserVO( id, name );

        return userVO;
    }

    @Override
    public UserResponse toResponse(UserVO vo) {
        if ( vo == null ) {
            return null;
        }

        UserResponse userResponse = new UserResponse();

        userResponse.setId( vo.id() );
        userResponse.setName( vo.name() );

        return userResponse;
    }
}
