package com.example.fym.coolweather.db;

import org.litepal.crud.DataSupport;

/**
 * Created by fym on 2018/8/20.
 */

public class City extends DataSupport {
    private int id;
    private String cityName;
    private int provinceId;
    private int cityCode;
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public int getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(int provinceId) {
        this.provinceId = provinceId;
    }

    public int getCityCode() {
        return cityCode;
    }

    public void setCityCode(int cityCode) {
        this.cityCode = cityCode;
    }
}
