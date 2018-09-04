package com.example.fym.coolweather.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by fym on 2018/9/3.
 */

public class Forecast {
    public String date;
    @SerializedName("tmp")
    public Temperature temperature;
    @SerializedName("cond")
    public More more;

    public class More {
     @SerializedName("txt_d")
        public String info;
    }

    public class Temperature {
        public String max;
        public String min;
    }
}
