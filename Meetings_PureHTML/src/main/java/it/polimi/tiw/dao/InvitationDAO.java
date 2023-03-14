package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import it.polimi.tiw.beans.Meeting;

public class InvitationDAO {
	private Connection connection;
	
	public InvitationDAO(Connection connection) {
		this.connection = connection;
	}
	
	public void addInvitation(String username, Meeting meeting) throws SQLException {
		String performedAction = " registering a new invitation in the database";
		String queryAddUser = "INSERT INTO invitation (username,title,date) VALUES(?,?,?)";
		PreparedStatement preparedStatementAddUser = null;	
		
		try {
			preparedStatementAddUser = connection.prepareStatement(queryAddUser);
			preparedStatementAddUser.setString(1, username);
			preparedStatementAddUser.setString(2, meeting.getTitle());
			preparedStatementAddUser.setDate(3, meeting.getDate());
			preparedStatementAddUser.executeUpdate();
		}catch(SQLException e) {
			throw new SQLException("Error accessing the DB when" + performedAction);
		}finally {
			try {
				preparedStatementAddUser.close();
			}catch (Exception e) {
				throw new SQLException("Error closing the statement when" + performedAction);
			}
		}
	}
	
}