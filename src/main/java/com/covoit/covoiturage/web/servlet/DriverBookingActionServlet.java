package com.covoit.covoiturage.web.servlet;

import com.covoit.covoiturage.model.User;
import com.covoit.covoiturage.service.BookingService;
import com.covoit.covoiturage.util.BusinessException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Gère les actions du conducteur sur une réservation: confirmer ou refuser.
 */
@WebServlet("/driver/booking/action")
public class DriverBookingActionServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final BookingService bookingService = new BookingService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        User driver = (session == null) ? null : (User) session.getAttribute("user");

        if (driver == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String bookingId = req.getParameter("bookingId");
        String action = req.getParameter("action");
        String message = req.getParameter("message");

        try {
            if ("confirm".equals(action)) {
                bookingService.confirmBooking(driver, bookingId, message);
            } else if ("reject".equals(action)) {
                bookingService.rejectBooking(driver, bookingId, message);
            }

        } catch (BusinessException e) {
            
        }

        resp.sendRedirect(req.getContextPath() + "/driver/bookings");
    }
}
