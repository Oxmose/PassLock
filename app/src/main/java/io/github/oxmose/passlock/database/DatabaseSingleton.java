package io.github.oxmose.passlock.database;

import android.arch.persistence.room.Room;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import io.github.oxmose.passlock.tools.ApplicationContextProvider;

public class DatabaseSingleton {
    private static final DatabaseSingleton instance = new DatabaseSingleton();

    public AppDatabase getDb() {
        return db;
    }

    final private AppDatabase db;

    static public DatabaseSingleton getInstance() {
        return instance;
    }

    private DatabaseSingleton() {
        /* Get the instance of the database */
        db = Room.databaseBuilder(ApplicationContextProvider.getContext(),
                AppDatabase.class, "PassLockDB").fallbackToDestructiveMigration().build();
    }

    public boolean usernameExists(String usernameText) {

        try {
            return (new GetUserAsync(usernameText, db).execute().get() != null);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean createUser(User newUser) {
        try {
            new CreateUserAsync(newUser, db).execute().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean isFingerprintAccountSet() {
        try {
            return (new IsPrincypalUserSetAsync(db).execute().get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }

    public User getUser(String username, String password) {
        try {
            return (new UserLoginAsync(username, password, db).execute().get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    public User getPincipalUser() {
        try {
            return (new GetPrincypalUserAsync(db).execute().get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void deleteUser(User newUser) {
        try {
            new DeleteUserAsync(newUser, db).execute().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public User getUser(String username) {
        try {
            return (new GetUserAsync(username, db).execute().get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean passwordNameExists(String passwordName, String username) {
        try {
            return (new GetPasswordAsync(username, passwordName, db).execute().get() != null);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean createPassword(Password newPassword) {
        try {
            new CreatePasswordAsync(newPassword, db).execute().get();
            return true;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getPasswordCount(User user) {
        try {
            return new GetPasswordCountAsync(user.getUsername(), db).execute().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public String getUserPasword(String username) {
        User user = getUser(username);
        if(user != null)
            return user.getPassword();

        return "";
    }

    public List<Password> getUserPasswordsNonAsync(User user, String text) {
        return db.passwordDAO().getUserPassordsLike(user.getUsername(), "%" + text + "%");
    }

    private static class GetPasswordCountAsync extends AsyncTask<Void, Void, Integer> {
        private String username;
        private AppDatabase db;

        GetPasswordCountAsync(String username, AppDatabase db) {
            this.username = username;
            this.db = db;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            return db.passwordDAO().getPasswordCount(username);
        }
    }

    private static class GetUserAsync extends AsyncTask<Void, Void, User> {
        private String username;
        private AppDatabase db;

        GetUserAsync(String username, AppDatabase db) {
            this.username = username;
            this.db = db;
        }

        @Override
        protected User doInBackground(Void... params) {
            User user = db.userDAO().findByUsername(username);
            if(user!= null)
                user.setPasswordCount(db.passwordDAO().getPasswordCount(username));

            return user;
        }
    }

    private static class CreateUserAsync extends AsyncTask<Void, Void, Void> {
        private User user;
        private AppDatabase db;

        CreateUserAsync(User newUser, AppDatabase db) {
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

        IsPrincypalUserSetAsync(AppDatabase db) {
            this.db = db;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return (db.userDAO().getPrincipalUser() != null);
        }
    }

    private static class GetPrincypalUserAsync extends AsyncTask<Void, Void, User> {
        private AppDatabase db;

        GetPrincypalUserAsync(AppDatabase db) {
            this.db = db;
        }

        @Override
        protected User doInBackground(Void... params) {
            return db.userDAO().getPrincipalUser();
        }
    }

    private static class UserLoginAsync extends AsyncTask<Void, Void, User> {
        private String username;
        private String password;
        private AppDatabase db;

        UserLoginAsync(String username, String password, AppDatabase db) {
            this.username = username;
            this.password = password;
            this.db = db;
        }

        @Override
        protected User doInBackground(Void... params) {
            User user = db.userDAO().findByUsername(username);
            if(user == null)
                return null;

            if(user.getPassword().equals(password))
                return user;

            return null;
        }
    }

    private static class DeleteUserAsync extends AsyncTask<Void, Void, Void> {
        private User user;
        private AppDatabase db;

        DeleteUserAsync(User newUser, AppDatabase db) {
            this.user = newUser;
            this.db = db;
        }

        @Override
        protected Void doInBackground(Void... params) {
            db.userDAO().delete(user);
            return null;
        }
    }

    private static class GetPasswordAsync extends AsyncTask<Void, Void, Password> {
        private String username;
        private String passwordName;
        private AppDatabase db;

        GetPasswordAsync(String username, String passwordName, AppDatabase db) {
            this.username = username;
            this.passwordName = passwordName;
            this.db = db;
        }

        @Override
        protected Password doInBackground(Void... params) {
            return db.passwordDAO().getUserPassword(username, passwordName);
        }
    }

    private static class CreatePasswordAsync extends AsyncTask<Void, Void, Void> {
        private Password password;
        private AppDatabase db;

        CreatePasswordAsync(Password password, AppDatabase db) {
            this.password = password;
            this.db = db;
        }

        @Override
        protected Void doInBackground(Void... params) {
            db.passwordDAO().insert(password);
            return null;
        }
    }
}
