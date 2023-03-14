package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import it.polimi.tiw.beans.Meeting;

public class MeetingDAO {
	private Connection connection;
	
	public MeetingDAO(Connection connection) {
		this.connection = connection;
	}
	
	private String getMeToday() {
		String pattern = "yyyy-MM-dd";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		String date = simpleDateFormat.format(new Date());
		return date;
	}
	
	public Meeting getMeetingByTitleAndDate(String title, String date) throws SQLException{
		Meeting meeting = null;
		String performedAction = " finding a meeting by title";
		String query = "SELECT * FROM meeting WHERE title = ? AND date = ?";
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		try {
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, title);
			preparedStatement.setString(2, date);
			resultSet = preparedStatement.executeQuery();
			
			while(resultSet.next()) {
				meeting = new Meeting();
				meeting.setTitle(resultSet.getString("title"));
				meeting.setDate(resultSet.getDate("date"));
				meeting.setTime(resultSet.getTime("time"));
				meeting.setDuration(resultSet.getFloat("duration"));
				meeting.setMaxNumberOfParticipants(resultSet.getInt("maxNumberOfParticipants"));
				meeting.setCreatedBy(resultSet.getString("createdBy"));
			}
			
		}catch(SQLException e) {
			throw new SQLException("Error accessing the DB when" + performedAction);
		}finally {
			try {
				resultSet.close();
			}catch (Exception e) {
				throw new SQLException("Error closing the result set when" + performedAction);
			}
			try {
				preparedStatement.close();
			}catch (Exception e) {
				throw new SQLException("Error closing the statement when" + performedAction);
			}
		}
		return meeting;
	}
	
	public List<Meeting> getMyMeetingByUsername(String username) throws SQLException {
		List<Meeting> myMeetings = new ArrayList<>();
		String performedAction = " finding meetings created by " + username;
		String query = "SELECT * FROM meeting WHERE createdBy = ? AND date >= ?";
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		try {
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, username);
			preparedStatement.setString(2, getMeToday());
			resultSet = preparedStatement.executeQuery();
			
			while(resultSet.next()) {
				Meeting meeting = new Meeting();
				meeting = getMeetingByTitleAndDate(resultSet.getString("title"), resultSet.getDate("date").toString());
				myMeetings.add(meeting);
			}
			
		}catch(SQLException e) {
			throw new SQLException("Error accessing the DB when" + performedAction);
		}finally {
			try {
				resultSet.close();
			}catch (Exception e) {
				throw new SQLException("Error closing the result set when" + performedAction);
			}
			try {
				preparedStatement.close();
			}catch (Exception e) {
				throw new SQLException("Error closing the statement when" + performedAction);
			}
		}		
		
		return myMeetings;
	}
	
	public List<Meeting> getOtherMeetingByUsername(String username) throws SQLException{

		List<Meeting> meetings = new ArrayList<>();
		String performedAction = " finding meetings by username";
		String query = "SELECT DISTINCT M.* FROM meeting AS M JOIN invitation AS I ON M.title=I.title JOIN user AS U ON U.username=I.username WHERE I.username = ? and M.date >= ?  and M.date = I.date";
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		try {
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, username);
			preparedStatement.setString(2, getMeToday());
			resultSet = preparedStatement.executeQuery();
			
			while(resultSet.next()) {
				Meeting meeting = new Meeting();
				meeting = getMeetingByTitleAndDate(resultSet.getString("title"), resultSet.getDate("date").toString());
				meetings.add(meeting);
			}
			
		}catch(SQLException e) {
			throw new SQLException("Error accessing the DB when" + performedAction);
		}finally {
			try {
				resultSet.close();
			}catch (Exception e) {
				throw new SQLException("Error closing the result set when" + performedAction);
			}
			try {
				preparedStatement.close();
			}catch (Exception e) {
				throw new SQLException("Error closing the statement when" + performedAction);
			}
		}		
		
		return meetings;
	}
	
	public void createMeeting(String title,Date date,Time time,float duration,int maxNumberOfParticipants,String createdBy) throws SQLException {
		String performedAction = " registering a new meeting in the database";
		String query = "INSERT into meeting (title, date, time, duration, maxNumberOfParticipants, createdBy) VALUES(?, ?, ?, ?, ?, ?)";
		PreparedStatement pstatement = null;
		
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setString(1, title);
			pstatement.setDate(2, new java.sql.Date(date.getTime()));
			pstatement.setTime(3, new java.sql.Time(time.getTime()));
			pstatement.setFloat(4, duration);
			pstatement.setInt(5, maxNumberOfParticipants);
			pstatement.setString(6, createdBy);
			
			pstatement.executeUpdate();
		}catch(SQLException e) {
			throw new SQLException("Error accessing the DB when" + performedAction);
		}finally {
			try {
				pstatement.close();
			}catch (Exception e) {
				throw new SQLException("Error closing the statement when" + performedAction);
			}
		}
	}
	
}