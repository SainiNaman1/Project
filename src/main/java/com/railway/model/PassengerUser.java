package com.railway.model;

import com.railway.model.enums.Role;
import java.time.LocalDateTime;

/**
 * Represents a standard passenger user of the self-service railway booking portal.
 * Demonstrates INHERITANCE and POLYMORPHISM.
 */
public class PassengerUser extends User {
    private String phoneNumber;

    public PassengerUser() {
        this.role = Role.PASSENGER;
    }

    public PassengerUser(long id, String username, String passwordHash, String fullName, String email, String phoneNumber, LocalDateTime createdAt) {
        super(id, username, passwordHash, fullName, email, Role.PASSENGER, createdAt);
        this.phoneNumber = phoneNumber;
    }

    @Override
    public boolean hasAdminPrivileges() {
        return false;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
