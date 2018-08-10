package org.app.dao;

import org.app.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDAO extends CrudRepository<User, Long> {
	User save(User user);

	User findByAccount(String account);
	
	boolean existsByAccount(String account);
}
