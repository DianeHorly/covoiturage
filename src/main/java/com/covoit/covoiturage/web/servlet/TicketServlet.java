package com.covoit.covoiturage.web.servlet;

import com.covoit.covoiturage.dao.BookingDao;
import com.covoit.covoiturage.dao.RideDao;
import com.covoit.covoiturage.model.Booking;
import com.covoit.covoiturage.model.Ride;
import com.covoit.covoiturage.model.User;
import com.covoit.covoiturage.util.BusinessException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.bson.types.ObjectId;

import java.io.IOException;

/**
 * Affiche le ticket pour une réservation donnée.
 * URL: /ticket?bookingId=...
 */
@WebServlet("/ticket")
public class TicketServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;


    private final BookingDao bookingDao = new BookingDao();
    private final RideDao rideDao = new RideDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        User user = (session == null) ? null : (User) session.getAttribute("user");

        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String bookingIdStr = req.getParameter("bookingId");
        if (bookingIdStr == null || bookingIdStr.isBlank()) {
            resp.sendRedirect(req.getContextPath() + "/myBookings");
            return;
        }

        try {
            ObjectId bookingId = new ObjectId(bookingIdStr);
            Booking booking = bookingDao.findByPassengerId(user.getId()).stream()
                    .filter(b -> bookingId.equals(b.getId()))
                    .findFirst()
                    .orElse(null);

            if (booking == null) {
                throw new BusinessException("Réservation introuvable pour cet utilisateur.");
            }

            // On récupère le trajet
            Ride ride = rideDao.findById(booking.getRideId());

            req.setAttribute("booking", booking);
            req.setAttribute("ride", ride);

            req.getRequestDispatcher("/WEB-INF/jsp/ticket.jsp")
               .forward(req, resp);

        } catch (IllegalArgumentException e) {
            resp.sendRedirect(req.getContextPath() + "/myBookings");
        } catch (BusinessException e) {
            req.setAttribute("error", e.getMessage());
            req.getRequestDispatcher("/WEB-INF/jsp/ticket.jsp")
               .forward(req, resp);
        }
    }
}
