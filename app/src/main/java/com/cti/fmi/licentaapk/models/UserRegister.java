package com.cti.fmi.licentaapk.models;

import com.google.gson.annotations.SerializedName;

public class UserRegister
{
    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;

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

    public UserRegister(String email, String password, String firstName, String lastName,
                        String location,String countryName, String phoneNumber)
    {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.location = location;
        this.countryName = countryName;
        this.phoneNumber = phoneNumber;
    }
}
