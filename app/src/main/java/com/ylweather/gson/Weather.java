package com.ylweather.gson;

import com.ylweather.util.Utility;

import java.util.List;

/**
 * Created by gaohui on 2018/9/12.
 */

public class Weather {
    public String status;//	接口状态,返回信息
    public List<DailyForecast> daily_forecast;
    public Update update;
    public Basic basic;
}
