package com.library;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class DisplayServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-store");
        List<Book> books = new ArrayList<>();
        String sql = "SELECT BOOK_ID, BOOK_TITLE, AUTHOR_NAME, PUBLISHER_NAME, CATEGORY, "
                   + "EDITION, PUBLICATION_YEAR, PRICE, QUANTITY FROM BOOK ORDER BY BOOK_ID";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            while (rows.next()) {
                int year = rows.getInt("PUBLICATION_YEAR");
                Integer optionalYear = rows.wasNull() ? null : year;
                int quantity = rows.getInt("QUANTITY");
                Integer optionalQuantity = rows.wasNull() ? null : quantity;
                books.add(new Book(rows.getInt("BOOK_ID"), rows.getString("BOOK_TITLE"),
                        rows.getString("AUTHOR_NAME"), rows.getString("PUBLISHER_NAME"),
                        rows.getString("CATEGORY"), rows.getString("EDITION"),
                        optionalYear, rows.getBigDecimal("PRICE"), optionalQuantity));
            }
            HttpSession session = request.getSession(false);
            if (session != null && Boolean.TRUE.equals(session.getAttribute("registrationSuccess"))) {
                request.setAttribute("success", "Book Registered Successfully!");
                session.removeAttribute("registrationSuccess");
            }
        } catch (SQLException e) {
            log("Unable to load book records.", e);
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            request.setAttribute("error", "Unable to load books right now. Please check the database setup and try again.");
        }
        request.setAttribute("books", books);
        request.getRequestDispatcher("/display.jsp").forward(request, response);
    }
}
