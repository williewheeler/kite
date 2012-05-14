package org.zkybase.kite.samples.service;

import java.util.List;

import org.zkybase.kite.samples.model.User;


/**
 * @author Willie Wheeler (willie.wheeler@gmail.com)
 */
public interface UserService {
	
	List<User> getRecentUsers();
}
