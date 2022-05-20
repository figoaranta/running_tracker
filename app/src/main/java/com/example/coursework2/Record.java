package com.example.coursework2;

import android.content.ContentValues;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.time.format.DateTimeFormatter;

@Entity(tableName = "record_table")
public class Record {

    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_IMAGE = "image";

    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int id;

    private String name;
    private int distance;
    private Long time;
    private String status;
    private String date;
    private String note;

    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    private byte[] image;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Record(String name){
        this.name = name;
        this.status = "ongoing";
        this.time = (long) 0;
        this.date = String.valueOf(java.time.LocalDate.now());
        this.distance = 0;
        this.image = null;
        this.note = null;
    }
    @Ignore
    @RequiresApi(api = Build.VERSION_CODES.O)
    public Record(String name,long time, int distance){
        this.name = name;
        this.status = "finish";
        this.time = time;
        this.date = String.valueOf(java.time.LocalDate.now());
        this.distance = distance;
        this.image = null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static Record fromContentValues(@Nullable ContentValues values) {
        Record record = new Record(null);
        if (values != null && values.containsKey(COLUMN_NAME)) {
            record.setName(values.getAsString(COLUMN_NAME));
        }
        if (values != null && values.containsKey(COLUMN_IMAGE)) {
            record.setImage(values.getAsByteArray(COLUMN_IMAGE));
        }
        return record;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getDistance() {
        return distance;
    }

    public void setDistance(Integer distance) {
        this.distance = distance;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public byte[] getImage() {
        return image;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
