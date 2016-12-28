package com.luceolab.parentaladvisor.restclient;

import com.luceolab.parentaladvisor.restclient.models.AccessToken;
import com.luceolab.parentaladvisor.restclient.models.MonitoringObject;
import com.luceolab.parentaladvisor.restclient.models.Settings;
import com.luceolab.parentaladvisor.restclient.models.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.*;

public interface AccountsService {

    String ACCESS_GRANT_TYPE = "password";
    String REFRESH_GRANT_TYPE = "refresh_token";

    // oAuth2 authentication
    @GET("/oauth/token")
    //@FormUrlEncoded
    Call<AccessToken> getAccessToken(
            @Field("client_id") String clientId,
            @Field("client_secret") String clientSecret,
            @Field("username") String username,
            @Field("password") String password,
            @Field("grant_type") String grantType);

    @POST("/oauth/token")
    @FormUrlEncoded
    Call<AccessToken> refreshAccessToken(
            @Field("client_id") String clientId,
            @Field("client_secret") String clientSecret,
            @Field("refresh_token") String refreshToken,
            @Field("grant_type") String grantType);

    @POST("/api/users")
    @FormUrlEncoded
    Call<MonitoringObject> signUp(
            @Field("email") String email,
            @Field("password") String password,
            @Field("phone") String phone,
            @Field("full_name") String full_name);

    // New user activation
    @POST("/api/users/activate")
    @FormUrlEncoded
    Call<MonitoringObject> activateUser(
            @Field("email") String email,
            @Field("code") String code);

    // New user activation
    @POST("/api/users/resend")
    @FormUrlEncoded
    Call<MonitoringObject> resendUserSms(
            @Field("email") String email);

    // Password recovery
    @POST("/api/users/recovery")
    @FormUrlEncoded
    Call<MonitoringObject> recoveryUser(
            @Field("email") String email);

    // Save Firebase refresh token
    @POST("/api/users/save_firebase_token")
    @FormUrlEncoded
    Call<User> saveFirebaseToken(
            @Field("firebase_token") String firebase_token,
            @Field("access_token") String access_token);

    // Get user settings
    @GET("/api/users/settings")
    Call<Settings> getSettings(
            @Query("access_token") String access_token);

    // Create new monitoring object
    @POST("/api/objects")
    @FormUrlEncoded
    Call<MonitoringObject> addObject(
            @Field("full_name") String full_name,
            @Field("phone") String phone,
            @Field("access_token") String access_token);

    // Monitoring object activation
    @POST("/api/objects/activate")
    @FormUrlEncoded
    Call<MonitoringObject> activateObject(
            @Field("phone") String phone,
            @Field("code") String code,
            @Field("access_token") String access_token);

    // Get all user's monitoring objects
    @GET("/api/objects")
    Call<List<MonitoringObject>> getMonitoringObjects(
            @Query("access_token") String access_token);

    // Get single monitoring object
    @GET("/api/objects/{object_id}")
    Call<MonitoringObject> getSingleObject(
            @Path("object_id") String object_id,
            @Query("access_token") String access_token);

    // Delete single monitoring object
    @DELETE("/api/objects/{object_id}")
    Call<MonitoringObject> deleteObject(
            @Path("object_id") String object_id,
            @Query("access_token") String access_token);

    // Update single monitoring object
    @PUT("/api/objects/{object_id}")
    @FormUrlEncoded
    Call<MonitoringObject> editObject(
            @Path("object_id") String object_id,
            @Field("access_token") String access_token,
            @Field("full_name") String full_name);

    // Save user settings
    @POST("/api/users/settings")
    @FormUrlEncoded
    Call<Settings> saveSettings(
            @Field("access_token") String access_token,
            @Field("monitoring_status") String monitoring_status,
            @Field("notifications") String notifications,
            @Field("full_name") String full_name,
            @Field("phone") String phone,
            @Field("password") String password);

}
