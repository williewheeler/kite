package org.zkybase.kite.samples.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.zkybase.kite.annotation.GuardedByCircuitBreaker;
import org.zkybase.kite.annotation.GuardedByThrottle;
import org.zkybase.kite.samples.model.Message;
import org.zkybase.kite.samples.service.MessageService;
import org.zkybase.kite.samples.util.Flakinator;

/**
 * @author Willie Wheeler (willie.wheeler@gmail.com)
 */
@Service
public class MessageServiceImpl implements MessageService {

	/* (non-Javadoc)
	 * @see org.zkybase.kite.samples.service.MessageService#getMotd()
	 */
	@Override
	@GuardedByCircuitBreaker("messageServiceBreaker")
	@GuardedByThrottle("messageServiceThrottle")
	public Message getMotd() {
		Flakinator.simulateFlakiness();
		return createMessage("<p>Welcome to Aggro's Towne!</p>");
	}

	/* (non-Javadoc)
	 * @see org.zkybase.kite.samples.service.MessageService#getImportantMessages()
	 */
	@Override
	@GuardedByCircuitBreaker("messageServiceBreaker")
	@GuardedByThrottle("messageServiceThrottle")
	public List<Message> getImportantMessages() {
		Flakinator.simulateFlakiness();
		List<Message> messages = new ArrayList<Message>();
		messages.add(createMessage("<p>Important message 1</p>"));
		messages.add(createMessage("<p>Important message 2</p>"));
		messages.add(createMessage("<p>Important message 3</p>"));
		return messages;
	}
	
	private Message createMessage(String htmlText) {
		Message message = new Message();
		message.setHtmlText(htmlText);
		return message;
	}
	
}
