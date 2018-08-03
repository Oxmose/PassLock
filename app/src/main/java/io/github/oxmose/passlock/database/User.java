package io.github.oxmose.passlock.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import javax.annotation.Nonnull;

@Entity
public class User {

    @PrimaryKey
    @NonNull
    private String username = "";

    @ColumnInfo(name = "password")
    private String password;

    @ColumnInfo(name = "is_principal")
    private boolean isPrincipal;

    public User(@NonNull String usernameText, String passwordText, boolean isPrincipal) {
        this.username = usernameText;
        this.password = passwordText;
        this.isPrincipal = isPrincipal;
    }

    public User() {}

    public boolean isPrincipal() {
        return isPrincipal;
    }

    @NonNull
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setUsername(@NonNull String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPrincipal(boolean principal) {
        isPrincipal = principal;
    }
}
