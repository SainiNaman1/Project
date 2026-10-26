package com.railway.service;

import com.railway.exception.AuthenticationException;
import com.railway.exception.ValidationException;
import com.railway.model.PassengerUser;
import com.railway.model.User;

/**
 * Service interface governing user authentication, registration, and session management.
 */
public interface AuthService {
    User login(String username, String password) throws AuthenticationException;
    PassengerUser register(String fullName, String email, String phone, String username, String password, String confirmPassword) throws ValidationException;
    void logout();
}
