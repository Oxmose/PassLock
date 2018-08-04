package io.github.oxmose.passlock;

import io.github.oxmose.passlock.database.User;

class Session {
    private static final Session instance = new Session();

    private User currentUser;

    static Session getInstance() {
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
