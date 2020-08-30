/**
 * 
 */
package lk.sliit.ssd.service.impl;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import com.google.api.services.calendar.model.Events;

import lk.sliit.ssd.model.CalendarEvent;
import lk.sliit.ssd.service.CalendarService;
import lk.sliit.ssd.util.CommonUtil;

/**
 * @author vimukthi_r
 *
 */
@Service
public class GoogleCalendarService implements CalendarService {

	@Autowired
	GoogleAuthenticationService authenticationService;
	@Autowired
	private HttpTransport httpTransport;

	private Logger logger = LoggerFactory.getLogger(GoogleCalendarService.class);
	private Calendar service = null;

	@PostConstruct
	public void init() throws IOException {
		this.service = new Calendar.Builder(httpTransport, CommonUtil.JSON_FACTORY, authenticationService.getCredentials("userID"))
				.setApplicationName("PrettyCalendar")
				.build();
	}

	public List<Event> getLatestEventList() throws IOException {
		// List the next 10 events from the primary calendar.
		DateTime now = new DateTime(System.currentTimeMillis());
		Events events = service.events().list("primary").setMaxResults(10).setTimeMin(now).setOrderBy("startTime")
				.setSingleEvents(true).execute();
		List<Event> items = events.getItems();
		if (items.isEmpty()) {
			System.out.println("No upcoming events found.");
		} else {
			System.out.println("Upcoming events");
			for (Event event : items) {
				DateTime start = event.getStart().getDateTime();
				if (start == null) {
					start = event.getStart().getDate();
				}
				// model.addAttribute("name", String.format("%s (%s)\n", event.getSummary(),
				// start));
				System.out.printf("%s [%s] {%s} (%s)\n", event.getSummary(),event.getDescription(), event.getHtmlLink(), start);
			}
		}
		return items;
	}

	public void createNewEvent(CalendarEvent calendarEvent) throws IOException {
		Event event = new Event().setSummary(calendarEvent.getTitle()).setLocation(calendarEvent.getLocation())
				.setDescription(calendarEvent.getDescription());

//		LocalDate date = LocalDate.fro
//		System.out.println(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
//		        .format(LocalDateTime.of(date, time)));
		final String format = "%sT%s:00+05:30";
		System.err.println(calendarEvent);
		System.out.println(String.format(format, calendarEvent.getDate(), calendarEvent.getStartTime()));
		
		DateTime startDateTime = new DateTime(String.format(format, calendarEvent.getDate(), calendarEvent.getStartTime()));
		EventDateTime start = new EventDateTime().setDateTime(startDateTime).setTimeZone("Asia/Colombo");
		event.setStart(start);

		DateTime endDateTime = new DateTime(String.format(format, calendarEvent.getDate(), calendarEvent.getEndTime()));
		EventDateTime end = new EventDateTime().setDateTime(endDateTime).setTimeZone("Asia/Colombo");
		event.setEnd(end);

		//String[] recurrence = new String[] { "RRULE:FREQ=DAILY;COUNT=2" };
		//event.setRecurrence(Arrays.asList(recurrence));

		EventAttendee[] attendees = new EventAttendee[] { new EventAttendee().setEmail("vimukthi_r@epiclanka.net"),
				new EventAttendee().setEmail(calendarEvent.getGuests()) };
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
