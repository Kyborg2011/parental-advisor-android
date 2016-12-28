package com.luceolab.parentaladvisor.restclient.models;

import com.luceolab.parentaladvisor.restclient.BaseResponse;

public class State extends BaseResponse {
    public Double battery_level;
    public Double lng;
    public Double lat;
    public String timestamp;
    public String object;

    public String object_name;
    public String object_phone;

    @Override
    public String toString() {
        return "battery_level: " + battery_level +
                "lng: " + lng +
                "lat: " + lat +
                "timestamp: " + timestamp +
                "object: " + object + "object_name: " + object_name +
                "object_phone: " + object_phone;
     }
}