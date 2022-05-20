package com.example.coursework2;

import android.app.Application;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

public class MainActivityViewModel extends AndroidViewModel {

    public final static String TAG = "mobile";
    private MyRepository repository;
    private LiveData<List<Record>> records;
    private MutableLiveData<MyService.MyBinder> mBinder = new MutableLiveData<>();
    private MutableLiveData<Long> mTime = new MutableLiveData<>();
    private MutableLiveData<Integer> mDistance = new MutableLiveData<>();
    private MutableLiveData<Boolean> mIsPaused = new MutableLiveData<>();

    public MainActivityViewModel(@NonNull Application application) {
        super(application);
        repository = new MyRepository(application);
        records = repository.getRecords();
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected: Success");
            MyService.MyBinder binder = (MyService.MyBinder) service;
            mBinder.postValue(binder);
            ((MyService.MyBinder) service).registerCallback(callback);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBinder.postValue(null);
        }
    };

    ICallback callback = new ICallback() {
        @Override
        public void update(long time, int distance, boolean isPaused) {
            mTime.postValue(time);
            mIsPaused.postValue(isPaused);

            if(mDistance.getValue() != null){
                if(mDistance.getValue() != distance){
                    mDistance.postValue(distance);
                }
            }else{
                mDistance.postValue(distance);
            }
        }
    };

    public LiveData<MyService.MyBinder> getBinder(){
        return mBinder;
    }

    public ServiceConnection getServiceConnection(){
        return serviceConnection;
    }

    public LiveData<List<Record>> getRecords() {
        return records;
    }

    public void insertRecord(Record record){
        repository.insertRecord(record);
    }

    public Record getLastRecord(){
        return repository.getLastRecord();
    }

    public void updateLastRecord(Record record){
        repository.updateLastRecord(record);
    }

    public List<Record> getRecordsByDate(String date){
        return repository.getRecordsByDate(date);
    }

    public int getTotalDistanceByDate(String date){
        int distance = 0;
        List<Record> records= getRecordsByDate(date);
        for (Record record: records
             ) {
            distance += record.getDistance();
        }
        return distance;
    }

    public long getTotalTimeByDate(String date){
        long time = 0;
        List<Record> records= getRecordsByDate(date);
        for (Record record: records
        ) {
            time += record.getTime();
        }
        return time;
    }

    public LiveData<Long> getTime(){
        return mTime;
    }

    public LiveData<Integer> getDistance(){
        return mDistance;
    }

    public LiveData<Boolean> getIsPaused(){
        return mIsPaused;
    }

    public Record getRecordByMaxTime(String date){return repository.getRecordByMaxTime(date);}
}
