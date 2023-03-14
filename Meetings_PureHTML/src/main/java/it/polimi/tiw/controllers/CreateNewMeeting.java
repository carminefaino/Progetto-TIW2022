package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.polimi.tiw.beans.Meeting;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.InvitationDAO;
import it.polimi.tiw.dao.MeetingDAO;
import it.polimi.tiw.utilities.ConnectionHandler;

/**
 * Servlet implementation class CreateNewMeeting
 */
@WebServlet("/CreateNewMeeting")
public class CreateNewMeeting extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	List<User> users = new ArrayList<User>();
	private Meeting meeting = null;
	private User user = null;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CreateNewMeeting() {
        super();
    }
    
    public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String[] checkedIds = request.getParameterValues("users");
		InvitationDAO invitationDAO = new InvitationDAO(connection);
		int attemps = (int) request.getSession().getAttribute("attemps");
		
		MeetingDAO meetingDAO = new MeetingDAO(connection);
		Meeting alreadyExists = null;
		
		HttpSession session = request.getSession();
		meeting = (Meeting) session.getAttribute("meeting");
		user = (User) session.getAttribute("user");
		users = (List<User>) session.getAttribute("users");
		
		//check if the meeting already exists
		try {
			alreadyExists = meetingDAO.getMeetingByTitleAndDate(meeting.getTitle(), meeting.getDate().toString());
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		
		if(alreadyExists != null) {
			request.getSession().setAttribute("user", user);
			String error = "meetingAlreadyExists";
			request.getSession().setAttribute("error", error);
			String path = getServletContext().getContextPath() + "/GoToCancellazionePage";
			response.sendRedirect(path);
		} else {
			//check if the user has selected at least one guest
			if(checkedIds != null) {
				if(checkedIds.length < meeting.getMaxNumberOfParticipants()) {
					
					try {
						meetingDAO.createMeeting(meeting.getTitle(), meeting.getDate(), meeting.getTime(), meeting.getDuration(), meeting.getMaxNumberOfParticipants(), meeting.getCreatedBy());
					} catch (SQLException e) {
						e.printStackTrace();
					}
					for(String s : checkedIds) {
						try {
							invitationDAO.addInvitation(s, meeting);
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
					
					request.getSession().setAttribute("user", user);
					String path = getServletContext().getContextPath() + "/GoToHomePage";
					response.sendRedirect(path);
					
				} else {
					if(attemps == 2) {
						request.getSession().setAttribute("user", user);
						String error = "tooManyUsers";
						request.getSession().setAttribute("error", error);
						String path = getServletContext().getContextPath() + "/GoToCancellazionePage";
						response.sendRedirect(path);
						return;
					}
					int toRemove = checkedIds.length - (meeting.getMaxNumberOfParticipants() - 1);
					
					List<User> usersChecked = new ArrayList<User>();
					for(String s : checkedIds) {
						for(User u : users) {
							if(u.getUsername().equals(s)) {
								usersChecked.add(u);
							}
						}
					}
					
					//reload anagrafica page with checked users and number of attemps
					request.getSession().setAttribute("attemps", attemps + 1);
					request.getSession().setAttribute("usersChecked", usersChecked);
					request.getSession().setAttribute("errorMsg", "Troppi utenti selezionati, eliminarne almeno " + toRemove);
					request.getSession().setAttribute("meeting", meeting);
					request.getSession().setAttribute("user", user);
					String path = getServletContext().getContextPath() + "/GoToAnagrafica";
					response.sendRedirect(path);
				}
			} else {
				request.getSession().setAttribute("attemps", attemps);
				List<User> usersNull = new ArrayList<User>();
				request.getSession().setAttribute("usersChecked", usersNull);
				request.getSession().setAttribute("errorMsg", "Selezionare almeno un utente");
				request.getSession().setAttribute("meeting", meeting);
				request.getSession().setAttribute("user", user);
				String path = getServletContext().getContextPath() + "/GoToAnagrafica";
				response.sendRedirect(path);
			}
		}
	}

}
