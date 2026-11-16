# Library Management System — Book Registration and Display

A self-contained **Eclipse Dynamic Web Project** for the Topic 4 practical assignment. The librarian can register a book and view every registered book in a table. The UI uses local HTML/CSS; it needs no internet connection at runtime.

## Open and run on Windows

Use Eclipse IDE for Enterprise Java and Web Developers, a JDK, **Apache Tomcat 9**, and your existing PostgreSQL database. The source targets Java 8 and uses javax.servlet. No Maven, Gradle, npm, or framework is required.

1. **Copy this entire folder** to your Windows computer, keeping the hidden .project, .classpath and .settings files. If using the ZIP, extract it first.
2. In Eclipse choose **File → Import → General → Existing Projects into Workspace → Select root directory**. Select this LibraryManagementSystem folder, tick the project, then Finish. Do not import it as a plain Java project.
3. Right-click the project → **Properties → Targeted Runtimes**. Select your existing **Apache Tomcat v9.0** runtime and Apply. If the project shows an unbound server runtime, remove the old unbound runtime entry under **Java Build Path → Libraries**, then select your Tomcat runtime again. Under **Java Build Path**, choose an installed JDK for the JRE System Library if needed. The project is configured for the Java 1.8 and Dynamic Web Module 4.0 facets.
4. **Create the BOOK table once.** Connect to your database in pgAdmin Query Tool or psql and execute database/postgresql.sql as your application user. If you need a new database, create one named librarydb first, then connect to it before running the table script. Use the same user in the next step. The script contains only CREATE TABLE; it does not delete data. If BOOK already exists with this structure, skip this step.
5. Edit **src/db.properties** to match your existing database. Replace the username and password placeholders, and change the database name or port if needed:

   jdbc.driver=org.postgresql.Driver
   jdbc.url=jdbc:postgresql://localhost:5432/librarydb
   jdbc.user=postgres
   jdbc.password=YOUR_POSTGRESQL_PASSWORD

   librarydb is the database name, not a server or schema name. Replace it with your existing database name if different. Port 5432 is the PostgreSQL default. The user needs SELECT and INSERT access to BOOK. The script uses unquoted identifiers, which PostgreSQL stores as lowercase book; the supplied SQL queries use the same unquoted names. See the [official JDBC connection documentation](https://jdbc.postgresql.org/documentation/use/).
6. The **PostgreSQL JDBC driver is already included** in **WebContent/WEB-INF/lib** (postgresql-42.7.14.jar, with its license). Refresh the project (F5). No driver download is needed. Keep only one PostgreSQL driver JAR in that folder. Do not add servlet-api.jar; Tomcat supplies the Servlet API. Driver source: [official pgJDBC download](https://jdbc.postgresql.org/download/).
7. Choose **Project → Clean**, then right-click the project → **Run As → Run on Server** → select your Tomcat 9 server → Finish.
8. Open **http://localhost:8080/LibraryManagementSystem/**. Use your configured Tomcat port if it is different.

After changing db.properties or a driver JAR, clean/publish or restart the application in Eclipse so Tomcat receives the updated file.

## What to demonstrate

1. Open the registration page and enter, for example:

   | Field | Example |
   | --- | --- |
   | Book Id | 1001 |
   | Book Title | The Guide |
   | Author Name | R. K. Narayan |
   | Publisher Name | Indian Thought Publications |
   | Category | Fiction |
   | Edition | 1st |
   | Publication Year | 1958 |
   | Price | 299.50 |
   | Available Quantity | 5 |

2. Click **Register book**. The catalogue opens and displays **Book Registered Successfully!** with the new record. Refreshing this catalogue page does not repeat the INSERT. The success message appears once per registration.
3. Click **View catalogue** at any time to see all nine columns, ordered by Book ID.
4. Submit the same ID again to demonstrate the duplicate-ID message. The original record is preserved.

Book ID, title, price and quantity are required by this form. Other fields can be left blank. IDs are 1–99999, quantity 0–99999, optional year 1000–9999, and price 0–99999999.99 with at most two decimals. Text length limits follow the supplied table. Price uses the library's local currency; no currency conversion is performed.

## Project files

~~~text
LibraryManagementSystem/
├── .project / .classpath / .settings/   Eclipse project and deployment settings
├── src/
│   ├── db.properties                  Your PostgreSQL connection settings
│   └── com/library/
│       ├── DBConnection.java          Opens a JDBC connection
│       ├── RegisterServlet.java       Reads, validates and inserts form values
│       ├── DisplayServlet.java        SELECT, List<Book>, RequestDispatcher
│       └── Book.java                  Stores one retrieved book record
├── WebContent/
│   ├── index.html                     Styled nine-field registration form
│   ├── display.jsp                    Success/error state and escaped table
│   ├── assets/style.css               Shared responsive styling
│   └── WEB-INF/
│       ├── web.xml                    Servlet mappings and welcome page
│       └── lib/                       PostgreSQL JDBC driver + license included
├── database/postgresql.sql            PostgreSQL BOOK table, same nine columns
├── tests/                            Optional developer smoke test
└── docs/                             Implementation and verification notes
~~~

The Eclipse deployment assembly copies src resources, including db.properties, into WEB-INF/classes. This keeps database settings outside the public web root. Keep your local credentials private when sharing the project later.

## Request flow

- index.html submits its form using POST to RegisterServlet.
- RegisterServlet calls request.getParameter() for all nine values, validates them, opens a JDBC connection through DBConnection, and inserts using nine PreparedStatement placeholders.
- A successful insert redirects to DisplayServlet with a one-use session message.
- DisplayServlet executes SELECT ... FROM BOOK ORDER BY BOOK_ID, creates a List<Book>, and stores it as the books request attribute.
- RequestDispatcher forwards to display.jsp, which escapes stored text and renders the table. Empty results and database errors have their own messages.
- JDBC connections, statements, and result sets are closed with try-with-resources. Database exception details are logged to the Tomcat console rather than shown in the page.

There is no automatic table creation, seed data, login, borrowing, editing, or deletion. This project implements the registration/display practical.

## If your installed server is Tomcat 10.1

This supplied project targets **Tomcat 9**. Tomcat 10 changed the Servlet package from javax.servlet to jakarta.servlet; do not fix an API mismatch by copying a servlet-api.jar into WEB-INF/lib. See the [Apache migration guide](https://tomcat.apache.org/migration-10.html).

To adapt the source to an existing **Tomcat 10.1 / JDK 11+** setup:

1. In RegisterServlet.java and DisplayServlet.java, replace every javax.servlet import prefix with jakarta.servlet. Leave java.sql imports unchanged.
2. In WEB-INF/web.xml, change the web-app opening element to the following, keeping everything inside it and the closing element:

~~~xml
<web-app xmlns="https://jakarta.ee/xml/ns/jakartaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee https://jakarta.ee/xml/ns/jakartaee/web-app_6_0.xsd"
         version="6.0">
~~~

3. Under project Properties, use your installed JDK 11+ and set **Project Facets → Java** accordingly and **Dynamic Web Module → 6.0**. Under **Targeted Runtimes**, select Tomcat 10.1 and remove the old Tomcat 9 entry if it is unbound.
4. Clean, publish, and run. The bundled PostgreSQL driver supports Java 8 and newer. This alternate server configuration has not been tested here.

## Troubleshooting

| Symptom | Check |
| --- | --- |
| Servlet imports are unresolved | Select the correct Tomcat Targeted Runtime and JDK; use Eclipse's Enterprise Java/Web edition. |
| PostgreSQL driver not found in Tomcat log | Check the bundled JAR is in WebContent/WEB-INF/lib, then refresh, clean and republish. |
| Password authentication failed | Correct jdbc.user and jdbc.password in src/db.properties. |
| Connection refused | Check PostgreSQL is running and match the host/port in jdbc.url to pgAdmin. |
| Relation book does not exist | Run database/postgresql.sql in the database named in jdbc.url. Check the user search_path if you use a custom schema. |
| Relation book already exists during setup | Inspect the existing table instead of rerunning CREATE TABLE. |
| Page shows unable to register/load | Read the Eclipse Tomcat Console for the underlying JDBC error. No false success is displayed. |
| HTTP 404 | Add the project to the server, start it, and check the /LibraryManagementSystem context and port. |
| javax.servlet class error on Tomcat 10+ | Follow the compatibility steps above or use Tomcat 9. |

## Verification

The included smoke test sends real HTTP requests through embedded Tomcat and compiles/renders the JSP. It checks successful registration, the one-use message, duplicate IDs, invalid/missing inputs, numeric boundaries, optional blank fields, HTML escaping, SQL-like text, empty results, direct JSP access, and database failure pages.

The test database is **H2 in PostgreSQL compatibility mode**, supplied only to the test classpath. The application configuration and submitted SQL script target PostgreSQL. The same HTTP test was also run against a disposable PostgreSQL server using the bundled driver. See tests/README.md for reproduction steps and docs/VERIFICATION.md for the actual results and limits. Tests are outside src and are not deployed by Eclipse.
