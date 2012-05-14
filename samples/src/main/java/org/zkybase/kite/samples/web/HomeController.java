package org.zkybase.kite.samples.web;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.zkybase.kite.samples.service.MessageService;
import org.zkybase.kite.samples.service.UserService;

/**
 * @author Willie Wheeler (willie.wheeler@gmail.com)
 */
@Controller
public class HomeController {
	private static final Logger log = LoggerFactory.getLogger(HomeController.class);
	
	@Inject private MessageService messageService;
	@Inject private UserService userService;
	
	@RequestMapping(value = "", method = RequestMethod.GET)
	public String getHome(Model model) {
		loadMotd(model);
		loadImportantMessages(model);
		loadRecentUsers(model);
		return "home";
	}
	
	private void loadMotd(Model model) {
		try {
			model.addAttribute("motd", messageService.getMotd());
		} catch (Exception e) {
			log.error("Unable to load MOTD");
		}
	}
	
	private void loadImportantMessages(Model model) {
		try {
			model.addAttribute("importantMessages", messageService.getImportantMessages());
		} catch (Exception e) {
			log.error("Unable to load important messages");
		}
	}
	
	private void loadRecentUsers(Model model) {
		try {
			model.addAttribute("recentUsers", userService.getRecentUsers());
		} catch (Exception e) {
			log.error("Unable to load recent users");
		}
	}
}
