package com.example.coursework2;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class CompletedRecordViewModel extends AndroidViewModel {
    public final static String TAG = "mobile";
    private MyRepository repository;
    private MutableLiveData<Boolean> hasImage = new MutableLiveData<>();

    public CompletedRecordViewModel(@NonNull Application application) {
        super(application);
        repository = new MyRepository(application);
    }

    public Record getRecord(int id){
        Record record =  repository.getRecord(id);
        if (record.getImage()!=null){
            hasImage.postValue(true);
        }else{
            hasImage.postValue(false);
        }
        return record;
    }

    public MutableLiveData<Boolean> getHasImage() {
        return this.hasImage;
    }

    public void setHasImage(Boolean hasImage) {
        this.hasImage.postValue(hasImage);
    }

    public void updateRecord(Record record){
        repository.updateLastRecord(record);
    }

    public void deleteRecord(int id){
        repository.deleteRecord(id);
    }
}
