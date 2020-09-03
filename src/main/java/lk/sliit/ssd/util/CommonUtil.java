/**
 * 
 */
package lk.sliit.ssd.util;

import java.util.Collections;
import java.util.List;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.CalendarScopes;

/**
 * @author vimukthi_r
 *
 */
public class CommonUtil {

	private CommonUtil() {
	}

	public static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	public static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);

	public static final String GOOGLE_CALLBACK_URL = "http://localhost:8083/oauth";
	
	public static final String RFC_3339_DATETIME_FORMAT = "%sT%s:00+05:30";
	public static final String UTC_SL_TIME_ZONE = "Asia/Colombo";
	
	public static final String REDIRECT_TO_EVENTS_PAGE = "redirect:/events";
	public static final String ERROR = "error";

}
