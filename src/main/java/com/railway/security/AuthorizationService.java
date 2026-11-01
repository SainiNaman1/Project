package com.railway.security;

import com.railway.config.SessionContext;
import com.railway.exception.UnauthorizedException;
import com.railway.model.User;

/**
 * Centralized Role-Based Access Control (RBAC) security validator.
 * Enforces permission checks inside service-layer business logic.
 */
public final class AuthorizationService {

    private AuthorizationService() {
    }

    /**
     * Enforces that an active, authenticated user session exists.
     */
    public static User requireAuthenticated() throws UnauthorizedException {
        User user = SessionContext.getCurrentUser();
        if (user == null) {
            throw new UnauthorizedException("Authentication required to perform this action. Please log in.");
        }
        return user;
    }

    /**
     * Enforces that the current session holds administrator privileges.
     */
    public static User requireAdmin() throws UnauthorizedException {
        User user = requireAuthenticated();
        if (!user.hasAdminPrivileges()) {
            throw new UnauthorizedException("Administrative credentials required. User '" + user.getUsername() + "' does not possess ADMIN role.");
        }
        return user;
    }

    /**
     * Enforces that the current user owns the target reservation or possesses administrative privileges.
     */
    public static void requireReservationAccess(long reservationOwnerUserId) throws UnauthorizedException {
        User user = requireAuthenticated();
        if (!user.hasAdminPrivileges() && user.getId() != reservationOwnerUserId) {
            throw new UnauthorizedException("Access denied: You do not have permission to view or manage another passenger's reservation.");
        }
    }
}
