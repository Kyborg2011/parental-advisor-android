package com.luceolab.parentaladvisor.restclient.models;

import com.luceolab.parentaladvisor.restclient.BaseResponse;

public class User extends BaseResponse {
    public String full_name;
    public String phone;
    public String email;
    public int active;
    public String firebase_token;
    public String created;
}
