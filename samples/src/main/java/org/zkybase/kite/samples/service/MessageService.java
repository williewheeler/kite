package org.zkybase.kite.samples.service;

import java.util.List;

import org.zkybase.kite.samples.model.Message;


/**
 * @author Willie Wheeler (willie.wheeler@gmail.com)
 */
public interface MessageService {
	
	Message getMotd();
	
	List<Message> getImportantMessages();
}
