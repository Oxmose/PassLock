package io.github.oxmose.passlock.tools;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

public class ApplicationContextProvider extends Application {

    /**
     * Keeps a reference of the application context
     */
    @SuppressLint("StaticFieldLeak")
    private static Context sContext = null;

    @Override
    public void onCreate() {
        super.onCreate();

        sContext = getApplicationContext();
    }

    /**
     * Returns the application context
     *
     * @return application context
     */
    public static Context getContext() {
        return sContext;
    }

}
