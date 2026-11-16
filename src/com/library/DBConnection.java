package com.library;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DBConnection {
    private DBConnection() { }

    public static Connection getConnection() throws SQLException {
        Properties settings = new Properties();
        try (InputStream in = DBConnection.class.getResourceAsStream("/db.properties")) {
            if (in == null) {
                throw new SQLException("db.properties is missing from WEB-INF/classes.");
            }
            settings.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new SQLException("Unable to read database configuration.", e);
        }
        String driver = settings.getProperty("jdbc.driver");
        String url = settings.getProperty("jdbc.url");
        String user = settings.getProperty("jdbc.user");
        String password = settings.getProperty("jdbc.password");
        if (driver == null || url == null || user == null || password == null) {
            throw new SQLException("Set jdbc.driver, jdbc.url, jdbc.user and jdbc.password in db.properties.");
        }
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new SQLException("JDBC driver not found. Add the PostgreSQL JDBC JAR to WEB-INF/lib.", e);
        }
        return DriverManager.getConnection(url, user, password);
    }
}
