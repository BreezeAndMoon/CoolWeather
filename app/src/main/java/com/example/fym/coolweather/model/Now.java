package com.example.fym.coolweather.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by fym on 2018/8/27.
 */

public class Now {
    @SerializedName("tmp")
    public String temperature;
    @SerializedName("cond")
    public More more;

    public class More {
        @SerializedName("txt")
        public String info;
    }
}
