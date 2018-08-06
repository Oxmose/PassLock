package io.github.oxmose.passlock.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

@Dao
public interface UserDAO {
    @Query("SELECT * FROM user WHERE username = (:username)")
    User findByUsername(String username);

    @Insert
    void insert(User user);

    @Delete
    void delete(User user);

    @Query("SELECT * FROM user WHERE is_principal = 1")
    User getPrincipalUser();
}