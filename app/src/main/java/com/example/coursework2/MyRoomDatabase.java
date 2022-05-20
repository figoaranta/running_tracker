package com.example.coursework2;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Record.class},version = 2, exportSchema = false)
public abstract class MyRoomDatabase extends RoomDatabase {
    public abstract RecordDao recordDao();
    private static volatile MyRoomDatabase INSTANCE;
    static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(4);

    static MyRoomDatabase getDatabase(final Context context){
        if(INSTANCE == null){
            synchronized (MyRoomDatabase.class){
                INSTANCE = Room.databaseBuilder(context.getApplicationContext(),MyRoomDatabase.class,"database")
                        .fallbackToDestructiveMigration().allowMainThreadQueries().build();
            }
        }
        return INSTANCE;
    };
}
