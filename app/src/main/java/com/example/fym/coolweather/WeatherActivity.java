package com.example.fym.coolweather;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.Image;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.fym.coolweather.model.Forecast;
import com.example.fym.coolweather.model.Weather;
import com.example.fym.coolweather.util.HttpUtil;
import com.example.fym.coolweather.util.Utility;

import org.litepal.util.Const;

import java.io.IOException;
import java.time.ZoneId;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    private ScrollView mScrollView;
    private TextView mTitleTextView;
    private TextView mUpdateTimeTextView;
    private TextView mDegreeTextView;
    private TextView mWeatherInfoTextView;
    private LinearLayout mForecastLinearLayout;
    private TextView mAqiTextView;
    private TextView mPm25TextView;
    private TextView mComfortTextView;
    private TextView mCarWashTextView;
    private TextView mSportTextView;
    private ImageView mImageView;
    public SwipeRefreshLayout mSwipeRefresh;
    private String mWeatherId;
    public DrawerLayout mDrawerLayout;
    private Button mNavButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        initViews();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = sharedPreferences.getString("weather", null);
        if (weatherString != null) {
            Weather weather = Utility.handleWeatherResponse(weatherString);
            mWeatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        } else {
            mWeatherId = getIntent().getStringExtra(Constant.WEATHERID);
            mScrollView.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId);
        }
        //初始化背景图片
        String bingPicUrl = sharedPreferences.getString(Constant.BING_PIC, null);
        if (bingPicUrl != null) {
            Glide.with(this).load(bingPicUrl).into(mImageView);
        } else {
            loadBingPic();
        }

    }

    private void loadBingPic() {
        String requestBindPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBindPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString(Constant.BING_PIC, bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(mImageView);
                    }
                });
            }
        });
    }


    private void initViews() {
        mScrollView = findViewById(R.id.scrollView_weather);
        mTitleTextView = findViewById(R.id.tv_title_city);
        mUpdateTimeTextView = findViewById(R.id.tv_update_time);
        mDegreeTextView = findViewById(R.id.tv_degree);
        mWeatherInfoTextView = findViewById(R.id.tv_weather_info);
        mForecastLinearLayout = findViewById(R.id.layout_forecast);
        mAqiTextView = findViewById(R.id.tv_aqi);
        mPm25TextView = findViewById(R.id.tv_pm25);
        mComfortTextView = findViewById(R.id.tv_comfort);
        mCarWashTextView = findViewById(R.id.tv_car_wash);
        mSportTextView = findViewById(R.id.tv_sport);
        mImageView = findViewById(R.id.image_view_bing);
        mSwipeRefresh = findViewById(R.id.swipe_refresh);
        mSwipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });
        mDrawerLayout = findViewById(R.id.drawer_layout);

        mNavButton = findViewById(R.id.btn_nav);
        mNavButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;
        mTitleTextView.setText(cityName);
        mUpdateTimeTextView.setText(updateTime);
        mDegreeTextView.setText(degree);
        mWeatherInfoTextView.setText(weatherInfo);
        mForecastLinearLayout.removeAllViews();
        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.item_forecast, mForecastLinearLayout, false);
            TextView dateTextView = view.findViewById(R.id.tv_date);
            TextView infoTextView = view.findViewById(R.id.tv_info);
            TextView maxTextView = view.findViewById(R.id.tv_max);
            TextView minTextView = view.findViewById(R.id.tv_min);
            dateTextView.setText(forecast.date);
            infoTextView.setText(forecast.more.info);
            maxTextView.setText(forecast.temperature.max);
            minTextView.setText(forecast.temperature.min);
            mForecastLinearLayout.addView(view);
        }
        if (weather.aqi != null) {
            mAqiTextView.setText(weather.aqi.city.aqi);
            mPm25TextView.setText(weather.aqi.city.pm25);
        }

        String comfort = "舒适度" + weather.suggestion.comfort.info;
        String carWash = "洗车指数" + weather.suggestion.carWash.info;
        String sport = "运动建议" + weather.suggestion.sport.info;
        mComfortTextView.setText(comfort);
        mCarWashTextView.setText(carWash);
        mSportTextView.setText(sport);
        mScrollView.setVisibility(View.VISIBLE);
    }

    public void requestWeather(String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=bc0418b57b2d4918819d3974ac1285d9";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        mSwipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                        mSwipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
        loadBingPic();//每次请求天气后刷新背景图片
    }
}
