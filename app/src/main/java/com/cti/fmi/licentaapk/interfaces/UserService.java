package com.cti.fmi.licentaapk.interfaces;

import com.cti.fmi.licentaapk.models.Advert;
import com.cti.fmi.licentaapk.models.CategoryDisplay;
import com.cti.fmi.licentaapk.models.LoginResponse;
import com.cti.fmi.licentaapk.models.UserLogin;
import com.cti.fmi.licentaapk.models.UserProfile;
import com.cti.fmi.licentaapk.models.UserRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface UserService
{
    @POST("login/")
    Call<LoginResponse> loginRequest(@Body UserLogin userLogin);

    @POST("register/")
    Call<LoginResponse> registerRequest(@Body UserRegister userRegister);

    @GET("users/{id_user}/")
    Call<UserProfile> getUserDetails(@Header("Authorization") String authToken,
                                     @Path("id_user") int idUser);

    @GET("categories/")
    Call<ArrayList<CategoryDisplay>> getCategories(@Header("Authorization") String authToken);

    @Multipart
    @POST("ads/")
    Call<ResponseBody> createNewAdvert(
            @Header("Authorization") String authToken,
            @PartMap() Map<String, RequestBody> partMap,
            @Part List<MultipartBody.Part> pictures
    );

    @GET("ads")
    Call<ArrayList<Advert>> getAllAdverts(
            @Header("Authorization") String authToken,
            @Query("page") int pageNumber);

    @GET("user/ads")
    Call<ArrayList<Advert>> getUserAdverts(
            @Header("Authorization") String authToken,
            @Query("page") int pageNumber);
}

