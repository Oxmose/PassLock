package io.github.oxmose.passlock.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface PasswordDAO {
    @Query("SELECT * FROM password WHERE id = (:id)")
    Password findById(int id);

    @Insert
    void insertAll(Password... passwords);

    @Insert
    void insert(Password password);

    @Delete
    void delete(Password password);

    @Query("SELECT COUNT(*) FROM password WHERE user = (:username)")
    int getPasswordCount(String username);

    @Query("SELECT * FROM password WHERE user = (:username) AND name = (:passwordName)")
    Password getUserPassword(String username, String passwordName);

    @Query("SELECT * FROM password WHERE user = (:username)")
    List<Password> getAllUserPasswords(String username);

    @Query("SELECT * FROM password WHERE user = (:username) AND name LIKE (:text)")
    List<Password> getUserPassordsLike(String username, String text);
}