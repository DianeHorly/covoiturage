package com.covoit.covoiturage.web.servlet;

import com.covoit.covoiturage.dao.BookingDao;
import com.covoit.covoiturage.model.Booking;
import com.covoit.covoiturage.model.User;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.bson.types.ObjectId;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Génère le QR Code (PNG) pour un ticket donné.
 * URL: /ticket/qrcode?bookingId=...
 */
@WebServlet("/ticket/qrcode")
public class TicketQrServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final BookingDao bookingDao = new BookingDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        User user = (session == null) ? null : (User) session.getAttribute("user");

        if (user == null) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String bookingIdStr = req.getParameter("bookingId");
        if (bookingIdStr == null || bookingIdStr.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            ObjectId bookingId = new ObjectId(bookingIdStr);
            Booking booking = bookingDao.findByPassengerId(user.getId()).stream()
                    .filter(b -> bookingId.equals(b.getId()))
                    .findFirst()
                    .orElse(null);

            if (booking == null || booking.getTicketCode() == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            if (!"CONFIRMED".equalsIgnoreCase(booking.getStatus())) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            

            String data = booking.getTicketCode(); // contenu du QR

            int size = 250;
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, size, size);

            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    int grayValue = (bitMatrix.get(x, y) ? 0 : 0xFFFFFF);
                    image.setRGB(x, y, grayValue);
                }
            }

            resp.setContentType("image/png");
            ImageIO.write(image, "PNG", resp.getOutputStream());

        } catch (IllegalArgumentException | WriterException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
