package io.github.oxmose.passlock.database;

import android.app.Activity;
import android.arch.persistence.room.Room;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutionException;

import io.github.oxmose.passlock.ApplicationContextProvider;

public class DatabaseSingleton {
    private static final DatabaseSingleton instance = new DatabaseSingleton();
    final private AppDatabase db;

    static public DatabaseSingleton getInstance() {
        return instance;
    }

    private DatabaseSingleton() {
        /* Get the instance of the database */
        db = Room.databaseBuilder(ApplicationContextProvider.getContext(),
                AppDatabase.class, "PassLockDB").build();
    }

    public boolean usernameExists(String usernameText) {

        try {
            return (new GetUserAsync(usernameText, db).execute().get() != null);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean createUser(User newUser) {
        try {
            new CreateUserAsync(newUser, db).execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean isFingerprintAccountSet() {
        try {
            return (new IsPrincypalUserSetAsync(db).execute().get().booleanValue());
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static class GetUserAsync extends AsyncTask<Void, Void, User> {
        private String username;
        private AppDatabase db;

        public GetUserAsync(String username, AppDatabase db) {
            this.username = username;
            this.db = db;
    }

        @Override
        protected User doInBackground(Void... params) {
            return db.userDAO().findByUsername(username);
        }
    }

    private static class CreateUserAsync extends AsyncTask<Void, Void, Void> {
        private User user;
        private AppDatabase db;

        public CreateUserAsync(User newUser, AppDatabase db) {
            this.user = newUser;
            this.db = db;
        }

        @Override
        protected Void doInBackground(Void... params) {
            db.userDAO().insert(user);
            return null;
        }
    }

    private static class IsPrincypalUserSetAsync extends AsyncTask<Void, Void, Boolean> {
        private AppDatabase db;

        public IsPrincypalUserSetAsync(AppDatabase db) {
            this.db = db;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return (db.userDAO().getPrincipalUser() != null);
        }
    }
}