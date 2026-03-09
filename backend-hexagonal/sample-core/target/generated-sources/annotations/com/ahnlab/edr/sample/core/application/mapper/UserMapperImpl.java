package com.ahnlab.edr.sample.core.application.mapper;

import com.ahnlab.edr.sample.core.domain.entity.UserEntity;
import com.ahnlab.edr.sample.core.domain.vo.UserVO;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-09T21:43:35+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.1 (Oracle Corporation)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserEntity toEntity(UserVO vo) {
        if ( vo == null ) {
            return null;
        }

        String id = null;
        String name = null;

        id = vo.id();
        name = vo.name();

        UserEntity userEntity = new UserEntity( id, name );

        return userEntity;
    }

    @Override
    public UserVO toVO(UserEntity entity) {
        if ( entity == null ) {
            return null;
        }

        String id = null;
        String name = null;

        id = entity.getId();
        name = entity.getName();

        UserVO userVO = new UserVO( id, name );

        return userVO;
    }
}
