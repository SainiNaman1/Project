package com.railway.security;

import com.railway.config.SessionContext;
import com.railway.exception.UnauthorizedException;
import com.railway.model.User;

public final class AuthorizationService {

    private AuthorizationService() {
    }

    public static User requireAuthenticated() throws UnauthorizedException {
        User user = SessionContext.getCurrentUser();
        if (user == null) {
            throw new UnauthorizedException("Authentication required to perform this action. Please log in.");
        }
        return user;
    }

    public static User requireAdmin() throws UnauthorizedException {
        User user = requireAuthenticated();
        if (!user.hasAdminPrivileges()) {
            throw new UnauthorizedException("Administrative credentials required. User '" + user.getUsername() + "' does not possess ADMIN role.");
        }
        return user;
    }

    public static void requireReservationAccess(long reservationOwnerUserId) throws UnauthorizedException {
        User user = requireAuthenticated();
        if (!user.hasAdminPrivileges() && user.getId() != reservationOwnerUserId) {
            throw new UnauthorizedException("Access denied: You do not have permission to view or manage another passenger's reservation.");
        }
    }
}
