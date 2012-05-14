package org.zkybase.kite.samples.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.zkybase.kite.annotation.GuardedByCircuitBreaker;
import org.zkybase.kite.samples.model.User;
import org.zkybase.kite.samples.service.UserService;
import org.zkybase.kite.samples.util.Flakinator;


/**
 * @author Willie Wheeler (willie.wheeler@gmail.com)
 */
@Service
public class UserServiceImpl implements UserService {

	/* (non-Javadoc)
	 * @see org.zkybase.kite.samples.service.UserService#getRecentUsers()
	 */
	@GuardedByCircuitBreaker("userServiceBreaker")
	public List<User> getRecentUsers() {
		Flakinator.simulateFlakiness();
		
		List<User> users = new ArrayList<User>();
		users.add(createUser("aggro_the_axe"));
		users.add(createUser("the_elf"));
		users.add(createUser("greg_broiles"));
		users.add(createUser("steven_lichter"));
		users.add(createUser("blackie_lawless"));
		users.add(createUser("the_knave"));
		
		return users;
	}
	
	private User createUser(String username) {
		User user = new User();
		user.setUsername(username);
		return user;
	}
}
