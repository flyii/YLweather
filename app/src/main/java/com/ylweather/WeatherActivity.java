package com.ylweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.ylweather.gson.DailyForecast;
import com.ylweather.gson.Weather;
import com.ylweather.service.UpdateWeatherService;
import com.ylweather.util.HttpUtil;
import com.ylweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    private ScrollView scrollView;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfo;
    private LinearLayout forecastLayout;
    public SwipeRefreshLayout refreshLayout;
    private Button changeCity;
    public DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        //初始化控件
        scrollView = (ScrollView)findViewById(R.id.weather_layout);
        titleCity = (TextView)findViewById(R.id.title_city);
        titleUpdateTime = (TextView)findViewById(R.id.title_ut);
        degreeText = (TextView)findViewById(R.id.degree_text);
        weatherInfo = (TextView)findViewById(R.id.weather_info);
        forecastLayout = (LinearLayout)findViewById(R.id.forecast_item);
        refreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
        refreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));
        changeCity = (Button)findViewById(R.id.change_city);
        drawerLayout = (DrawerLayout)findViewById(R.id.change_city_layout);
        changeCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString=preferences.getString("weather",null);
        final String weatherId;
        if(weatherString!=null){
            Weather weather = Utility.handleWeatherResponse(weatherString);
            weatherId=weather.basic.cid;
            showWeatherInfo(weather);
        }else{
            weatherId = getIntent().getStringExtra("weather_id");
            scrollView.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(weatherId);
            }
        });
    }
    //根据天气ID请求天气信息
    public void requestWeather(final String weatherId){
        String url="https://free-api.heweather.com/s6/weather/forecast?location="
                +weatherId+"&key=01063f9ab9c145ce9f75f8a29d80d2d5";
        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                        refreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String weatherText= response.body().string();
                final Weather weather = Utility.handleWeatherResponse(weatherText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather!=null && weather.status.equals("ok")){
                            SharedPreferences.Editor editor = PreferenceManager
                                    .getDefaultSharedPreferences(WeatherActivity.this)
                                    .edit();
                            editor.putString("weather", weatherText);
                            editor.apply();
                            showWeatherInfo(weather);
                        }else{
                            Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                        }
                        refreshLayout.setRefreshing(false);
                    }
                });
            }
        });
    }

    //显示天气信息
    private void showWeatherInfo(Weather weather){
        titleCity.setText(weather.basic.location);
        String[] datas=weather.update.loc.split(" ");
        titleUpdateTime.setText(datas[1]);
        forecastLayout.removeAllViews();
        for(DailyForecast forecast : weather.daily_forecast){
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
            if(forecast.date.equals(datas[0])){
                degreeText.setText(forecast.tmp_max+"℃");
                weatherInfo.setText(forecast.cond_txt_d);
            }else{
                TextView dataText = (TextView)view.findViewById(R.id.data_text);
                TextView infoText = (TextView)view.findViewById(R.id.info_text);
                TextView maxText = (TextView)view.findViewById(R.id.max_text);
                TextView minText = (TextView)view.findViewById(R.id.min_text);
                dataText.setText(forecast.date);
                infoText.setText(forecast.cond_txt_d);
                maxText.setText(forecast.tmp_max);
                minText.setText(forecast.tmp_min);
                forecastLayout.addView(view);
            }
        }
        Intent intent = new Intent(this, UpdateWeatherService.class);
        startService(intent);//启动后台自动更新天气服务
        scrollView.setVisibility(View.VISIBLE);
    }
}





























