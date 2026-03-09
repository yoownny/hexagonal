package com.ahnlab.edr.sample.core.application.query.port.in;

import com.ahnlab.edr.sample.core.domain.vo.UserVO;
import java.util.Optional;

/**
 * Query-side inbound port for loading users.
 */
public interface UserQueryUseCase {

	Optional<UserVO> getUser(String id);
}
