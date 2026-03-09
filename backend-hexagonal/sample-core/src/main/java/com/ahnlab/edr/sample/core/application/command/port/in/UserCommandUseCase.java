package com.ahnlab.edr.sample.core.application.command.port.in;

import com.ahnlab.edr.sample.core.domain.vo.UserVO;

/**
 * Command-side inbound port for saving users.
 */
public interface UserCommandUseCase {

	void saveUser(UserVO userVO);
}
