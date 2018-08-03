package io.github.oxmose.passlock.database;

import android.arch.persistence.room.Room;

import io.github.oxmose.passlock.ApplicationContextProvider;

public class DatabaseSingleton {
    private static final DatabaseSingleton instance = new DatabaseSingleton();
    private AppDatabase db;

    static public DatabaseSingleton getInstance() {
        return instance;
    }

    private DatabaseSingleton() {
        /* Get the instance of the database */
        db = Room.databaseBuilder(ApplicationContextProvider.getContext(),
                AppDatabase.class, "PassLockDB").build();
    }

    public boolean usernameExists(String usernameText) {
        return (db.userDAO().findByUsername(usernameText) != null);
    }
}
