package io.github.oxmose.passlock.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class User {
    @PrimaryKey
    private String username;

    @ColumnInfo(name = "password")
    private String password;

    @ColumnInfo(name = "is_principal")
    private boolean isPrincipal;
}
