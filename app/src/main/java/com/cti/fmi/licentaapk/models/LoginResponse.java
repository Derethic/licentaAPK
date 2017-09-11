package com.cti.fmi.licentaapk.models;


import com.google.gson.annotations.SerializedName;

public class LoginResponse
{
    @SerializedName("id")
    private int id;

    @SerializedName("token")
    private String token;

    public int getId()
    {
        return id;
    }

    public String getToken()
    {
        return token;
    }
}
