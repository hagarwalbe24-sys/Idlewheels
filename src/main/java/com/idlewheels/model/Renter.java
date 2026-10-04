package com.idlewheels.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("RENTER")
public class Renter extends User {

    protected Renter() {
        // Required by JPA.
    }

    public Renter(String name, String email, String passwordHash, String phone, String city) {
        super(name, email, passwordHash, phone, city);
    }

    @Override
    public UserRole getRole() {
        return UserRole.RENTER;
    }
}