package com.birthcertify.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;

public class DatabaseConnection {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);  // Logger initialization
    private static final HikariDataSource dataSource;
    private static final Dotenv dotenv = Dotenv.load();

    static {
        logger.info("Initializing database connection pool...");

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dotenv.get("DB_URL"));
        config.setUsername(dotenv.get("DB_USER"));
        config.setPassword(dotenv.get("DB_PASSWORD"));

        // Connection Pool Settings
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(30000);
        config.setConnectionTimeout(20000);
        config.setMaxLifetime(1800000);

        // Statement Caching
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");

        // Optional
        config.setConnectionInitSql("DEALLOCATE ALL");
        config.setAutoCommit(true);
        config.setLeakDetectionThreshold(60000);

        dataSource = new HikariDataSource(config);

        logger.info("Database connection pool initialized successfully.");
    }

    public static Connection getConnection() {
        try {
            // Attempt to get a connection from the pool
            return dataSource.getConnection();
        } catch (Exception e) {
            logger.error("Unexpected error occurred while getting database connection.", e);
            return null;  // Return null if an error occurs
        }
    }

    public static void closePool() {
        logger.info("Closing database connection pool...");
        if (dataSource != null && !dataSource.isClosed()) {
            try {
                dataSource.close();
                logger.info("Database connection pool closed successfully.");
            } catch (Exception e) {
                logger.error("Error closing database connection pool.", e);
            }
        } else {
            logger.warn("Database connection pool is already closed or not initialized.");
        }
    }
}
