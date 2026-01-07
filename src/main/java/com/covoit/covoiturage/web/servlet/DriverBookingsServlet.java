package com.covoit.covoiturage.web.servlet;

import com.covoit.covoiturage.dao.BookingDao;
import com.covoit.covoiturage.dao.RideDao;
import com.covoit.covoiturage.dao.UserDao;
import com.covoit.covoiturage.model.Booking;
import com.covoit.covoiturage.model.Ride;
import com.covoit.covoiturage.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.covoit.covoiturage.model.DriverBookingView;


//import org.bson.types.ObjectId;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Liste les réservations concernant les trajets du conducteur.
 */
@WebServlet("/driver/bookings")
public class DriverBookingsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final RideDao rideDao = new RideDao();
    private final BookingDao bookingDao = new BookingDao();
    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        User driver = (session == null) ? null : (User) session.getAttribute("user");

        if (driver == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // Récupérer tous les trajets de ce conducteur
        //List<Ride> driverRides = new ArrayList<>();
        // Petite méthode utilitaire dans RideDao à ajouter si tu veux :
        // findByDriverId(ObjectId driverId)

        // Si tu ne l'as pas encore, implémente-la comme dans BookingDao (findByPassengerId)

        // Pour l'instant, on suppose qu'on a rideDao.findByDriverId(...)
        // Sinon, tu peux le faire plus tard.

        // Tous les trajets du conducteur
        List<Ride> driverRides = rideDao.findByDriverId(driver.getId());
        if (driverRides == null) {
            driverRides = new ArrayList<>();
        }
        
        // Construit la liste DriverBookingView -> envoyée à la JSP
        List<DriverBookingView> views = new ArrayList<>();

        for (Ride ride : driverRides) {
            List<Booking> bookings = bookingDao.findByRideId(ride.getId());
            if (bookings == null) continue;
            
            for (Booking b : bookings) {
            	User passenger = userDao.findById(b.getPassengerId());
                DriverBookingView v = new DriverBookingView();
                v.setBooking(b);
                v.setRide(ride);
                v.setPassenger(passenger);

                views.add(v);
            }
        }

        req.setAttribute("driverBookings", views);
        req.getRequestDispatcher("/WEB-INF/jsp/driver_bookings.jsp")
           .forward(req, resp);
    }
}
