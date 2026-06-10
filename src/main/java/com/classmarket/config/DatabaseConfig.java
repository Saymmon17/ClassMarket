package com.classmarket.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Configuration
public class DatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    private HikariDataSource dataSource;

    @Bean
    @Primary
    public DataSource dataSource(Environment env) {
        String jdbcUrl = env.getProperty("JDBC_DATABASE_URL");
        String username = env.getProperty("JDBC_DATABASE_USERNAME", "root");
        String password = env.getProperty("JDBC_DATABASE_PASSWORD", "012356");
        String driver = env.getProperty("JDBC_DATABASE_DRIVER", "com.mysql.cj.jdbc.Driver");

        if (jdbcUrl != null && !jdbcUrl.isBlank()) {
            logger.info("Attempting to connect to external JDBC database: {}", jdbcUrl);
            HikariDataSource mysqlDataSource = buildDataSource(jdbcUrl, username, password, driver);
            if (testConnection(mysqlDataSource)) {
                logger.info("Connected successfully to external JDBC database.");
                this.dataSource = mysqlDataSource;
                return mysqlDataSource;
            }
            logger.warn("Cannot connect to external JDBC database. Falling back to embedded H2.");
            mysqlDataSource.close();
        }

        HikariDataSource h2DataSource = buildDataSource(
                "jdbc:h2:mem:classmarket;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=MYSQL",
                "sa",
                "",
                "org.h2.Driver"
        );
        logger.info("Using embedded H2 database fallback.");
        this.dataSource = h2DataSource;
        return h2DataSource;
    }

    private HikariDataSource buildDataSource(String url, String username, String password, String driver) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driver);
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setAutoCommit(true);
        return new HikariDataSource(config);
    }

    private boolean testConnection(HikariDataSource ds) {
        try (Connection ignored = ds.getConnection()) {
            return true;
        } catch (SQLException e) {
            logger.error("Database connection test failed: {}", e.getMessage());
            return false;
        }
    }

    @PreDestroy
    public void shutdown() {
        if (this.dataSource != null) {
            this.dataSource.close();
        }
    }
}
