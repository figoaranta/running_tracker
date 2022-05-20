package com.example.coursework2;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.core.app.TaskStackBuilder;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.coursework2.databinding.ActivityRecordBinding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class RecordActivity extends AppCompatActivity {
    private MyService mService;
    private RecordActivityViewModel recordViewModel;
    public final static String TAG = "mobile";
    private TextView tv1,tv2,tv3,tv4;
    private Handler mHandler;
    private Button button1,button2;
    private ImageView imageView;

    private static final int IMAGE_PICK_CODE = 1000;
    private static final int PERMISSION_CODE = 1001;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);

        ActivityRecordBinding activityRecordBinding = DataBindingUtil.setContentView(this,R.layout.activity_record);
        activityRecordBinding.setLifecycleOwner(this);

        imageView =  findViewById(R.id.myImageView);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tv1 = findViewById(R.id.textView3);
        tv2 = findViewById(R.id.textView4);
        tv3 = findViewById(R.id.Date2);
        tv4 = findViewById(R.id.textView15);

        button1 = findViewById(R.id.button);
        button2 = findViewById(R.id.button4);

        recordViewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication())).get(RecordActivityViewModel.class);

        activityRecordBinding.setRecordViewModel(recordViewModel);

        Bundle bundle = getIntent().getExtras();
        if(bundle.getBoolean("fromNotification")){
            displayRecordFromNotification();
        }else{
            autoInsertRecord(bundle);
            displayExistingRecord(bundle);
        }
        


        mHandler = new Handler();
        recordViewModel.getBinder().observe(this, new Observer<MyService.MyBinder>() {
            @Override
            public void onChanged(MyService.MyBinder myBinder) {
                if(myBinder != null){
                    mService = myBinder.getService();
                    if(mService.getIsPaused()){
                        if(mService.getTotalTime() == 0){
                            mService.unPausedTracking();
                            recordViewModel.setIsTracking(true);
                        }else{
                            tv1.setText(String.valueOf(mService.getTotalDistance())+" metres");

                            tv2.setText(String.valueOf(mService.getTotalTime()/60)+":"+String.valueOf(mService.getTotalTime()%60));
                        }
                    }else{
                        recordViewModel.setIsTracking(false);
                    }
                }else{
                    mService = null;
                }
            }
        });

        recordViewModel.getTime().observe(this, new Observer<Long>() {
            @Override
            public void onChanged(Long aLong) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        tv2.setText(String.valueOf(aLong/60)+":"+String.format("%02d",aLong%60));
                        button1.setText("Pause");
                    }
                });
            }
        });

        recordViewModel.getDistance().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        tv1.setText(String.valueOf(integer)+" metres");
                    }
                });
            }
        });

//        if(recordViewModel.getLastRecord() != null){
        recordViewModel.getImage().observe(this, new Observer<byte[]>() {
            @Override
            public void onChanged(byte[] bytes) {
                if(bytes!=null){
                    Bitmap image = BitmapFactory.decodeByteArray(bytes, 0 , bytes.length);
                    imageView.setImageBitmap(image);
                    button2.setVisibility(View.VISIBLE);
                }else{
                    button2.setVisibility(View.GONE);
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void autoInsertRecord(Bundle bundle){
        try {
            String name = bundle.getString("name");
            if(name!= null){
                Record record = new Record(name);
                tv3.setText(record.getDate());
                tv4.setText(record.getName());
                recordViewModel.insertRecord(record);
                recordViewModel.setImage(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void displayExistingRecord(Bundle bundle){
        try {
            int id = bundle.getInt("id");
            Log.d(TAG, "displayExistingRecord: "+id);
            Record record = recordViewModel.getRecord(id);
            tv3.setText(record.getDate());
            tv4.setText(record.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void displayRecordFromNotification(){
        Record record = recordViewModel.getLastRecord();
        tv3.setText(record.getDate());
        tv4.setText(record.getName());
    }

    public void onClickButton1(View v){
        toggleUpdate();
    }

    public void toggleUpdate(){
        if(mService!=null){
            if(mService.getIsPaused()){
                mService.unPausedTracking();
                recordViewModel.setIsTracking(true);
            }else{
                mService.pauseTracking();
                recordViewModel.setIsTracking(false);
            }
        }
    }

    public void onClickButton2(View v){
        confirmAlertBox();
    }

//    @Override
//    protected void onNewIntent(Intent intent) {
//        try {
//            Log.d(TAG, "onNewIntent: ");
//            Bundle bundle = intent.getExtras();
//            super.onNewIntent(intent);
//            if(bundle.getBoolean("fromNotification")){
//                displayRecordFromNotification();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public void onClickButton3(View v){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED){
                String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                requestPermissions(permissions,PERMISSION_CODE);
            }else{
                pickImageFromGallery();
            }
        }else{
            pickImageFromGallery();
        }
    }

    public void onClickButton4(View v){
        Record record = recordViewModel.getLastRecord();
        record.setImage(null);
        imageView.setImageResource(R.mipmap.ic_launcher_round);

        imageView.getLayoutParams().height = 263;
        imageView.getLayoutParams().width = 263;
        recordViewModel.updateLastRecord(record);

        recordViewModel.setImage(null);
    }

//    Reference: https://www.youtube.com/watch?v=O6dWwoULFI8
    public void pickImageFromGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Title"),IMAGE_PICK_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case PERMISSION_CODE:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    pickImageFromGallery();
                }else{
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK && requestCode == IMAGE_PICK_CODE){
            try {
                Record record = recordViewModel.getLastRecord();

                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG,100,byteArrayOutputStream);
                byte[] img = byteArrayOutputStream.toByteArray();
                record.setImage(img);

                recordViewModel.updateLastRecord(record);
                recordViewModel.setImage(img);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mService != null){ //recordViewModel.getBinder() != null
            unbindService(recordViewModel.getServiceConnection());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(isTaskRoot() && recordViewModel.getLastRecord().getStatus().equals("finish")){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }else{
            startService();
            initiateTracking();
        }

    }

    public void initiateTracking(){
        if(mService!=null){
            if(mService.getIsPaused()){
                if(recordViewModel.getTime().getValue()  == 0){
                    Log.d(TAG, "initiateTracking: ");
                    mService.unPausedTracking();
                    recordViewModel.setIsTracking(true);
                }
            }
        }
    }

    private void startService(){
        Intent serviceIntent = new Intent(this, MyService.class);
        startService(serviceIntent);
        bindService();
    }

    private void bindService(){
        Intent serviceIntent = new Intent(this,MyService.class);
        bindService(serviceIntent, recordViewModel.getServiceConnection() , Context.BIND_AUTO_CREATE);
    }

//    Reference: https://stackoverflow.com/questions/600207/how-to-check-if-a-service-is-running-on-android
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    //Reference: https://developer.android.com/guide/topics/ui/dialogs
    public void confirmAlertBox(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Do you want to finish running activity?");
        builder.setMessage("By clicking 'OK', this activity will be marked as finish.");
        // Set up the buttons
        builder.setPositiveButton("Enter", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Record record = recordViewModel.getLastRecord();
                int recordId = record.getId();
                if(isMyServiceRunning(MyService.class)){
                    Intent intent = new Intent(RecordActivity.this, MyService.class);
                    stopService(intent);
                }
                if(mService != null){
                    record.setTime(mService.getTotalTime());
                    record.setDistance(mService.getTotalDistance());
                }

                record.setStatus("finish");
                mService.stopTracking();
                recordViewModel.setIsTracking(false);
                recordViewModel.updateLastRecord(record);

                Intent intent = new Intent(RecordActivity.this, CompletedRecord.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.putExtra("id",recordId);
                startActivity(intent);
                mService.stopSelf();
                finish();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    // Reference https://stackoverflow.com/questions/35057188/how-to-add-back-button-arrow-functionality-in-navigation-bar
    public boolean onOptionsItemSelected(MenuItem item){
        Intent upIntent = NavUtils.getParentActivityIntent(this);
        if (NavUtils.shouldUpRecreateTask(this, upIntent) || isTaskRoot()) {
            // This activity is NOT part of this app's task, so create a new task
            // when navigating up, with a synthesized back stack.
            TaskStackBuilder.create(this)
                    // Add all of this activity's parents to the back stack
                    .addNextIntentWithParentStack(upIntent)
                    // Navigate up to the closest parent
                    .startActivities();
        } else {
            // This activity is part of this app's task, so simply
            // navigate up to the logical parent activity.
            NavUtils.navigateUpTo(this, upIntent);
        }
        return true;
    }
}