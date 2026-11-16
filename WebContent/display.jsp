<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="java.util.List,com.library.Book" %>
<%!
    private String html(Object value) {
        if (value == null) return "&mdash;";
        return value.toString().replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
%>
<%
    response.setHeader("Cache-Control", "no-store");
    if (request.getAttribute("books") == null && request.getAttribute("error") == null) {
        request.getRequestDispatcher("/DisplayServlet").forward(request, response);
        return;
    }
    List<Book> books = (List<Book>) request.getAttribute("books");
    boolean hasError = request.getAttribute("error") != null;
    boolean registrationError = Boolean.TRUE.equals(request.getAttribute("registrationError"));
    String context = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Book catalogue | Folio Library</title>
    <link rel="stylesheet" href="<%= html(context) %>/assets/style.css">
</head>
<body>
    <a class="skip-link" href="#main">Skip to content</a>
    <header class="site-header">
        <a class="brand" href="<%= html(context) %>/index.html" aria-label="Folio Library home"><span class="brand-mark" aria-hidden="true">F.</span><span>folio<span class="brand-label">LIBRARY DESK</span></span></a>
        <nav aria-label="Main navigation"><a class="nav-link" href="<%= html(context) %>/index.html">Register a book</a><a class="nav-link active" href="<%= html(context) %>/DisplayServlet" aria-current="page">View catalogue <span aria-hidden="true">&#8599;</span></a></nav>
    </header>
    <main id="main" class="catalogue">
        <div class="catalogue-heading"><div><p class="eyebrow">THE LIBRARY DESK / 02</p><h1><%= registrationError ? "Let's check those details." : "The book collection." %></h1><p><%= registrationError ? "Review the message below and return to the registration form." : "Every title, every detail. Your library at a glance." %></p></div><a class="button primary" href="<%= html(context) %>/index.html"><span aria-hidden="true">+</span> Register a book</a></div>
        <% if (request.getAttribute("success") != null) { %>
        <div class="notice success" role="status"><span aria-hidden="true">&#10003;</span><%= html(request.getAttribute("success")) %></div>
        <% } %>
        <% if (hasError) { %>
        <div class="notice error" role="alert"><%= html(request.getAttribute("error")) %></div>
        <div class="error-actions"><a class="button secondary" href="<%= html(context) %>/index.html">Return to registration</a><a class="text-link" href="<%= html(context) %>/DisplayServlet">Try loading catalogue &#8594;</a></div>
        <% } else { %>
        <section class="catalogue-card" aria-labelledby="records-title">
            <div class="table-toolbar"><h2 id="records-title">All registered books <span class="count"><%= books.size() %></span></h2><span class="table-hint">Arranged by Book ID</span></div>
            <% if (books.isEmpty()) { %>
            <div class="empty-state"><span class="empty-mark" aria-hidden="true">F.</span><h2>No books registered yet.</h2><p>A new collection starts with a single book.</p><a class="text-link" href="<%= html(context) %>/index.html">Register your first book &#8594;</a></div>
            <% } else { %>
            <p id="table-help" class="scroll-hint">On smaller screens, scroll across to see every detail.</p>
            <div class="table-scroll" tabindex="0" role="region" aria-label="Book records" aria-describedby="table-help">
                <table>
                    <caption class="sr-only">Registered library books and available quantities</caption>
                    <thead><tr><th scope="col">Book ID</th><th scope="col">Book Title</th><th scope="col">Author Name</th><th scope="col">Publisher Name</th><th scope="col">Category</th><th scope="col">Edition</th><th scope="col">Publication Year</th><th scope="col" class="number">Price</th><th scope="col" class="number">Available Quantity</th></tr></thead>
                    <tbody>
                    <% for (Book book : books) { %>
                    <tr>
                        <td class="book-id">#<%= book.getBookId() %></td>
                        <th scope="row" class="book-title"><%= html(book.getBookTitle()) %></th>
                        <td><%= html(book.getAuthorName()) %></td>
                        <td><%= html(book.getPublisherName()) %></td>
                        <td><span class="category-tag"><%= html(book.getCategory()) %></span></td>
                        <td><%= html(book.getEdition()) %></td>
                        <td><%= html(book.getPublicationYear()) %></td>
                        <td class="number"><%= html(book.getPrice() == null ? null : book.getPrice().toPlainString()) %></td>
                        <td class="number"><span class="quantity"><%= html(book.getQuantity()) %></span></td>
                    </tr>
                    <% } %>
                    </tbody>
                </table>
            </div>
            <% } %>
            <div class="table-footer">Well kept. Ready to be discovered.</div>
        </section>
        <% } %>
    </main>
    <footer class="site-footer"><span>FOLIO / LIBRARY MANAGEMENT SYSTEM</span><span>A place for every story.</span></footer>
</body>
</html>
