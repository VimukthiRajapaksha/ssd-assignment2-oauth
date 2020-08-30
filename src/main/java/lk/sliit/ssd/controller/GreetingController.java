package lk.sliit.ssd.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;

import lk.sliit.ssd.model.CalendarEvent;

//@Controller
public class GreetingController {

	private static final String TOKENS_DIRECTORY_PATH = "tokens";
	private final String callback = "http://localhost:8083/oauth";
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);
	private String userid = "MY_DUMMY_USER";
	private static final String CREDENTIALS_FILE_PATH = "/client_secrets.json";

	private GoogleAuthorizationCodeFlow flow = null;
	private Calendar service = null;

	@PostConstruct
	public void init() throws GeneralSecurityException, IOException {

		final HttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		final DataStoreFactory dataStoreFactory = new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH));

		InputStream in = GreetingController.class.getResourceAsStream(CREDENTIALS_FILE_PATH);

		if (in == null) {
			throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
		}
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		// Build flow and trigger user authorization request.
		this.flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
				.setDataStoreFactory(dataStoreFactory).setAccessType("offline").build();
	}

	@GetMapping("/demo")
	public String greeting(@RequestParam(name = "name", required = false, defaultValue = "World") String name,
			Model model, HttpServletResponse response, HttpServletRequest request) {
		try {

			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

			System.out.println("isauthenticated ::" + authentication.isAuthenticated());
			if (!(authentication instanceof AnonymousAuthenticationToken)) {
				DefaultOidcUser user = (DefaultOidcUser) authentication.getPrincipal();

				model.addAttribute("name", user.getFullName());
				model.addAttribute("calendarEvent", new CalendarEvent());

				LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8084).build();

				//Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");// this.flow.loadCredential(userid);

				// Build a new authorized API client service.
				final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
				this.service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, flow.getRequestInitializer())
						.setApplicationName("PrettyCalendar").build();

//				// List the next 10 events from the primary calendar.
//				DateTime now = new DateTime(System.currentTimeMillis());
//				Events events = service.events().list("primary").setMaxResults(10).setTimeMin(now)
//						.setOrderBy("startTime").setSingleEvents(true).execute();
//				List<Event> items = events.getItems();
//				if (items.isEmpty()) {
//					System.out.println("No upcoming events found.");
//				} else {
//					System.out.println("Upcoming events");
//					for (Event event : items) {
//						DateTime start = event.getStart().getDateTime();
//						if (start == null) {
//							start = event.getStart().getDate();
//						}
//						model.addAttribute("name", String.format("%s (%s)\n", event.getSummary(), start));
//						System.out.printf("%s (%s)\n", event.getSummary(), start);
//					}
//				}

			}

		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("name", name);
			// TODO: handle exception
		}
		return "header";
	}

	@PostMapping("/event")
	public void addEvent(@ModelAttribute CalendarEvent calendarEvent) throws IOException {
		Event event = new Event().setSummary(calendarEvent.getTitle()).setLocation(calendarEvent.getLocation())
				.setDescription(calendarEvent.getDescription());

		DateTime startDateTime = new DateTime("2020-08-28T18:00:00-07:00");
		EventDateTime start = new EventDateTime().setDateTime(startDateTime).setTimeZone("America/Los_Angeles");
		event.setStart(start);

		DateTime endDateTime = new DateTime("2020-08-28T19:00:00-07:00");
		EventDateTime end = new EventDateTime().setDateTime(endDateTime).setTimeZone("America/Los_Angeles");
		event.setEnd(end);

		String[] recurrence = new String[] { "RRULE:FREQ=DAILY;COUNT=2" };
		event.setRecurrence(Arrays.asList(recurrence));

		EventAttendee[] attendees = new EventAttendee[] { new EventAttendee().setEmail("vimukthi_r@epiclanka.net"),
				new EventAttendee().setEmail("vggayan@gmail.com") };
		event.setAttendees(Arrays.asList(attendees));

		EventReminder[] reminderOverrides = new EventReminder[] {
				new EventReminder().setMethod("email").setMinutes(24 * 60),
				new EventReminder().setMethod("popup").setMinutes(10), };
		Event.Reminders reminders = new Event.Reminders().setUseDefault(false)
				.setOverrides(Arrays.asList(reminderOverrides));
		event.setReminders(reminders);

		String calendarId = "primary";
		event = this.service.events().insert(calendarId, event).execute();
		System.out.printf("Event created: %s\n", event.getHtmlLink());
	}

}
