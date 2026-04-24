package com.oracle.boutique.config;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.Statement;

/**
 * Wraps the DataSource to log all SQL statements to the console.
 */
@Component
public class SqlLogger {

    public SqlLogger(JdbcTemplate jdbcTemplate) {
        DataSource original = jdbcTemplate.getDataSource();
        if (original != null) {
            DataSource loggingDs = (DataSource) Proxy.newProxyInstance(
                    original.getClass().getClassLoader(),
                    new Class[]{DataSource.class},
                    new DataSourceHandler(original));
            jdbcTemplate.setDataSource(loggingDs);
        }
    }

    private static class DataSourceHandler implements InvocationHandler {
        private final DataSource target;

        DataSourceHandler(DataSource target) {
            this.target = target;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Object result = method.invoke(target, args);
            if (result instanceof Connection conn) {
                return Proxy.newProxyInstance(
                        conn.getClass().getClassLoader(),
                        new Class[]{Connection.class},
                        new ConnectionHandler(conn));
            }
            return result;
        }
    }

    private static class ConnectionHandler implements InvocationHandler {
        private final Connection target;

        ConnectionHandler(Connection target) {
            this.target = target;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if ("prepareStatement".equals(method.getName()) && args != null && args.length > 0) {
                String sql = (String) args[0];
                logSql(sql);
            }
            if ("createStatement".equals(method.getName())) {
                Object result = method.invoke(target, args);
                if (result instanceof Statement stmt) {
                    return Proxy.newProxyInstance(
                            stmt.getClass().getClassLoader(),
                            new Class[]{Statement.class},
                            new StatementHandler(stmt));
                }
                return result;
            }
            return method.invoke(target, args);
        }
    }

    private static class StatementHandler implements InvocationHandler {
        private final Statement target;

        StatementHandler(Statement target) {
            this.target = target;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (("execute".equals(method.getName()) || "executeQuery".equals(method.getName())
                    || "executeUpdate".equals(method.getName()))
                    && args != null && args.length > 0 && args[0] instanceof String sql) {
                logSql(sql);
            }
            return method.invoke(target, args);
        }
    }

    private static void logSql(String sql) {
        String trimmed = sql.strip().replaceAll("\\s+", " ");
        if (trimmed.isEmpty()) return;
        // Skip internal Spring/Hikari health checks
        if (trimmed.startsWith("SELECT 1") || trimmed.contains("pg_catalog")) return;
        System.out.println("[SQL] " + trimmed);
    }
}
