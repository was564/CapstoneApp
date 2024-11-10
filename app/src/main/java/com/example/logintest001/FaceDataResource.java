package com.example.logintest001;

import com.google.gson.annotations.SerializedName;

public class FaceDataResource {

    @SerializedName("status")
    public int statusResult;

    @SerializedName("name")
    public String name;
    @SerializedName("birth")
    public String birth;
    @SerializedName("try")
    public String tryCount;
}
