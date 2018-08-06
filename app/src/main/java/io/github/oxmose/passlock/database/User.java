package io.github.oxmose.passlock.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(indices = {@Index("username")})
public class User {

    @PrimaryKey
    @NonNull
    private String username = "";

    @ColumnInfo(name = "password")
    private String password;

    @ColumnInfo(name = "salt")
    private String salt;

    @ColumnInfo(name = "is_principal")
    private boolean isPrincipal;

    @ColumnInfo(name = "avatar")
    private String avatar;

    private int passwordCount;

    private String decryptionKey;

    public User(@NonNull String usernameText, String passwordText,
                boolean isPrincipal, String avatar) {
        this.username = usernameText;
        this.password = passwordText;
        this.isPrincipal = isPrincipal;
        this.avatar = avatar;

        passwordCount = 0;
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

    public String getAvatar() {
        return avatar;
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

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }


    public String getDecryptionKey() {
        return decryptionKey;
    }

    public void setDecryptionKey(String decryptionKey) {
        this.decryptionKey = decryptionKey;
    }

    public int getPasswordCount() {
        return passwordCount;
    }

    public void setPasswordCount(int passwordCount) {
        this.passwordCount = passwordCount;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }
}
