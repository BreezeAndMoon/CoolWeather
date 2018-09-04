package com.example.fym.coolweather.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by fym on 2018/8/27.
 */

public class Basic {
    @SerializedName("city")
    public String cityName;
    @SerializedName("id")
    public String weatherId;
    public Update update;

    public class Update {
        @SerializedName("loc")
        public String updateTime;
    }
}
