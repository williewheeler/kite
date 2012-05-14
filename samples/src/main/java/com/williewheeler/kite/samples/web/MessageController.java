package com.williewheeler.kite.samples.web;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.williewheeler.kite.circuitbreaker.CircuitBreakerCallback;
import com.williewheeler.kite.circuitbreaker.CircuitBreakerTemplate;
import com.williewheeler.kite.samples.model.Message;
import com.williewheeler.kite.samples.service.MessageService;

/**
 * @author Willie Wheeler (willie.wheeler@gmail.com)
 */
@Controller
public class MessageController {
	private static Logger log = LoggerFactory.getLogger(MessageController.class);
	
	@Inject private MessageService messageService;
	@Inject private CircuitBreakerTemplate breaker;
	
	@RequestMapping(value = "/motd", method = RequestMethod.GET)
	public String getMotd(Model model) {
		try {
			// Execute the service call inside a template method. This approach offers fine-grained control of exactly
			// what we do inside the template method, but it's invasive. Usually we won't need this level of control and
			// so it's generally recommended to use one of the declarative configuration approaches, such as AOP or else
			// annotation-driven configuration.
			Message motd = breaker.execute(new CircuitBreakerCallback<Message>() {
				public Message doInCircuitBreaker() throws Exception {
					return messageService.getMotd();
				}
			});
			model.addAttribute("motd", motd);
		} catch (Exception e) {
			log.error("Couldn't get MOTD: {}", e.getMessage());
		}
		
		return "motd";
	}
}
