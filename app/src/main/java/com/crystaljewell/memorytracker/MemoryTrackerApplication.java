package com.crystaljewell.memorytracker;

import android.app.Application;

/**
 * Created by crystaladkins on 5/31/17.
 */

public class MemoryTrackerApplication extends Application {

    private static MemoryTrackerApplication application;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
    }

    public static MemoryTrackerApplication getInstance() {
        return application;
    }
}
