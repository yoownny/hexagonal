package com.ahnlab.edr.sample.core.domain.entity;

import com.ahnlab.edr.sample.core.domain.vo.UserVO;

/**
 * Entity representing a user for persistence layer.
 * Used by outbound ports for database mapping.
 */
public class UserEntity {

	private final String id;

	private final String name;

	public UserEntity(String id, String name) {
		this.id = id;
		this.name = name;
	}

	/**
	 * Creates a UserEntity from a Value Object.
	 *
	 * @param vo the UserVO to convert
	 * @return a new UserEntity instance
	 */
	public static UserEntity fromVO(UserVO vo) {
		return new UserEntity(vo.id(), vo.name());
	}

	/**
	 * Converts this entity to a Value Object.
	 *
	 * @return a new UserVO instance
	 */
	public UserVO toVO() {
		return new UserVO(id, name);
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}
}
