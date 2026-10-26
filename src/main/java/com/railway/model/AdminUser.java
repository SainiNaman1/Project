package com.railway.model;

import com.railway.model.enums.Role;
import java.time.LocalDateTime;

/**
 * Represents an authorized railway operations administrator.
 * Demonstrates INHERITANCE and POLYMORPHISM.
 */
public class AdminUser extends User {
    private String department;

    public AdminUser() {
        this.role = Role.ADMIN;
    }

    public AdminUser(long id, String username, String passwordHash, String fullName, String email, String department, LocalDateTime createdAt) {
        super(id, username, passwordHash, fullName, email, Role.ADMIN, createdAt);
        this.department = department;
    }

    @Override
    public boolean hasAdminPrivileges() {
        return true;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}
