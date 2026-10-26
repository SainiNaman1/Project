package com.railway.config;

import com.railway.model.User;

/**
 * Global session registry holding the currently authenticated user.
 * Thread-safe for desktop applications.
 */
public final class SessionContext {

    private static User currentUser;

    private SessionContext() {
    }

    public static synchronized void setCurrentUser(User user) {
        currentUser = user;
    }

    public static synchronized User getCurrentUser() {
        return currentUser;
    }

    public static synchronized boolean isAuthenticated() {
        return currentUser != null;
    }

    public static synchronized boolean isAdmin() {
        return currentUser != null && currentUser.hasAdminPrivileges();
    }

    public static synchronized void logout() {
        currentUser = null;
    }
}
