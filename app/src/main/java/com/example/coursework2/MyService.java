package com.example.coursework2;

import android.Manifest;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteCallbackList;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MyService extends Service {

    private static final String TAG = "mobile";

    private IBinder mBinder = new MyBinder();
    private Handler mHandler;
    private Boolean mIsPaused;
    private MutableLiveData<Boolean> mIsPaused2 = new MutableLiveData<>();
    private long mTotalTime;
    private int mTotalDistance;
    private float distanceTravelled;
    private long startTime,endTime;
    private final String CHANNEL_ID = "100";
    int NOTIFICATION_ID = 001;
    private MyRepository repository;

    private MyLocationListener locationListener;
    private LocationManager locationManager;
    private Location location;
    private NotificationCompat.Builder mBuilder;
    private NotificationManager notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler();
        mIsPaused = true;
        mIsPaused2.postValue(true);
        mTotalTime = 0;
        mTotalDistance = 0;
        distanceTravelled = 0;
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        locationListener = new MyLocationListener(null);
        repository = new MyRepository(getApplication());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            Bundle bundle = intent.getExtras();
            if(bundle != null){
                Boolean receiveNotificationToggle = bundle.getBoolean("fromNotification");
                if(receiveNotificationToggle){
                    if(mIsPaused2.getValue()!=null && mIsPaused2.getValue()){
                        unPausedTracking();
                    }else{
                        pauseTracking();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    RemoteCallbackList<MyBinder> remoteCallbackList = new RemoteCallbackList<MyBinder>();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class MyBinder extends Binder implements IInterface {

        ICallback callback;

        MyService getService(){
            return MyService.this;
        }

        void registerCallback(ICallback callback){
            this.callback = callback;
            remoteCallbackList.register(MyBinder.this);
        }

        void unRegisterCallback(){
            remoteCallbackList.unregister(MyBinder.this);
        }

        @Override
        public IBinder asBinder() {
            return this;
        }
    }

    public LiveData<Boolean> getIsPaused2(){
        return mIsPaused2;
    }

    public void startTracking(){
        locationListener.setCurrentLocation(null);
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(mIsPaused){
                    pauseTracking();
//                    locationManager.removeUpdates(locationListener);
                    mHandler.removeCallbacks(this);
                }else{
                    try {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                1000, // minimum time interval between updates
                                (float) 0, // minimum distance between updates, in metres
                                locationListener);
                    } catch(SecurityException e) {
                        Log.d("mobile", e.toString());
                    }

                    // Logic for tracking.
                    if(distanceTravelled != locationListener.getDistanceTravelled()){
                        distanceTravelled = locationListener.getDistanceTravelled();
                    }else{
                        distanceTravelled = 0;
                    }
                    mTotalDistance += distanceTravelled;
                    endTime = System.nanoTime();

                    long convert = TimeUnit.SECONDS.convert((endTime-startTime) , TimeUnit.NANOSECONDS);

                    mTotalTime = mTotalTime+ 1;
                    mHandler.postDelayed(this,1000);
                }
                doCallbacks(mTotalTime,mTotalDistance,mIsPaused);
            }
        };
        mHandler.postDelayed(runnable,1000);
    }

    private void doCallbacks(long mTotalTime, int mTotalDistance,boolean mIsPaused){
        final int clients = remoteCallbackList.beginBroadcast();

        for (int i = 0 ; i < clients ; i++){
            remoteCallbackList.getBroadcastItem(i).callback.update(mTotalTime,mTotalDistance, mIsPaused);
        }
        remoteCallbackList.finishBroadcast();
    }

    public void pauseTracking(){
        mIsPaused = true;
        mIsPaused2.setValue(true);
        mHandler.removeCallbacksAndMessages(null);
    }

    public void unPausedTracking(){
        if(!isServiceRunningInForeground(getApplicationContext(),MyService.class)){
            createNotification();
        }

        startTime = System.nanoTime();
        mIsPaused = false;
        mIsPaused2.setValue(false);
        startTracking();
    }

    public void stopTracking(){
        mIsPaused = true;
        mIsPaused2.setValue(true);
        mTotalTime = 0;
        mTotalDistance = 0;
        distanceTravelled = 0;
        locationListener.setCurrentLocation(null);
        locationManager.removeUpdates(locationListener);
    }

    public Boolean getIsPaused(){
        return mIsPaused;
    }

    public long getTotalTime(){
        return mTotalTime;
    }

    public int getTotalDistance(){
        return mTotalDistance;
    }

    public void togglePlayPauseNotification(){
        if(mIsPaused){
            unPausedTracking();
        }else{
            pauseTracking();
        }
    }

    private void createNotification(){
        notificationManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "channel name";
            String description = "channel description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name,
                    importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager.createNotificationChannel(channel);
        }

        // To start recordActivity Activity from notification
        Intent mainActivityIntent = new Intent(MyService.this,RecordActivity.class);
        mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mainActivityIntent.putExtra("fromNotification",true);
//        mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, mainActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //Toggling start/pause button
        Intent actionIntent = new Intent(MyService.this,MyService.class);
        actionIntent.putExtra("fromNotification",true);
        actionIntent.putExtra("action","play/pause");
        PendingIntent pendingActionIntent = PendingIntent.getService(this, 0, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Building the notification
        mBuilder = new NotificationCompat.Builder(this,
                CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Running Tracker")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .addAction(R.drawable.ic_launcher_foreground,"start/pause",pendingActionIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        startForeground(NOTIFICATION_ID, mBuilder.build());
    }

//    Reference: https://stackoverflow.com/questions/6452466/how-to-determine-if-an-android-service-is-running-in-the-foreground
    public static boolean isServiceRunningInForeground(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                if (service.foreground) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        if(mIsPaused2.getValue()){
            Record record = repository.getLastRecord();
            record.setStatus("finish");
            record.setTime(mTotalTime);
            record.setDistance(mTotalDistance);
            stopTracking();
            repository.updateLastRecord(record);
            stopSelf();
        }
    }
}
