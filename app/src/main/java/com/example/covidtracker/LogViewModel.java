package com.example.covidtracker;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class LogViewModel extends AndroidViewModel {
    private LogRepo mRepository;

    private LiveData<List<LogEntry>> mAllWords;

    public LogViewModel(Application application) {
        super(application);
        mRepository = new LogRepo(application);
        mAllWords = mRepository.getAllLogs();
    }

    LiveData<List<LogEntry>> getAllWords() {
        return mAllWords;
    }

    public void deleteAll() {
        mRepository.deleteAll();
    }

    public void insert(LogEntry word) {
        mRepository.insert(word);
    }
}
