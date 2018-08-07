package io.github.oxmose.passlock.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface PasswordDAO {
    @Query("SELECT * FROM password WHERE id = (:id)")
    Password findById(int id);

    @Insert
    void insert(Password password);

    @Delete
    void delete(Password password);

    @Update
    void update(Password password);

    @Query("SELECT COUNT(*) FROM password WHERE user = (:username)")
    int getPasswordCount(String username);

    @Query("SELECT * FROM password WHERE user = (:username) AND name = (:passwordName)")
    Password getUserPassword(String username, String passwordName);

    @Query("SELECT * FROM password WHERE user = (:username) AND name LIKE (:text)")
    List<Password> getUserPasswordsLike(String username, String text);

    @Query("SELECT * FROM password WHERE user = (:username) AND name LIKE (:text) AND is_favorite = 1")
    List<Password> getUserFavoritePasswordsLike(String username, String text);

    @Query("SELECT * FROM password WHERE user = (:username) AND name LIKE (:text) AND cat_password = 1")
    List<Password> getUserPasswordPasswordsLike(String username, String text);

    @Query("SELECT * FROM password WHERE user = (:username) AND name LIKE (:text) AND cat_pin = 1")
    List<Password> getUserPinPasswordsLike(String username, String text);

    @Query("SELECT * FROM password WHERE user = (:username) AND name LIKE (:text) AND cat_digicode = 1")
    List<Password> getUserDigicodePasswordsLike(String username, String text);
}