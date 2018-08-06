package io.github.oxmose.passlock.data;

import io.github.oxmose.passlock.database.Password;
import io.github.oxmose.passlock.database.User;

public class Session {
    private static final Session instance = new Session();

    private User currentUser;

    static public Session getInstance() {
        return instance;
    }

    private Session() {
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }
}
