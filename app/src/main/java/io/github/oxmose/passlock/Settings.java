package io.github.oxmose.passlock;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class Settings {

    /* Sharedpreferences reader and editor */
    private SharedPreferences reader;
    private SharedPreferences.Editor writer;

    private static final Settings instance = new Settings();

    public static Settings getInstance() {
        return instance;
    }

    private Settings() {
        /* Get application context */
        Context context = ApplicationContextProvider.getContext();

        /* Get shared preference reader and writer */
        reader = context.getSharedPreferences("io.github.oxmose.passlock", Context.MODE_PRIVATE);
        writer = reader.edit();
    }

    public boolean getLastConnectionExists() {
        Log.i("SharedPreference",
              "lastConnectionExists: " +
                      reader.getBoolean("lastConnectionExists", false));
        return reader.getBoolean("lastConnectionExists", false);
    }



}
