package com.example.coursework2;

import android.database.Cursor;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface RecordDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertRecord(Record record);

    @Query("SELECT * FROM record_table")
    LiveData<List<Record>> getAllRecord();

    @Query("SELECT * FROM record_table WHERE id=:id")
    Record getRecord(int id);

    @Query("DELETE FROM record_table")
    void deleteAllRecord();

    @Query("DELETE FROM record_table WHERE id=:id")
    void deleteRecord(int id);

    @Query("SELECT * FROM record_table ORDER BY id DESC LIMIT 1")
    Record getLastRecord();

    @Update
    void updateRecord(Record record);

    @Query("SELECT * FROM record_table")
    Cursor getRecordAllFromContentProvider();

    @Query("SELECT * FROM record_table WHERE id=:id")
    Cursor getRecordByIdFromContentProvider(int id);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    Long insertRecordFromContentProvider(Record record);

    @Query("DELETE FROM record_table")
    int deleteAllRecordFromContentProvider();

    @Query("DELETE FROM record_table WHERE id=:id")
    int deleteRecordFromContentProvider(int id);

    @Update
    int updateRecordFromContentProvider(Record record);

    @Query("SELECT * FROM record_table WHERE date=:date")
    List<Record> getRecordsByDate(String date);

    @Query("SELECT * from record_table WHERE time = (SELECT MAX(time) from record_table WHERE date=:date) ")
    Record getRecordByMaxTime(String date);

}
