package com.example.btshare;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import java.lang.ref.WeakReference;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DatabaseWorker.initialize(MyApplication.this);
        ProcessorController.initialize();

    }
    private static WeakReference<MainActivity> mainActivityWeakRef;

    public static void setMainActivity(MainActivity mainActivity) {
        mainActivityWeakRef = new WeakReference<>(mainActivity);
    }

    public static MainActivity getMainActivity() {
        return mainActivityWeakRef.get();
    }



}

