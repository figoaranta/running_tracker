package com.example.coursework2;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

public class RecordActivityViewModel  extends AndroidViewModel {

    public final static String TAG = "mobile";
    private MyRepository repository;
    private MutableLiveData<MyService.MyBinder> mBinder = new MutableLiveData<>();
    private MutableLiveData<Boolean> mIsTracking = new MutableLiveData<>();
    private MutableLiveData<Long> mTime = new MutableLiveData<>();
    private MutableLiveData<Integer> mDistance = new MutableLiveData<>();
    private MutableLiveData<Boolean> mIsPaused = new MutableLiveData<>();
    private MutableLiveData<byte[]> image= new MutableLiveData<>();

    public RecordActivityViewModel(@NonNull Application application) {
        super(application);
        repository = new MyRepository(application);
        mIsTracking.postValue(true);
        if(repository.getLastRecord() != null){
            image.postValue(repository.getLastRecord().getImage());
        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected: Success");
            MyService.MyBinder binder = (MyService.MyBinder) service;
            mBinder.postValue(binder);
            ((MyService.MyBinder) service).registerCallback(callback);

            binder.getService().getIsPaused2().observeForever(new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean aBoolean) {
                    Log.d(TAG, "onChanged: "+aBoolean);
                    mIsTracking.postValue(!aBoolean);
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBinder.postValue(null);
            mBinder.getValue().unRegisterCallback();
        }

    };

    ICallback callback = new ICallback() {
        @Override
        public void update(long time, int distance, boolean isPaused) {
            mTime.postValue(time);
            mDistance.postValue(distance);
            mIsPaused.postValue(isPaused);
            Log.d(TAG, "update: "+distance);
        }
    };

    public LiveData<MyService.MyBinder> getBinder(){
        return mBinder;
    }

    public ServiceConnection getServiceConnection(){
        return serviceConnection;
    }

    public void insertRecord(Record record){
        repository.insertRecord(record);
    }

    public void setIsTracking(Boolean isTracking){
        mIsTracking.postValue(isTracking);
    }

    public LiveData<Boolean> getIsTracking(){
        return mIsTracking;
    }

    public Record getLastRecord(){
        return repository.getLastRecord();
    }

    public Record getRecord(int id){
        return repository.getRecord(id);
    }

    public void updateLastRecord(Record record){
        repository.updateLastRecord(record);

    }

    public LiveData<Long> getTime(){
        return mTime;
    }

    public LiveData<Integer> getDistance(){
        return mDistance;
    }

    public  void setTime(long time){
        mTime.postValue(time);
    }

    public void setImage(byte[] image){
        this.image.postValue(image);
    }

    public LiveData<byte[]> getImage() {
        return image;
    }
}
