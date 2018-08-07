package io.github.oxmose.passlock.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(foreignKeys = @ForeignKey(entity = User.class,
        parentColumns = "username",
        childColumns = "user",
        onDelete = CASCADE), indices = {@Index("user")})
public class Password {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "value")
    private String value;

    @ColumnInfo(name = "user")
    private String user;

    @ColumnInfo(name = "cat_password")
    private boolean isPassword;

    @ColumnInfo(name = "cat_pin")
    private boolean isPin;

    @ColumnInfo(name = "cat_digicode")
    private boolean isDigicode;

    @ColumnInfo(name = "note")
    private String note;

    @ColumnInfo(name = "associated_account")
    private String associatedAccount;

    @ColumnInfo(name = "is_favorite")
    private boolean isFavorite;

    public Password(String name, String value, String username, String associatedAccount, String note,
                    boolean isPassword, boolean isPin, boolean isDigicode, boolean isFavorite) {
        this.name = name;
        this.value = value;
        this.user = username;
        this.isPassword = isPassword;
        this.isPin = isPin;
        this.isDigicode = isDigicode;
        this.note = note;
        this.associatedAccount = associatedAccount;
        this.isFavorite = isFavorite;
    }

    public Password() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public boolean isPassword() {
        return isPassword;
    }

    public void setPassword(boolean password) {
        isPassword = password;
    }

    public boolean isPin() {
        return isPin;
    }

    public void setPin(boolean pin) {
        isPin = pin;
    }

    public boolean isDigicode() {
        return isDigicode;
    }

    public void setDigicode(boolean digicode) {
        isDigicode = digicode;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getAssociatedAccount() {
        return associatedAccount;
    }

    public void setAssociatedAccount(String associatedAccount) {
        this.associatedAccount = associatedAccount;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }
}
