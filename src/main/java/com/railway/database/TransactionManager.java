package com.railway.database;

import com.railway.exception.RailwayException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Standardizes transaction boundaries across multi-table operations.
 * Handles auto-commit management, commit, rollback on failure, and clean closure.
 */
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

    /**
     * Executes the given action within an atomic database transaction.
     *
     * @param action Functional block receiving the transactional Connection
     * @param <T> Return type
     * @return Result of the transaction action
     * @throws RailwayException if the transaction fails and rolls back
     */
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

    /**
     * Executes a void action within an atomic database transaction.
     */
    public static void executeVoid(TransactionActionVoid action) throws RailwayException {
        execute(conn -> {
            action.execute(conn);
            return null;
        });
    }
}
