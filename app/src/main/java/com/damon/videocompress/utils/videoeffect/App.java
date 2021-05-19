package com.damon.videocompress.utils.videoeffect;

import android.app.Application;
import android.content.Context;

public class App extends Application {

    public static Context sApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = getApplicationContext();
    }
}
