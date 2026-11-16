package com.library;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RegisterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        try {
            int bookId = integer(request.getParameter("bookId"), "Book ID", 1, 99999, true);
            String title = text(request.getParameter("bookTitle"), "Book title", 150, true);
            String author = text(request.getParameter("authorName"), "Author name", 100, false);
            String publisher = text(request.getParameter("publisherName"), "Publisher name", 100, false);
            String category = text(request.getParameter("category"), "Category", 50, false);
            String edition = text(request.getParameter("edition"), "Edition", 20, false);
            Integer year = integer(request.getParameter("publicationYear"), "Publication year", 1000, 9999, false);
            BigDecimal price = price(request.getParameter("price"));
            int quantity = integer(request.getParameter("quantity"), "Available quantity", 0, 99999, true);

            String sql = "INSERT INTO BOOK (BOOK_ID, BOOK_TITLE, AUTHOR_NAME, PUBLISHER_NAME, "
                       + "CATEGORY, EDITION, PUBLICATION_YEAR, PRICE, QUANTITY) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection connection = DBConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, bookId);
                statement.setString(2, title);
                statement.setString(3, author);
                statement.setString(4, publisher);
                statement.setString(5, category);
                statement.setString(6, edition);
                if (year == null) {
                    statement.setNull(7, Types.NUMERIC);
                } else {
                    statement.setInt(7, year);
                }
                statement.setBigDecimal(8, price);
                statement.setInt(9, quantity);
                if (statement.executeUpdate() != 1) {
                    throw new SQLException("Expected one inserted book record.");
                }
            }
            request.getSession().setAttribute("registrationSuccess", Boolean.TRUE);
            response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/DisplayServlet"));
        } catch (IllegalArgumentException e) {
            showError(request, response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                showError(request, response, HttpServletResponse.SC_CONFLICT,
                        "A book with this Book ID already exists. Please use a different ID.");
            } else if ("22001".equals(e.getSQLState())) {
                showError(request, response, HttpServletResponse.SC_BAD_REQUEST,
                        "A text value is too long for the database. Please shorten it and try again.");
            } else {
                log("Unable to register book.", e);
                showError(request, response, HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                        "Unable to register the book right now. Please check the database setup and try again.");
            }
        }
    }

    private static String text(String value, String label, int max, boolean required) {
        String cleaned = value == null ? "" : value.trim();
        if (cleaned.isEmpty()) {
            if (required) {
                throw new IllegalArgumentException(label + " is required.");
            }
            return null;
        }
        if (cleaned.length() > max) {
            throw new IllegalArgumentException(label + " must be at most " + max + " characters.");
        }
        return cleaned;
    }

    private static Integer integer(String value, String label, int min, int max, boolean required) {
        String cleaned = value == null ? "" : value.trim();
        if (cleaned.isEmpty() && !required) {
            return null;
        }
        if (!cleaned.matches("[0-9]{1,5}")) {
            throw new IllegalArgumentException(label + " must be a whole number from " + min + " to " + max + ".");
        }
        int number = Integer.parseInt(cleaned);
        if (number < min || number > max) {
            throw new IllegalArgumentException(label + " must be from " + min + " to " + max + ".");
        }
        return number;
    }

    private static BigDecimal price(String value) {
        String cleaned = value == null ? "" : value.trim();
        if (!cleaned.matches("[0-9]{1,8}(\\.[0-9]{1,2})?")) {
            throw new IllegalArgumentException("Price must be from 0 to 99999999.99, with at most two decimal places.");
        }
        return new BigDecimal(cleaned);
    }

    private void showError(HttpServletRequest request, HttpServletResponse response,
                           int status, String message) throws ServletException, IOException {
        response.setStatus(status);
        request.setAttribute("error", message);
        request.setAttribute("registrationError", Boolean.TRUE);
        request.getRequestDispatcher("/display.jsp").forward(request, response);
    }
}
