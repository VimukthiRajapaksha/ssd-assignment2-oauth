/**
 * 
 */
package lk.sliit.ssd.service;

import java.io.IOException;
import java.util.List;

import com.google.api.services.calendar.model.Event;

import lk.sliit.ssd.model.CalendarEvent;

/**
 * @author vimukthi_r
 *
 */
public interface CalendarService {

	public List<Event> getLatestEventList() throws IOException;

	public void createNewEvent(CalendarEvent calendarEvent) throws IOException;

}
