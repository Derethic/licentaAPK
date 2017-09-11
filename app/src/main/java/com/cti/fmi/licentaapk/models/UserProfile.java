package com.cti.fmi.licentaapk.models;

import com.google.gson.annotations.SerializedName;

public class UserProfile
{
    @SerializedName("id")
    private int idUser;

    @SerializedName("email")
    private String email;

    @SerializedName("first_name")
    private String firstName;

    @SerializedName("last_name")
    private String lastName;

    @SerializedName("location")
    private String location;

    @SerializedName("country_name")
    private String countryName;

    @SerializedName("phone_number")
    private String phoneNumber;

    @SerializedName("last_login")
    private String lastLogin;

    @SerializedName("date_joined")
    private String dateJoined;

    public UserProfile(int idUser, String email, String firstName, String lastName,
                       String location, String countryName, String phoneNumber, String lastLogin, String dateJoined)
    {
        this.idUser = idUser;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.location = location;
        this.countryName = countryName;
        this.phoneNumber = phoneNumber;
        this.lastLogin = lastLogin;
        this.dateJoined = dateJoined;
    }

    public int getIdUser()
    {
        return idUser;
    }

    public void setIdUser(int idUser)
    {
        this.idUser = idUser;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getFirstName()
    {
        return firstName;
    }

    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    public String getLastName()
    {
        return lastName;
    }

    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }

    public String getLocation()
    {
        return location;
    }

    public void setLocation(String location)
    {
        this.location = location;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getPhoneNumber()
    {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber)
    {
        this.phoneNumber = phoneNumber;
    }

    public String getLastLogin()
    {
        return lastLogin;
    }

    public void setLastLogin(String lastLogin)
    {
        this.lastLogin = lastLogin;
    }

    public String getDateJoined()
    {
        return dateJoined;
    }

    public void setDateJoined(String dateJoined)
    {
        this.dateJoined = dateJoined;
    }
}
