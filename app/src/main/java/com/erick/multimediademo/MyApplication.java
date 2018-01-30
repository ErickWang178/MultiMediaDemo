package com.erick.multimediademo;

import android.app.Application;

/**
 * Created by Administrator on 2018/1/30 0030.
 */

public class MyApplication extends Application {
    private static MyApplication mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static MyApplication getAppContext(){
        return mInstance;
    }
}
