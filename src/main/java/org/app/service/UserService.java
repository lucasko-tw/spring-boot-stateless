package org.app.service;

import org.app.config.Role;
import org.app.dao.UserDAO;
import org.app.model.User;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

	@Autowired
	private UserDAO USER_DAO;
 
	public User save(User user) {
		user.setPassword(user.getPassword());
		user.setEnabled(true);
		user.setRole(Role.ROLE_USER.toString());
		return USER_DAO.save(user);
	}
  
	public User findByAccount(String account) {

		return USER_DAO.findByAccount(account);
	}
	
	public JSONObject getProfile(String account) {

		User user = USER_DAO.findByAccount(account);

		JSONObject userJSON = new JSONObject();
		userJSON.put("account", user.getAccount());
		userJSON.put("firstname", user.getFirstname());
		userJSON.put("lastname", user.getLastname());
		userJSON.put("role", user.getRole());
		 

		return userJSON;
	}
 
}