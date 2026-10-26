package com.railway.model;

import com.railway.model.enums.Role;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Abstract base class for all authenticated identities in the system.
 * Demonstrates ABSTRACTION and ENCAPSULATION.
 */
public abstract class User {
    protected long id;
    protected String username;
    protected String passwordHash;
    protected String fullName;
    protected String email;
    protected Role role;
    protected LocalDateTime createdAt;

    public User() {
    }

    public User(long id, String username, String passwordHash, String fullName, String email, Role role, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt;
    }

    /**
     * Polymorphic authorization method.
     * Subclasses define whether this user holds administrative rights.
     */
    public abstract boolean hasAdminPrivileges();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return id == user.id && Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username);
    }

    @Override
    public String toString() {
        return fullName + " (" + username + " [" + role + "])";
    }
}
