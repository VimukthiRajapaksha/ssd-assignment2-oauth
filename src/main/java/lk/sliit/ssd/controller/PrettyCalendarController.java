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
import lk.sliit.ssd.util.CommonUtil;

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
	 * Handles the initial request. Checks if the user has already authenticated via
	 * OAuth.
	 * 
	 * @return
	 * @throws IOException
	 */
	@GetMapping("/")
	public String showEventsPage(HttpSession session, Model model) {
		try {
			if (authenticationService.isAuthenticatedUser(session.getId())) {
				logger.info("User has already authenticated. Redirecting to events...");
				return CommonUtil.REDIRECT_TO_EVENTS_PAGE;
			} else {
				logger.info("User has not authenticated. Redirecting to login...");
				return "index";
			}
		} catch (Exception e) {
			logger.error("Exception occured when checking user auth status. {}", e);
			model.addAttribute(CommonUtil.ERROR, e.getMessage());
			return CommonUtil.ERROR;
		}
	}

	/**
	 * 
	 * @param model
	 * @return add_calendar_event page
	 */
	@GetMapping("/event")
	public String showEventPage(Model model) {
		logger.info("called /event endpoint");
		model.addAttribute("calendarEvent", new CalendarEvent());
		return "add_calendar_event";
	}

	/**
	 * Calls the Google Authentication service to authenticate the user
	 * 
	 * @param response
	 * @throws IOException
	 */
	@GetMapping("/signin")
	public void doGoogleSignIn(HttpServletResponse response) throws IOException {
		logger.info("Called signin page. Redirecting to Google Auth server");
		response.sendRedirect(authenticationService.authenticateUser());
	}

	/**
	 * PrettyCalendar callback URL for redirection from Google OAuth
	 * 
	 * @param request
	 * @return
	 * @throws IOException
	 */
	@GetMapping("/oauth")
	public String saveAuthorizationCode(HttpServletRequest request, HttpSession session, Model model) {
		try {
			logger.info("Called callback URL. Request: {}", request);
			String code = request.getParameter("code");
			logger.info("OAuth consent complete. Received code: {}", code);

			if (code != null) {
				authenticationService.getTokensFromGoogleCode(code, session.getId());

				Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
				DefaultOidcUser user = (DefaultOidcUser) authentication.getPrincipal();

				// add values to the session
				session.setAttribute("sessionID", user.getName());
				session.setAttribute("name", user.getFullName());
				session.setAttribute("picture", user.getPicture());

				return CommonUtil.REDIRECT_TO_EVENTS_PAGE;
			} else {
				model.addAttribute(CommonUtil.ERROR, "Google authorization failed !");
			}
		} catch (Exception e) {
			logger.error("Exception occured when getting authorization code. {}", e);
			model.addAttribute(CommonUtil.ERROR, e.getMessage());
		}
		return CommonUtil.ERROR;
	}

	/**
	 * Return latest calendar events list
	 * 
	 * @param model
	 * @return
	 */
	@GetMapping("/events")
	public String getEventList(Model model) {
		try {
			logger.info("Retreaving latest calendar events list.");
			model.addAttribute("events", calendarService.getLatestEventList());
			return "home";

		} catch (Exception e) {
			logger.error("Exception occured when retreaving events. {}", e);
			model.addAttribute(CommonUtil.ERROR, e.getMessage());
			return CommonUtil.ERROR;
		}

	}

	/**
	 * Add a new event to the google calendar and redirect to events page.
	 * 
	 * @param calendarEvent
	 * @return
	 */
	@PostMapping("/events")
	public String addNewEvent(@ModelAttribute CalendarEvent calendarEvent, Model model) {
		try {
			logger.info("Adding a new calendar event");
			calendarService.createNewEvent(calendarEvent);
			return CommonUtil.REDIRECT_TO_EVENTS_PAGE;
		} catch (Exception e) {
			logger.error("Exception occured when adding new event. {}", e);
			model.addAttribute(CommonUtil.ERROR, e.getMessage());
			return CommonUtil.ERROR;
		}

	}

	/**
	 * Handle user logout
	 * 
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/logout")
	public String logout(HttpServletRequest request, Model model) throws Exception {
		try {
			logger.info("Called Logout...");
			authenticationService.removeUserSession();
			return "redirect:/login";
		} catch (Exception e) {
			logger.error("Exception occured when logout. {}", e);
			model.addAttribute(CommonUtil.ERROR, e.getMessage());
			return CommonUtil.ERROR;
		}
	}

}
