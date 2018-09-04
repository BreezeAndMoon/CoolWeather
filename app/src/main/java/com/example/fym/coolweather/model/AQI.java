package com.example.fym.coolweather.model;

/**
 * Created by fym on 2018/8/27.
 */

public class AQI {
    public AQICity city;

    public class AQICity {
        public String aqi;
        public String pm25;
    }
}
