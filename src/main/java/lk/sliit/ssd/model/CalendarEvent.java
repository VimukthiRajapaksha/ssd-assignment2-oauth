/**
 * 
 */
package lk.sliit.ssd.model;

import java.io.Serializable;

/**
 * @author vimukthi_r
 *
 */
public class CalendarEvent implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 123853314763830621L;

	private String title;
	private String description;
	private String date;
	private String startTime;
	private String endTime;
	private String location;
	private String guests;

	@Override
	public String toString() {
		return "CalendarEvent [title=" + title + ", description=" + description + ", date=" + date + ", startTime="
				+ startTime + ", endTime=" + endTime + ", location=" + location + ", guests=" + guests + "]";
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getGuests() {
		return guests;
	}

	public void setGuests(String guests) {
		this.guests = guests;
	}

}
