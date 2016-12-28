package com.luceolab.parentaladvisorobjectmodule.restclient;

import com.luceolab.parentaladvisorobjectmodule.restclient.models.MonitoringObject;
import com.luceolab.parentaladvisorobjectmodule.restclient.models.State;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface AccountsService {

    @POST("/api/states")
    @FormUrlEncoded
    Call<State> sendCurrentState(
            @Field("phone") String phone,
            @Field("lng") double lng,
            @Field("lat") double lat,
            @Field("battery_level") float battery_level);

    @POST("/api/objects/register_device")
    @FormUrlEncoded
    Call<MonitoringObject> startRegisteringDevice(
            @Field("phone") String phone);

    @POST("/api/objects/register_device")
    @FormUrlEncoded
    Call<MonitoringObject> endRegisteringDevice(
            @Field("phone") String phone,
            @Field("code") String code,
            @Field("os") String os,
            @Field("imei") String imei,
            @Field("firebase_token") String firebase_token);
}
