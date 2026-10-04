package com.idlewheels.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("OWNER")
public class Owner extends User {

    protected Owner() {
        // Required by JPA.
    }

    public Owner(String name, String email, String passwordHash, String phone, String city) {
        super(name, email, passwordHash, phone, city);
    }

    @Override
    public UserRole getRole() {
        return UserRole.OWNER;
    }
}
