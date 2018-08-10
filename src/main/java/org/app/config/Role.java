package org.app.config;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
	ROLE_ADMIN, ROLE_USER;

	@Override
	public String getAuthority() {

		return name();
	}

	static public boolean isExist(String rolename) {

		try {
			Role role = valueOf(rolename);
			return true;
		} catch (IllegalArgumentException ex) {
			System.out.println("role is not exist.");
			return false;
		}
	}
}