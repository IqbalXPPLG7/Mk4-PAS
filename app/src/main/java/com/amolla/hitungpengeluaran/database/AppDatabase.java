package com.amolla.hitungpengeluaran.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.amolla.hitungpengeluaran.database.dao.DatabaseDao;
import com.amolla.hitungpengeluaran.model.ModelDatabase;

@Database(entities = {ModelDatabase.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract DatabaseDao databaseDao();
}
