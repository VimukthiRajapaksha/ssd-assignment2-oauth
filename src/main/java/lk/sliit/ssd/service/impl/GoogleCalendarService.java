/**
 * 
 */
package lk.sliit.ssd.service.impl;

import java.io.IOException;
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
		this.service = new Calendar.Builder(httpTransport, CommonUtil.JSON_FACTORY,
				authenticationService.getCredentials("userID")).setApplicationName("PrettyCalendar").build();
	}

	public List<Event> getLatestEventList() throws IOException {
		// List the next 10 events from the primary calendar.
		DateTime now = new DateTime(System.currentTimeMillis());

		// Sends the metadata request to the resource server and returns the parsed
		// metadata response.
		Events events = service.events().list("primary").setMaxResults(10).setTimeMin(now).setOrderBy("startTime")
				.setSingleEvents(true).execute();

		// Retrieve events list
		List<Event> items = events.getItems();
		if (items.isEmpty()) {
			logger.info("No upcoming events found.");
		} else {
			logger.info("Upcoming events found.");
			items.forEach(e -> logger.info("Event : {} {} {} {}", e.getSummary(), e.getDescription(), e.getHtmlLink(),
					e.getStart().getDateTime()));
		}
		return items;
	}

	// Create a new event and sends the request to the auth end point
	public void createNewEvent(CalendarEvent calendarEvent) throws IOException {
		// Initialize a new event
		Event event = new Event().setSummary(calendarEvent.getTitle()).setLocation(calendarEvent.getLocation())
				.setDescription(calendarEvent.getDescription());

		// Add start date
		DateTime startDateTime = new DateTime(String.format(CommonUtil.RFC_3339_DATETIME_FORMAT,
				calendarEvent.getDate(), calendarEvent.getStartTime()));
		EventDateTime start = new EventDateTime().setDateTime(startDateTime).setTimeZone(CommonUtil.UTC_SL_TIME_ZONE);
		event.setStart(start);

		// Add end date
		DateTime endDateTime = new DateTime(String.format(CommonUtil.RFC_3339_DATETIME_FORMAT, calendarEvent.getDate(),
				calendarEvent.getEndTime()));
		EventDateTime end = new EventDateTime().setDateTime(endDateTime).setTimeZone(CommonUtil.UTC_SL_TIME_ZONE);
		event.setEnd(end);

		// Add attendees
		EventAttendee[] attendees = new EventAttendee[] { new EventAttendee().setEmail(calendarEvent.getGuests()) };
		event.setAttendees(Arrays.asList(attendees));

		// Add reminders
		EventReminder[] reminderOverrides = new EventReminder[] {
				new EventReminder().setMethod("email").setMinutes(24 * 60),
				new EventReminder().setMethod("popup").setMinutes(10), };
		Event.Reminders reminders = new Event.Reminders().setUseDefault(false)
				.setOverrides(Arrays.asList(reminderOverrides));
		event.setReminders(reminders);

		// Created the event. This request holds the parameters needed by the calendar server.
		this.service.events().insert("primary", event).execute();
	}

}
