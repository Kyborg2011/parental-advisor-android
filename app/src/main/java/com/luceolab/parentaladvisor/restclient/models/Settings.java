package com.luceolab.parentaladvisor.restclient.models;

import com.luceolab.parentaladvisor.restclient.BaseResponse;

public class Settings extends BaseResponse {
    public User user;
    public boolean monitoring_status;
    public int notifications;
}
