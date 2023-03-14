package it.polimi.tiw.beans;

import java.sql.Date;
import java.sql.Time;

public class Meeting {
	private String title;
	private Date date;
	private Time time;
	private float duration;
	private int maxNumberOfParticipants;
	private String createdBy;
	
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	
	public Time getTime() {
		return time;
	}
	public void setTime(Time time) {
		this.time = time;
	}
	
	public float getDuration() {
		return duration;
	}
	public void setDuration(float duration) {
		this.duration = duration;
	}
	
	public int getMaxNumberOfParticipants() {
		return maxNumberOfParticipants;
	}
	public void setMaxNumberOfParticipants(int maxNumberOfParticipants) {
		this.maxNumberOfParticipants = maxNumberOfParticipants;
	}
	
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
		
}