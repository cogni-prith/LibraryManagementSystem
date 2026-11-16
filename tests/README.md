# Optional HTTP/JDBC/JSP smoke test

This is a developer check, not a prerequisite for running the project in Windows Eclipse. It uses the actual servlets and JSP on embedded Tomcat, with H2 in PostgreSQL compatibility mode. It never connects to the application PostgreSQL database. The DROP TABLE in the test operates only on its own in-memory H2 table.

## Dependencies

Use JDK 11 or newer to run this test (verified with JDK 21). Compile the application at Java 8 compatibility with --release 8. Download these test-only JARs into test-work/lib, at the project root:

- [tomcat-embed-core-9.0.124.jar](https://repo.maven.apache.org/maven2/org/apache/tomcat/embed/tomcat-embed-core/9.0.124/tomcat-embed-core-9.0.124.jar)
- [tomcat-embed-jasper-9.0.124.jar](https://repo.maven.apache.org/maven2/org/apache/tomcat/embed/tomcat-embed-jasper/9.0.124/tomcat-embed-jasper-9.0.124.jar)
- [tomcat-embed-el-9.0.124.jar](https://repo.maven.apache.org/maven2/org/apache/tomcat/embed/tomcat-embed-el/9.0.124/tomcat-embed-el-9.0.124.jar)
- [ecj-3.33.0.jar](https://repo.maven.apache.org/maven2/org/eclipse/jdt/ecj/3.33.0/ecj-3.33.0.jar)
- [javax.annotation-api-1.3.2.jar](https://repo.maven.apache.org/maven2/javax/annotation/javax.annotation-api/1.3.2/javax.annotation-api-1.3.2.jar)
- [h2-2.2.224.jar](https://repo.maven.apache.org/maven2/com/h2database/h2/2.2.224/h2-2.2.224.jar)

Keep these out of WebContent/WEB-INF/lib. They are not application dependencies.

## Windows PowerShell

From the project directory, after downloading those JARs:

~~~powershell
New-Item -ItemType Directory -Force test-work/classes | Out-Null
$sources = (Get-ChildItem src/com/library/*.java).FullName
javac --release 8 -Xlint:-options -cp "test-work/lib/*" -d test-work/classes $sources tests/SmokeTest.java
@'
jdbc.driver=org.h2.Driver
jdbc.url=jdbc:h2:mem:library;MODE=PostgreSQL;DB_CLOSE_DELAY=-1
jdbc.user=sa
jdbc.password=
'@ | Set-Content -Encoding ascii test-work/classes/db.properties
java -cp "test-work/classes;test-work/lib/*" SmokeTest . test-work/classes
~~~

## Linux/macOS shell

~~~sh
mkdir -p test-work/classes
javac --release 8 -Xlint:-options -cp 'test-work/lib/*' -d test-work/classes src/com/library/*.java tests/SmokeTest.java
cat > test-work/classes/db.properties <<'PROPERTIES'
jdbc.driver=org.h2.Driver
jdbc.url=jdbc:h2:mem:library;MODE=PostgreSQL;DB_CLOSE_DELAY=-1
jdbc.user=sa
jdbc.password=
PROPERTIES
java -cp 'test-work/classes:test-work/lib/*' SmokeTest . test-work/classes
~~~

Expected final test line: **PASS: 33 HTTP/JDBC/JSP checks**. Tomcat startup/shutdown logs, including deliberate database errors from the outage checks, are expected. An AssertionError or nonzero process exit indicates a failure. The test creates temporary Tomcat directories in the OS temporary folder; they can be removed after the process exits.

This verifies request processing, binding, validation, rendering and error handling. It does not certify the user's PostgreSQL installation or credentials, Windows Eclipse integration, or the alternate Tomcat 10.1 configuration.
