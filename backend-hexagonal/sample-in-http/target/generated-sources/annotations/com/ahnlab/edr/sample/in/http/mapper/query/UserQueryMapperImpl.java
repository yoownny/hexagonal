package com.ahnlab.edr.sample.in.http.mapper.query;

import com.ahnlab.edr.sample.core.domain.vo.UserVO;
import com.ahnlab.edr.sample.in.http.dto.query.UserResponse;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-09T21:43:41+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.1 (Oracle Corporation)"
)
@Component
public class UserQueryMapperImpl implements UserQueryMapper {

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
