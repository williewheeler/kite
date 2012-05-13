package com.williewheeler.kite.samples.service.impl;

import org.springframework.stereotype.Service;

import com.williewheeler.kite.samples.model.Message;
import com.williewheeler.kite.samples.service.MessageService;

/**
 * @author Willie Wheeler (willie.wheeler@gmail.com)
 */
@Service
public class MessageServiceImpl implements MessageService {

	/* (non-Javadoc)
	 * @see com.williewheeler.kite.samples.service.MessageService#getMotd()
	 */
	public Message getMotd() {
		Message motd = new Message();
		motd.setHtmlText("<p>Welcome to Aggro's Towne!</p>");
		return motd;
	}

}
