package com.williewheeler.kite.samples.web;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.williewheeler.kite.samples.service.MessageService;

/**
 * @author Willie Wheeler (willie.wheeler@gmail.com)
 */
@Controller
public class MessageController {
	@Inject private MessageService messageService;
	
	@RequestMapping(value = "/motd", method = RequestMethod.GET)
	public String getMotd(Model model) {
		model.addAttribute("motd", messageService.getMotd());
		return "motd";
	}
}
