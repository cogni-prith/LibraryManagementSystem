import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

/** Runs real HTTP requests through Tomcat, JSP compilation and test-only H2 JDBC. */
public class SmokeTest {
    private static final String DB = "jdbc:h2:mem:library;MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
    private static String base;
    private static int checks;

    public static void main(String[] args) throws Exception {
        File project = new File(args[0]);
        File classes = new File(args[1]);
        Class.forName("org.h2.Driver");
        try (Connection c = DriverManager.getConnection(DB, "sa", ""); Statement s = c.createStatement()) {
            String ddl = new String(Files.readAllBytes(new File(project, "database/postgresql.sql").toPath()), StandardCharsets.UTF_8);
            s.execute(ddl.trim().replaceFirst(";\\s*$", ""));
        }
        Tomcat server = new Tomcat();
        server.setBaseDir(Files.createTempDirectory("library-tomcat-").toString());
        server.setPort(0);
        server.getConnector();
        Context app = server.addWebapp("/library", new File(project, "WebContent").getAbsolutePath());
        app.setParentClassLoader(SmokeTest.class.getClassLoader());
        StandardRoot resources = new StandardRoot(app);
        resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes", classes.getAbsolutePath(), "/"));
        app.setResources(resources);
        CookieHandler.setDefault(new CookieManager());
        try {
            server.start();
            base = "http://127.0.0.1:" + server.getConnector().getLocalPort() + "/library";
            Reply form = request("GET", "/index.html", null);
            check(form.status == 200 && form.body.contains("Book Title"), "registration form is available");
            check(request("GET", "/DisplayServlet", null).body.contains("No books registered yet."), "empty table has guidance");
            Map<String, String> book = validBook();
            Reply saved = request("POST", "/RegisterServlet", book);
            check(saved.status == 302, "successful registration redirects");
            Reply list = request("GET", saved.location.replace(base, "").replaceFirst("^/library", ""), null);
            check(list.status == 200 && list.body.contains("Book Registered Successfully!"), "success message appears");
            check(list.body.contains("Effective &lt;Java&gt; &amp; &quot;JDBC&quot;") && list.body.contains("O&#39;Neil"), "stored text is HTML escaped");
            check(list.body.contains("499.50") && list.body.contains("2018"), "numeric fields reach the table");
            check(!request("GET", "/DisplayServlet", null).body.contains("Book Registered Successfully!"), "success message is one use");
            Reply duplicate = request("POST", "/RegisterServlet", book);
            check(duplicate.status == 409 && duplicate.body.contains("already exists"), "duplicate ID is explained");
            String[][] invalid = {{"bookId", "0"}, {"bookId", "100000"}, {"bookId", "2147483648"},
                {"bookTitle", "   "}, {"bookTitle", repeat('x', 151)}, {"authorName", repeat('x', 101)},
                {"publicationYear", "999"}, {"publicationYear", "year"}, {"price", "-1"},
                {"price", "1.234"}, {"price", "1e3"}, {"price", "100000000"},
                {"quantity", "-1"}, {"quantity", "100000"}, {"quantity", "1.5"}};
            for (String[] pair : invalid) {
                Map<String, String> bad = validBook();
                bad.put("bookId", "102");
                bad.put(pair[0], pair[1]);
                check(request("POST", "/RegisterServlet", bad).status == 400, "invalid " + pair[0] + " rejected: " + pair[1]);
            }
            Map<String, String> missing = validBook();
            missing.remove("bookId");
            check(request("POST", "/RegisterServlet", missing).status == 400, "missing parameter is rejected");
            try (Connection c = DriverManager.getConnection(DB, "sa", ""); Statement s = c.createStatement(); ResultSet r = s.executeQuery("SELECT COUNT(*) FROM BOOK")) {
                r.next(); check(r.getInt(1) == 1, "failed requests never insert rows");
            }
            Map<String, String> optional = validBook();
            optional.put("bookId", "102");
            optional.put("bookTitle", "Learning JDBC");
            for (String name : new String[]{"authorName", "publisherName", "category", "edition", "publicationYear"}) optional.put(name, "");
            optional.put("price", "0"); optional.put("quantity", "0");
            check(request("POST", "/RegisterServlet", optional).status == 302, "optional blanks and zero stock/price are accepted");
            Reply optionalList = request("GET", "/DisplayServlet", null);
            check(optionalList.status == 200 && optionalList.body.contains("Learning JDBC") && !optionalList.body.contains(">null<"), "null columns display cleanly");
            check(request("GET", "/display.jsp", null).body.contains("Learning JDBC"), "direct JSP URL loads records through servlet");
            Map<String, String> injection = validBook();
            injection.put("bookId", "103"); injection.put("bookTitle", "x'); DROP TABLE BOOK; --");
            check(request("POST", "/RegisterServlet", injection).status == 302, "SQL-like text is stored as data");
            try (Connection c = DriverManager.getConnection(DB, "sa", ""); Statement s = c.createStatement(); ResultSet r = s.executeQuery("SELECT BOOK_TITLE, PRICE, QUANTITY FROM BOOK WHERE BOOK_ID = 101")) {
                r.next(); check(r.getString(1).equals(book.get("bookTitle")) && r.getBigDecimal(2).toPlainString().equals("499.50") && r.getInt(3) == 7, "original data and exact decimal price are preserved");
            }
            Map<String, String> maximum = validBook();
            maximum.put("bookId", "99999"); maximum.put("publicationYear", "9999");
            maximum.put("price", "99999999.99"); maximum.put("quantity", "99999");
            check(request("POST", "/RegisterServlet", maximum).status == 302, "schema boundary values are accepted");
            try (Connection c = DriverManager.getConnection(DB, "sa", ""); Statement s = c.createStatement()) { s.execute("DROP TABLE BOOK"); }
            Reply unavailable = request("GET", "/DisplayServlet", null);
            check(unavailable.status == 503 && unavailable.body.contains("Unable to load books") && !unavailable.body.contains("JdbcSQL"), "database failure has a safe error page");
            Reply failedSave = request("POST", "/RegisterServlet", validBook());
            check(failedSave.status == 503 && failedSave.body.contains("Unable to register") && !failedSave.body.contains("jdbc:h2"), "failed save does not expose JDBC details");
            System.out.println("PASS: " + checks + " HTTP/JDBC/JSP checks");
        } finally { server.stop(); server.destroy(); }
    }

    private static Map<String, String> validBook() {
        Map<String, String> b = new LinkedHashMap<>();
        b.put("bookId", "101"); b.put("bookTitle", "Effective <Java> & \"JDBC\"");
        b.put("authorName", "Joshua O'Neil"); b.put("publisherName", "Library Press");
        b.put("category", "Technology"); b.put("edition", "3rd"); b.put("publicationYear", "2018");
        b.put("price", "499.50"); b.put("quantity", "7"); return b;
    }

    private static Reply request(String method, String path, Map<String, String> fields) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(base + path).openConnection();
        c.setInstanceFollowRedirects(false); c.setRequestMethod(method);
        c.setConnectTimeout(5000); c.setReadTimeout(15000);
        if (fields != null) {
            StringBuilder form = new StringBuilder();
            for (Map.Entry<String, String> e : fields.entrySet()) {
                if (form.length() > 0) form.append('&');
                form.append(URLEncoder.encode(e.getKey(), "UTF-8")).append('=').append(URLEncoder.encode(e.getValue(), "UTF-8"));
            }
            c.setDoOutput(true); c.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            try (java.io.OutputStream out = c.getOutputStream()) { out.write(form.toString().getBytes(StandardCharsets.UTF_8)); }
        }
        int status = c.getResponseCode(); String body = "";
        InputStream stream = status >= 400 ? c.getErrorStream() : c.getInputStream();
        if (stream != null) {
            try (InputStream in = stream; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[4096]; int count;
                while ((count = in.read(buffer)) != -1) out.write(buffer, 0, count);
                body = new String(out.toByteArray(), StandardCharsets.UTF_8);
            }
        }
        Reply reply = new Reply(status, body, c.getHeaderField("Location")); c.disconnect(); return reply;
    }
    private static String repeat(char c, int count) { char[] chars = new char[count]; java.util.Arrays.fill(chars, c); return new String(chars); }
    private static void check(boolean condition, String description) {
        if (!condition) throw new AssertionError(description); checks++;
    }
    private static class Reply {
        final int status; final String body; final String location;
        Reply(int status, String body, String location) { this.status = status; this.body = body; this.location = location; }
    }
}
