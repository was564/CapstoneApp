package com.example.logintest001;

import com.google.gson.annotations.SerializedName;

public class FaceDataResource {

    @SerializedName("status")
    public int statusResult;
    @SerializedName("file_name")
    public String fileName;
    @SerializedName("name")
    public String name;
    @SerializedName("birth")
    public String birth;
}
