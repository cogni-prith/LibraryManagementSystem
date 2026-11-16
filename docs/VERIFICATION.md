# Verification record

- Application Java sources and the smoke test compiled successfully with javac 21 using --release 8 and -Xlint:all -Xlint:-options; no compiler warnings or errors.
- **33 HTTP/JDBC/JSP checks passed against PostgreSQL 17.12**, running in a temporary local postgres:17-alpine container, with the bundled pgJDBC 42.7.14 driver and embedded Tomcat 9.0.124.
- The same **33 checks passed with H2 2.2.224 in PostgreSQL compatibility mode**. The included tests/SmokeTest.java uses this in-memory test configuration by default.
- For the real PostgreSQL run, an isolated copy of the test changed only the JDBC URL, driver, test username, and driver-specific exception-leak checks. Application Java sources, SQL schema, servlet mappings and JSP were the delivered files. All data and deliberate DROP TABLE operations were confined to the disposable test database.
- The PostgreSQL-mode test first failed with the old NUMBER-based schema, then passed after the schema conversion.
- Checks include form availability, empty catalogue, POST redirect, exact success message, one-use message, duplicate ID, missing/invalid/overflowing values, scale and precision boundaries, optional blanks, zero stock/price, persisted decimal values, HTML escaping, SQL-like text stored as data, direct JSP access, and safe database-failure messages.
- Desktop registration and catalogue, plus the 390-pixel registration layout, were previously rendered in headless Chrome and visually inspected. No HTML, JSP or CSS changed in the PostgreSQL update.
- A separate read-only code review of the initial application found no concrete implementation defects.
- database/postgresql.sql defines the same nine BOOK columns with PostgreSQL types and numeric constraints. No existing user database was accessed or modified.
- The PostgreSQL JDBC driver is bundled with its license. The rebuilt ZIP is checked for integrity and byte-for-byte agreement with the project folder.

## Limits

The user's PostgreSQL installation, credentials, Windows and Eclipse were not available for verification. Run the README setup steps in that environment. The Tomcat 10.1 adaptation is documented but was not executed.

An Aikido security scan was attempted again after the PostgreSQL update, but its server could not read authentication from the OS keychain in this environment. No automated security-scan result is claimed.
