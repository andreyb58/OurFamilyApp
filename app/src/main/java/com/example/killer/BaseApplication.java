package com.example.killer;

import android.app.Application;
import android.content.Context;
import androidx.multidex.MultiDex;


public class BaseApplication extends Application {

    private static BaseApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;


        // Initialize other components
        initializeAppComponents();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static BaseApplication getInstance() {
        return instance;
    }

    private void initializeAppComponents() {
        // Initialize preferences, database, etc.
    }
}