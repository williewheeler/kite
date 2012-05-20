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
import org.zkybase.kite.samples.model.Message;
import org.zkybase.kite.samples.service.MessageService;
import org.zkybase.kite.samples.util.Flakinator;

/**
 * @author Willie Wheeler (willie.wheeler@gmail.com)
 * @since 1.0
 */
@Service
public class MessageServiceImpl implements MessageService {
	private Flakinator flakinator = new Flakinator();

	/* (non-Javadoc)
	 * @see org.zkybase.kite.samples.service.MessageService#getMotd()
	 */
	@Override
	@GuardedBy({
		"messageServiceConcurrencyThrottle",
		"rateLimitingThrottle",
		"messageServiceBreaker"
	})
	public Message getMotd() {
		flakinator.simulateFlakiness();
		return createMessage("<p>Welcome to Aggro's Towne!</p>");
	}

	/* (non-Javadoc)
	 * @see org.zkybase.kite.samples.service.MessageService#getImportantMessages()
	 */
	@Override
	@GuardedBy({
		"messageServiceConcurrencyThrottle",
		"rateLimitingThrottle",
		"messageServiceBreaker"
	})
	public List<Message> getImportantMessages() {
		flakinator.simulateFlakiness();
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
