package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.sql.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;

import it.polimi.tiw.beans.Meeting;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.UserDAO;
import it.polimi.tiw.utilities.ConnectionHandler;

/**
 * Servlet implementation class CreateNewMeeting
 */
@WebServlet("/SaveMeetingInSession")
public class SaveMeetingInSession extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SaveMeetingInSession() {
        super();
    }
    
    public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}
    
    private Date getMeYesterday() {
		return new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		//check if user is correctly logged in
		HttpSession session = request.getSession();
		if (session.isNew() || session.getAttribute("user") == null) {
			String loginpath = getServletContext().getContextPath() + "/index.html";
			response.sendRedirect(loginpath);
			return;
		}

		boolean isBadRequest = false;
		String title = null;
		Date date = null;
		Time time = null;
		float duration = 0;
		int maxNumberOfParticipants = 1;
		try {
			title = StringEscapeUtils.escapeJava(request.getParameter("title"));
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			java.util.Date utilDate = new java.util.Date();
			utilDate = sdf.parse(request.getParameter("date"));
		    date = new java.sql.Date(utilDate.getTime());
			
			SimpleDateFormat sdf2 = new SimpleDateFormat("hh:mm");
			long ms = sdf2.parse(request.getParameter("time")).getTime();
			time = new Time(ms);
			
			duration = Float.parseFloat(request.getParameter("duration"));
			maxNumberOfParticipants = Integer.parseInt(request.getParameter("maxNumberOfParticipants"));
			isBadRequest = title.isEmpty() || getMeYesterday().after(date);
		} catch (NumberFormatException | NullPointerException | ParseException e) {
			isBadRequest = true;
			e.printStackTrace();
		}
		
		if (isBadRequest) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parametri incorretti o mancanti");
			return;
		}

		User user = (User) session.getAttribute("user");
		Meeting meeting = new Meeting();
		
		meeting.setTitle(title);
		meeting.setDate(date);
		meeting.setTime(time);
		meeting.setDuration(duration);
		meeting.setMaxNumberOfParticipants(maxNumberOfParticipants);
		meeting.setCreatedBy(user.getUsername());

		UserDAO userDAO = new UserDAO(connection);
		
		List<User> usersChecked = new ArrayList<User>();
		usersChecked.add(user);
		List<User> users = new ArrayList<User>();
		
		try {
			users = userDAO.getOtherUser(user.getUsername());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		request.getSession().setAttribute("meeting", meeting);
		request.getSession().setAttribute("attemps", 0);
		request.getSession().setAttribute("usersChecked", usersChecked);
		request.getSession().setAttribute("users", users);
		request.getSession().setAttribute("errorMsg", "");
		String path = getServletContext().getContextPath() + "/GoToAnagrafica";
		response.sendRedirect(path);
	}

	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
