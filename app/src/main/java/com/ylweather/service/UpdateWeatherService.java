package com.ylweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.util.ULocale;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import com.ylweather.gson.Weather;
import com.ylweather.util.HttpUtil;
import com.ylweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class UpdateWeatherService extends Service {
    public UpdateWeatherService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
       return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();
        Log.d("updateWeather","正在更新天气数据");
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        int time= 5 * 1000;//5秒更新一次
        long triggerAtTime = SystemClock.elapsedRealtime()+time;
        Intent i = new Intent(this,UpdateWeatherService.class);
        PendingIntent pi= PendingIntent.getService(this,0,i,0);
        alarmManager.cancel(pi);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
        return super.onStartCommand(intent, flags, startId);
    }
    //更新天气
    private void updateWeather(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = sharedPreferences.getString("weather",null);
        if(weatherString!=null){
            Weather weather = Utility.handleWeatherResponse(weatherString);
            String weatherId = weather.basic.cid;
            String url="https://free-api.heweather.com/s6/weather/forecast?location="
                    +weatherId+"&key=01063f9ab9c145ce9f75f8a29d80d2d5";
            HttpUtil.sendOkHttpRequest(url, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseText=response.body().string();
                    Weather weather1=Utility.handleWeatherResponse(responseText);
                    if(weather1!=null&&weather1.status.equals("ok")){
                        SharedPreferences.Editor editor = PreferenceManager
                                .getDefaultSharedPreferences(UpdateWeatherService.this)
                                .edit();
                        editor.putString("weather",responseText);
                        editor.apply();
                    }
                }
            });
        }
    }
}












































