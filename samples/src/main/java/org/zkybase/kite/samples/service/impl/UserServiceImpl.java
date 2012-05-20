/*
 * Copyright (c) 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zkybase.kite.samples.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.zkybase.kite.GuardedBy;
import org.zkybase.kite.samples.model.User;
import org.zkybase.kite.samples.service.UserService;
import org.zkybase.kite.samples.util.Flakinator;

/**
 * @author Willie Wheeler (willie.wheeler@gmail.com)
 * @since 1.0
 */
@Service
public class UserServiceImpl implements UserService {
	private Flakinator flakinator = new Flakinator();

	/* (non-Javadoc)
	 * @see org.zkybase.kite.samples.service.UserService#getRecentUsers()
	 */
	@GuardedBy({ "rateLimitingThrottle", "userServiceBreaker" })
	public List<User> getRecentUsers() {
		flakinator.simulateFlakiness();
		
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
