package com.covoit.covoiturage.web.servlet;

import com.covoit.covoiturage.dao.RideDao;

import com.covoit.covoiturage.model.Booking;
import com.covoit.covoiturage.model.BookingWithRide;
import com.covoit.covoiturage.model.Ride;
import com.covoit.covoiturage.model.User;
import com.covoit.covoiturage.service.BookingService;
import com.covoit.covoiturage.util.BusinessException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
//import org.bson.types.ObjectId;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Affiche la liste des réservations de l'utilisateur connecté.
 * URL : /myBookings
 */
@WebServlet({"/myBookings", "/my_bookings"})
public class MyBookingsServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final BookingService bookingService = new BookingService();
    private final RideDao rideDao = new RideDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        User user = (session == null) ? null : (User) session.getAttribute("user");

        // AuthFilter est censé déjà filtrer, mais on re-vérifie par sécurité
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String error = null;
        List<BookingWithRide> list = new ArrayList<>();

        try {
            List<Booking> bookings = bookingService.getBookingsForUser(user);

            for (Booking b : bookings) {
                Ride ride = rideDao.findById(b.getRideId());
                BookingWithRide bwr = new BookingWithRide();
                bwr.setBooking(b);
                bwr.setRide(ride);
                list.add(bwr);
            }

        } catch (BusinessException e) {
            error = e.getMessage();
        }

        req.setAttribute("error", error);
        req.setAttribute("bookingsWithRide", list);

        req.getRequestDispatcher("/WEB-INF/jsp/my_bookings.jsp")
           .forward(req, resp);
    }
}
