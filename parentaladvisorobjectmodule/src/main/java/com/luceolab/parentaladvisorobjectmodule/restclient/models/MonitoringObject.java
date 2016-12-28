package com.luceolab.parentaladvisorobjectmodule.restclient.models;

import com.luceolab.parentaladvisorobjectmodule.restclient.BaseResponse;

import java.util.List;

public class MonitoringObject extends BaseResponse {
    public String _id;
    public String full_name;
    public String phone;
    public String imei;
    public String operation_system;
    public String firebase_token;
    public int active;
    public String activation_code;
    public List<State> states;
}