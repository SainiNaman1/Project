package com.railway.service.impl;

import com.railway.config.SessionContext;
import com.railway.dao.UserDAO;
import com.railway.dao.impl.UserDAOImpl;
import com.railway.exception.AuthenticationException;
import com.railway.exception.ValidationException;
import com.railway.model.PassengerUser;
import com.railway.model.User;
import com.railway.model.enums.Role;
import com.railway.service.AuthService;
import com.railway.util.PasswordUtil;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Pattern;

public class AuthServiceImpl implements AuthService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[6-9]\\d{9}$");

    private final UserDAO userDAO;

    public AuthServiceImpl() {
        this.userDAO = new UserDAOImpl();
    }

    public AuthServiceImpl(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public User login(String username, String password) throws AuthenticationException {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new AuthenticationException("Please enter both username and password.");
        }

        try {
            Optional<User> userOpt = userDAO.findByUsername(username.trim());
            if (userOpt.isEmpty()) {
                throw new AuthenticationException("Invalid username or password.");
            }

            User user = userOpt.get();
            if (!PasswordUtil.checkPassword(password, user.getPasswordHash())) {
                throw new AuthenticationException("Invalid username or password.");
            }

            SessionContext.setCurrentUser(user);
            return user;
        } catch (SQLException e) {
            throw new AuthenticationException("Database error during login: " + e.getMessage());
        }
    }

    @Override
    public PassengerUser register(String fullName, String email, String phone, String username, String password, String confirmPassword) throws ValidationException {
        // Validate presence
        if (fullName == null || fullName.trim().length() < 2) {
            throw new ValidationException("Full name must contain at least 2 characters.");
        }
        if (email == null || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new ValidationException("Please provide a valid email address (e.g., user@example.com).");
        }
        if (phone == null || !PHONE_PATTERN.matcher(phone.trim()).matches()) {
            throw new ValidationException("Please enter a valid 10-digit Indian mobile number starting with 6, 7, 8, or 9.");
        }
        if (username == null || username.trim().length() < 3) {
            throw new ValidationException("Username must be at least 3 characters long.");
        }
        if (password == null || password.length() < 6) {
            throw new ValidationException("Password must be at least 6 characters long.");
        }
        if (!password.equals(confirmPassword)) {
            throw new ValidationException("Passwords do not match. Please re-enter.");
        }

        try {
            if (userDAO.existsByUsername(username.trim())) {
                throw new ValidationException("Username '" + username + "' is already taken. Please choose another.");
            }
            if (userDAO.existsByEmail(email.trim())) {
                throw new ValidationException("An account with email '" + email + "' already exists.");
            }

            String hashed = PasswordUtil.hashPassword(password);
            PassengerUser newUser = new PassengerUser(
                    0L,
                    username.trim(),
                    hashed,
                    fullName.trim(),
                    email.trim().toLowerCase(),
                    phone.trim(),
                    LocalDateTime.now()
            );

            // Crucial security requirement: Role is hardcoded to PASSENGER
            newUser.setRole(Role.PASSENGER);

            return (PassengerUser) userDAO.save(newUser);
        } catch (SQLException e) {
            throw new ValidationException("Failed to register account: " + e.getMessage());
        }
    }

    @Override
    public void logout() {
        SessionContext.logout();
    }
}
