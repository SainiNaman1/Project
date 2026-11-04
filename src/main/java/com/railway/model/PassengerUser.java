package com.railway.model;

import java.time.LocalDateTime;

import com.railway.model.enums.Role;

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
