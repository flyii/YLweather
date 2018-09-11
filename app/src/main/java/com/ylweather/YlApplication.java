package com.ylweather;

import android.app.Application;

import org.litepal.LitePal;

/**
 * Created by gaohui on 2018/9/11.
 */

public class YlApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        LitePal.initialize(this);//把全局application context传给LitePal
    }
}
