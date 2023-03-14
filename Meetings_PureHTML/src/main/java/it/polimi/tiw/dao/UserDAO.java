package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.polimi.tiw.beans.User;

public class UserDAO {
	private Connection connection;
	
	public UserDAO(Connection connection) {
		this.connection = connection;
	}
	
	public User getUserByUsername(String username) throws SQLException{
		User user = null;
		String performedAction = " finding a user by username";
		String query = "SELECT * FROM user WHERE username = ?";
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		try {
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, username);
			resultSet = preparedStatement.executeQuery();
			
			while(resultSet.next()) {
				user = new User();
				user.setUsername(resultSet.getString("username"));
				user.setEmail(resultSet.getString("email"));
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
		return user;
	}
	
	public List<User> getOtherUser(String username) throws SQLException {
		List<User> users = new ArrayList<User>();
		
		String performedAction = " finding users without current user";
		String query = "SELECT * FROM user WHERE username != ?";
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		try {
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, username);
			resultSet = preparedStatement.executeQuery();
			
			while(resultSet.next()) {
				User user = new User();
				user = getUserByUsername(resultSet.getString("username"));
				users.add(user);
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
		
		return users;
	}
	
	public User checkCredentials(String username, String password) throws SQLException {
		String query = "SELECT username, email FROM user  WHERE username = ? AND password =?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, username);
			pstatement.setString(2, password);
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst())    //no results, credential check failed
					return null;
				else {
					result.next();
					User user = new User();
					user.setUsername(result.getString("username"));
					user.setEmail(result.getString("email"));
					return user;
				}
			}
		}
	}
	
	public void registerUser(String username, String email, String password) throws SQLException {
		String performedAction = " registering a new user in the database";
		String queryAddUser = "INSERT INTO user (username,email,password) VALUES(?,?,?)";
		PreparedStatement preparedStatementAddUser = null;	
		
		try {
			preparedStatementAddUser = connection.prepareStatement(queryAddUser);
			preparedStatementAddUser.setString(1, username);
			preparedStatementAddUser.setString(2, email);
			preparedStatementAddUser.setString(3, password);
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