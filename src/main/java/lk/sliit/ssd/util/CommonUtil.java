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

}
