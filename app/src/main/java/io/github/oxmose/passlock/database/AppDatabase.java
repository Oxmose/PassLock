package io.github.oxmose.passlock.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {User.class}, version = 7)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDAO userDAO();
}