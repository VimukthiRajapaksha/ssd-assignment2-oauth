/**
 * 
 */
package lk.sliit.ssd.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import lk.sliit.ssd.model.CalendarEvent;
import lk.sliit.ssd.service.impl.GoogleAuthenticationService;
import lk.sliit.ssd.service.impl.GoogleCalendarService;

/**
 * @author vimukthi_r
 *
 */
@Controller
public class PrettyCalendarController {

	@Autowired
	GoogleAuthenticationService authenticationService;
	@Autowired
	GoogleCalendarService calendarService;

	private Logger logger = LoggerFactory.getLogger(PrettyCalendarController.class);

	/**
	 * Handles the root request. Checks if user is already authenticated via SSO.
	 * 
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/")
	public String showHomePage() throws Exception {
		if (authenticationService.isAuthenticatedUser("userID")) {
			logger.debug("User has already authenticated. Redirecting to home...");
			return "redirect:/events";
		} else {
			logger.debug("User is not authenticated. Redirecting to sso...");
			return "index";
		}
	}

	@GetMapping("/add_calendar_event")
	public String showEventPage(Model model) throws Exception {
		model.addAttribute("calendarEvent", new CalendarEvent());
		return "add_calendar_event";
	}
	
	/**
	 * Calls the Google OAuth service to authorize the app
	 * 
	 * @param response
	 * @throws Exception
	 */
	@GetMapping("/signin")
	public void doGoogleSignIn(HttpServletResponse response) throws Exception {
		logger.debug("SSO Called...");
		response.sendRedirect(authenticationService.authenticateUser());
	}

	/**
	 * Applications Callback URI for redirection from Google auth server after user
	 * approval/consent
	 * 
	 * @param request
	 * @return
	 * @throws IOException
	 */
	@GetMapping("/oauth")
	public String saveAuthorizationCode(HttpServletRequest request, HttpSession session) throws IOException {
		logger.debug("SSO Callback invoked...");
		String code = request.getParameter("code");
		logger.debug("SSO Callback Code Value..., " + code);

		if (code != null) {
			authenticationService.getTokensFromGoogleCode(code, "userID");

			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			DefaultOidcUser user = (DefaultOidcUser) authentication.getPrincipal();

			session.setAttribute("sessionID", user.getName());
			session.setAttribute("name", user.getFullName());
			session.setAttribute("picture", user.getPicture());

			return "redirect:/events";
		}
		return "header";
	}

	@GetMapping("/events")
	public String getEventList(Model model) throws IOException {
		model.addAttribute("events", calendarService.getLatestEventList());

		return "home";

	}

	@PostMapping("/events")
	public String addNewEvent(@ModelAttribute CalendarEvent calendarEvent) throws IOException {
		calendarService.createNewEvent(calendarEvent);

		return "redirect:/events";

	}
	
	/**
	 * Handles logout
	 * 
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/logout")
	public String logout(HttpServletRequest request) throws Exception {
		logger.debug("Logout invoked...");
		authenticationService.removeUserSession();
		return "redirect:/login";
	}

}
