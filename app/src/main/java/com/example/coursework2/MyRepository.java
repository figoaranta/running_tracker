package com.example.coursework2;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.RoomDatabase;

import java.util.List;

public class MyRepository {
    public final static String TAG = "mobile";
    private RecordDao recordDao;
    public LiveData<List<Record>> records;
    public MutableLiveData<Record> lastRecord;

    MyRepository(Application application){
        MyRoomDatabase db = MyRoomDatabase.getDatabase(application);
        recordDao = db.recordDao();
        records = recordDao.getAllRecord();
    }

    void insertRecord(Record record){
        MyRoomDatabase.databaseWriteExecutor.execute(()->{
            recordDao.insertRecord(record);
        });
    }

    LiveData<List<Record>> getRecords(){
        return records;
    }

    Record getRecord(int id){
        return recordDao.getRecord(id);
    }

    Record getLastRecord(){
        return recordDao.getLastRecord() ;
    }

    void updateLastRecord(Record record){
        recordDao.updateRecord(record);
    }

    void deleteRecord(int id){
        recordDao.deleteRecord(id);
    }

    List<Record> getRecordsByDate(String date){
        return recordDao.getRecordsByDate(date);
    }

    Record getRecordByMaxTime(String date){return recordDao.getRecordByMaxTime(date);}
}
