package com.example.covidtracker;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.util.List;

public class LogRepo {
    private LogDao logDao;
    private List<LogEntry> logs;

    // Note that in order to unit test the WordRepository, you have to remove the Application
    // dependency. This adds complexity and much more code, and this sample is not about testing.
    // See the BasicSample in the android-architecture-components repository at
    // https://github.com/googlesamples
    LogRepo(Context application) {
        LogDatabase db = LogDatabase.getDatabase(application);
        logDao = db.logDao();
    }

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    LiveData<List<LogEntry>> getAllLogs() {
        return logDao.getLogs();
    }

    // You must call this on a non-UI thread or your app will throw an exception. Room ensures
    // that you're not doing any long running operations on the main thread, blocking the UI.
    void insert(LogEntry entry) {
        LogDatabase.databaseWriteExecutor.execute(() -> {
            logDao.insert(entry);
        });
    }

    void deleteAll() {
        LogDatabase.databaseWriteExecutor.execute(() -> {
            logDao.deleteAll();
        });
    }
}
