package com.ahnlab.edr.sample.in.http.mapper.command;

import com.ahnlab.edr.sample.core.domain.vo.UserVO;
import com.ahnlab.edr.sample.in.http.dto.command.UserRequest;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-09T21:43:40+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.1 (Oracle Corporation)"
)
@Component
public class UserCommandMapperImpl implements UserCommandMapper {

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
}
