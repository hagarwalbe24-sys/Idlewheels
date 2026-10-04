package com.idlewheels.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ProfileForm {
    @NotBlank @Size(max = 100)
    private String name;
    @Size(max = 15)
    @Pattern(regexp = "^$|^(?=(?:\\D*\\d){7,15}\\D*$)[0-9+ -]{1,15}$",
            message = "Use digits, spaces, +, or - and enter 7–15 digits.")
    private String phone;
    @NotBlank @Size(max = 80)
    private String city;
    private String currentPassword;
    @Pattern(regexp = "^$|^(?=.*[A-Za-z])(?=.*\\d)[!-~]{8,72}$",
            message = "Use 8–72 printable characters with at least one letter and one number.")
    private String newPassword;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getCurrentPassword() { return currentPassword; }
    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
