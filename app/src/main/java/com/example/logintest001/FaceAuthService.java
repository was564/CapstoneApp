package com.example.logintest001;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface FaceAuthService {
    @Multipart
    @POST("/face/check")
    Call<FaceDataResource> AuthFace(@Part MultipartBody.Part image);

    @Multipart
    @POST("/face/register")
    Call<FaceDataResource> RegisterFace(
            @Part MultipartBody.Part image,
            @Part("name") String name,
            @Part("birth") String birth);
}
