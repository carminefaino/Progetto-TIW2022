package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;

import it.polimi.tiw.beans.Meeting;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.MeetingDAO;
import it.polimi.tiw.dao.InvitationDAO;
import it.polimi.tiw.utils.ConnectionHandler;

/**
 * Servlet implementation class CreateMeeting
 */
@WebServlet("/CreateMeeting")
@MultipartConfig
public class CreateMeeting extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CreateMeeting() {
        super();
    }
    
    public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");
		boolean isBadRequest = false;
		String title = null;
		Date date = null;
		Time time = null;
		float duration = 0;
		int maxNumberOfParticipants = 1;
		String createdBy = null;
		String[] users =  null;
		
		try {
			title = StringEscapeUtils.escapeJava(request.getParameter("title"));
			
			if(title.isEmpty() || title == "") {
				isBadRequest = true;
			}
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			java.util.Date utilDate = new java.util.Date();
			utilDate = sdf.parse(request.getParameter("date"));
		    date = new java.sql.Date(utilDate.getTime());
			
			SimpleDateFormat sdf2 = new SimpleDateFormat("hh:mm");
			long ms = sdf2.parse(request.getParameter("time")).getTime();
			time = new Time(ms);
			
			duration = Float.parseFloat(request.getParameter("duration"));
			maxNumberOfParticipants = Integer.parseInt(request.getParameter("maxNumberOfParticipants"));
			
			if(maxNumberOfParticipants < 2) {
				isBadRequest = true;
			}
			
			createdBy = user.getUsername();
			
			//Ricevo gli utenti da invitare in una stringa allegata al form. La stringa viene splittata
			users = StringEscapeUtils.escapeJava(request.getParameter("users_checked")).split(",");
		} catch (NumberFormatException | NullPointerException | ParseException e) {
			isBadRequest = true;
			e.printStackTrace();
		}
		
		if (isBadRequest) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Parametri incorretti o mancanti");
			return;
		}
		
		MeetingDAO meetingDAO = new MeetingDAO(connection);
		InvitationDAO invitationDAO = new InvitationDAO(connection);
		Meeting meeting = null;
		
		try {
			meeting = meetingDAO.getMeetingByTitleAndDate(title, date.toString());
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		
		if(meeting != null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().println("Questo meeting esiste gia'!");
			return;
		}
		
		//verifica lato server sul corretto numero di invitati
		if(users.length >= maxNumberOfParticipants) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().println("Il numero di ivitati è superiore al numero consentito dalla riunione");
			return;
		} else {
			try {
				meetingDAO.createMeeting(title, date, time, duration, maxNumberOfParticipants, createdBy);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			try {
				meeting = meetingDAO.getMeetingByTitleAndDate(title, date.toString());
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			for(String s : users) {
				try {
					invitationDAO.addInvitation(s, meeting);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().print(meeting.getTitle());
		}		
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
