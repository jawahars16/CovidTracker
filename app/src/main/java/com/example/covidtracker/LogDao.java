package com.example.covidtracker;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface LogDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(LogEntry logEntry);

    @Query("DELETE FROM logs")
    void deleteAll();

    @Query("SELECT * from logs ORDER BY timestamp DESC")
    LiveData<List<LogEntry>> getLogs();
}
