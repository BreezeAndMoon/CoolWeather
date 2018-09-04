package com.example.fym.coolweather.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fym.coolweather.Constant;
import com.example.fym.coolweather.MainActivity;
import com.example.fym.coolweather.R;
import com.example.fym.coolweather.WeatherActivity;
import com.example.fym.coolweather.db.City;
import com.example.fym.coolweather.db.Country;
import com.example.fym.coolweather.db.Province;
import com.example.fym.coolweather.util.HttpUtil;
import com.example.fym.coolweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.internal.Util;

/**
 * Created by fym on 2018/8/23.
 */

public class ChooseAreaFragment extends Fragment {
    private static final String TAG = "ChooseAreaFragment";

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTRY = 2;
    private TextView mTitleTextView;
    private Button mBackButton;
    private ListView mListView;
    private ProgressDialog mProgressDialog;
    private ArrayAdapter<String> mArrayAdapter;
    private List<String> mDataList = new ArrayList<>();
    private List<Province> mProvinceList;
    private List<City> mCityList;
    private List<Country> mCountryList;
    private Province mSelectedProvince;
    private City mSelectedCity;
    private int mCurrentLevel;


    @Nullable
    @Override

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        mTitleTextView = view.findViewById(R.id.tv_title);
        mListView = view.findViewById(R.id.list_view);
        mArrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, mDataList);
        mListView.setAdapter(mArrayAdapter);
        //郭神是在onActivityCreated方法中设置点击事件
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mCurrentLevel == LEVEL_PROVINCE) {
                    mSelectedProvince = mProvinceList.get(position);
                    queryCities();
                } else if (mCurrentLevel == LEVEL_CITY) {
                    mSelectedCity = mCityList.get(position);
                    queryCountries();
                } else if (mCurrentLevel == LEVEL_COUNTRY) {
                   String weatherId  = mCountryList.get(position).getWeatherId();
                   if(getActivity() instanceof MainActivity) {
                       Intent intent = new Intent(getActivity(), WeatherActivity.class);
                       intent.putExtra(Constant.WEATHERID, weatherId);
                       startActivity(intent);
                       getActivity().finish();
                   }else if(getActivity() instanceof WeatherActivity){
                       WeatherActivity weatherActivity = (WeatherActivity) getActivity();
                       weatherActivity.mDrawerLayout.closeDrawers();
                       weatherActivity.mSwipeRefresh.setRefreshing(true);
                       weatherActivity.requestWeather(weatherId);
                   }
                }
            }
        });
//        mBackButton = view.findViewById(R.id.button_back);
//        mBackButton.setOnClickListener((v) -> {
//            if (mCurrentLevel == LEVEL_COUNTRY) {
//                queryCities();
//            } else if (mCurrentLevel == LEVEL_CITY) {
//                queryProvinces();
//            }
//        });

        return view;
    }

    private void queryProvinces() {
        mTitleTextView.setText("中国");
        mBackButton.setVisibility(View.GONE);
        mProvinceList = DataSupport.findAll(Province.class);
        if (mProvinceList.size() > 0) {
            mDataList.clear();
            for (Province province : mProvinceList) {
                mDataList.add(province.getProvinceName());
            }
            mArrayAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            mCurrentLevel = LEVEL_PROVINCE;
        } else {
            String address = "http://guolin.tech/api/china";
            queryFromServe(address, Constant.PROVINCE);
        }
    }

    private void queryCities() {
        mTitleTextView.setText(mSelectedProvince.getProvinceName());
        mBackButton.setVisibility(View.VISIBLE);
        mCityList = DataSupport.where("provinceid=?", String.valueOf(mSelectedProvince.getId())).find(City.class);
        if (mCityList.size() > 0) {
            mDataList.clear();
            for (City city : mCityList) {
                mDataList.add(city.getCityName());
            }
            mArrayAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            mCurrentLevel = LEVEL_CITY;
        }else{
            int provinceCode = mSelectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/"+provinceCode;
            queryFromServe(address,Constant.CITY);
        }
    }

    private void queryCountries() {
        mTitleTextView.setText(mSelectedCity.getCityName());
        mBackButton.setVisibility(View.VISIBLE);
        mCountryList = DataSupport.where("cityid=?", String.valueOf(mSelectedCity.getId())).find(Country.class);
        if (mCountryList.size() > 0) {
            mDataList.clear();
            for (Country country : mCountryList) {
                mDataList.add(country.getCountryName());
            }
            mArrayAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            mCurrentLevel = LEVEL_COUNTRY;
        } else {
            int provinceCode = mSelectedProvince.getProvinceCode();
            int cityCode = mSelectedCity.getCityCode();
            String address ="http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            queryFromServe(address,Constant.COUNTRY);
        }
    }


    private void queryFromServe(String address, String type) {
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(() -> {
                    closeProgressDialog();
                    Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body().string();
                boolean result = false;
                if (Constant.PROVINCE.equals(type)) {
                    result = Utility.handleProvinceResponse(responseStr);
                } else if (Constant.CITY.equals(type)) {
                    result = Utility.handleCityResponse(responseStr, mSelectedProvince.getId());
                } else if (Constant.COUNTRY.equals(type)) {
                    result = Utility.handleCountryResponse(responseStr, mSelectedCity.getId());
                }
                if (result) {
                    getActivity().runOnUiThread(() -> {
                        closeProgressDialog();
                        if (Constant.PROVINCE.equals(type)) {
                            queryProvinces();
                        } else if (Constant.CITY.equals(type)) {
                            queryCities();
                        } else if (Constant.COUNTRY.equals(type)) {
                            queryCountries();
                        }
                    });
                }

            }
        });
    }

    private void closeProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getContext());
            mProgressDialog.setMessage("正在加载");
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mProgressDialog.show();
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //下面Button的这种初始化方式不能在onCreateView方法中使用，在onCreateView中只能使用view.findViewById
        mBackButton = getActivity().findViewById(R.id.button_back);
        mBackButton.setOnClickListener((v) -> {
            if (mCurrentLevel == LEVEL_COUNTRY) {
                queryCities();
            } else if (mCurrentLevel == LEVEL_CITY) {
                queryProvinces();
            }
        });
        queryProvinces();
    }
}
