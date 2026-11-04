package com.railway.database;

import com.railway.exception.RailwayException;
import java.sql.Connection;
import java.sql.SQLException;

public final class TransactionManager {

    @FunctionalInterface
    public interface TransactionAction<T> {
        T execute(Connection conn) throws Exception;
    }

    @FunctionalInterface
    public interface TransactionActionVoid {
        void execute(Connection conn) throws Exception;
    }

    private static final DatabaseConnection db = DatabaseConnection.getInstance();

    private TransactionManager() {
    }

    public static <T> T execute(TransactionAction<T> action) throws RailwayException {
        Connection conn = null;
        try {
            conn = db.getConnection();
            conn.setAutoCommit(false);

            T result = action.execute(conn);

            conn.commit();
            return result;
        } catch (Exception e) {
            DatabaseConnection.rollbackQuietly(conn);
            if (e instanceof RailwayException re) {
                throw re;
            }
            throw new RailwayException("Transaction rolled back due to error: " + e.getMessage(), e);
        } finally {
            DatabaseConnection.closeQuietly(conn);
        }
    }

    public static void executeVoid(TransactionActionVoid action) throws RailwayException {
        execute(conn -> {
            action.execute(conn);
            return null;
        });
    }
}
