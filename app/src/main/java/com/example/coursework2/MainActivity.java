package com.example.coursework2;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.List;

public class MainActivity extends AppCompatActivity{

    private static final String TAG = "mobile";
    private static final int MY_PERMISSIONS_REQUEST_PROCESS_CALLS = 1;
    public int MY_PERMISSIONS_REQUEST_RESULT = 0;
    private SectionsStatePagerAdapter sectionsStatePagerAdapter;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private String[] TabTitles = new String[]{"Home","Activity"};
    private MainActivityViewModel mViewModel;
//    private RecordActivityViewModel recordActivityViewModel;
    MyService mService;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager2);
        sectionsStatePagerAdapter = new SectionsStatePagerAdapter(this);
        setupViewPager(viewPager);

        new TabLayoutMediator(tabLayout,viewPager,(tab, position) -> tab.setText(TabTitles[position])).attach();

        mViewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication())).get(MainActivityViewModel.class);

//        recordActivityViewModel = new ViewModelProvider(this,
//                ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication())).get(RecordActivityViewModel.class);

        mViewModel.getBinder().observe(this, new Observer<MyService.MyBinder>() {
            @Override
            public void onChanged(MyService.MyBinder myBinder) {
                if(myBinder != null){
                    mService = myBinder.getService();
                }else{
                    mService = null;
                }
            }
        });
    }

    public MainActivityViewModel getViewModel(){
        return mViewModel;
    }

    public MyService getService(){
        return mService;
    }

    private void setupViewPager(ViewPager2 viewPager){
        sectionsStatePagerAdapter.addFragment(new HomeFragment(), TabTitles[0]);
        sectionsStatePagerAdapter.addFragment(new ListFragment(), TabTitles[1]);
        viewPager.setAdapter(sectionsStatePagerAdapter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mService != null){ //recordViewModel.getBinder() != null
            unbindService(mViewModel.getServiceConnection());
        }
    }

    @Override
    protected void onResume() {
        bindService();
        super.onResume();
    }

//    Reference: https://developer.android.com/guide/topics/ui/dialogs
    public void confirmAlertBox(Record record){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Do you want to finish previous running activity?");
        builder.setMessage("You currently have an active running activity. By clicking 'OK', previous activity will be marked as finish and new a running activity will be created.");
        // Set up the buttons
        builder.setPositiveButton("Enter", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Logic to insert record to database
                if(isMyServiceRunning(MyService.class)){
                    Intent intent = new Intent(MainActivity.this, MyService.class);
                    stopService(intent);
                }
                if(mService != null){
                    record.setTime(mService.getTotalTime());
                    record.setDistance(mService.getTotalDistance());
                }

                record.setStatus("finish");
                mService.stopTracking();
//                recordActivityViewModel.setIsTracking(false);
                mViewModel.updateLastRecord(record);
                createRecordAlertBox();
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

    //    Reference: https://stackoverflow.com/questions/10903754/input-text-dialog-android
    public void createRecordAlertBox(){
        final String[] m_Text = {""};
        Intent intent = new Intent(this, RecordActivity.class);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Record Name: ");

        // Set up the input
        final EditText input = new EditText(this);

        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Enter", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                m_Text[0] = input.getText().toString();
                if(m_Text[0].length()>0){
                    intent.putExtra("name",m_Text[0]);
//                    mViewModel.insertRecord(new Record(m_Text[0]));
                    startActivity(intent);
                }

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

//    Reference https://stackoverflow.com/questions/600207/how-to-check-if-a-service-is-running-on-android
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void startService(){
        Intent serviceIntent = new Intent(this, MyService.class);
        startService(serviceIntent);
        bindService();
    }

    private void bindService(){
        Intent serviceIntent = new Intent(this,MyService.class);
        bindService(serviceIntent, mViewModel.getServiceConnection() , Context.BIND_AUTO_CREATE);
    }

//    Reference https://moodle.nottingham.ac.uk/pluginfile.php/7484803/mod_resource/content/0/comp3018_lab06.pdf
    public void attemptRegisterBroadcast(){
        // permission is already granted
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            MY_PERMISSIONS_REQUEST_RESULT = 1;
        }else if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.ACCESS_FINE_LOCATION)){
            Log.d("g53mdp", "explanation required");
            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
            alertDialog.setTitle("Alert");
            alertDialog.setMessage("explanation - this permission is required for this app to function");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", (dialog, which) ->
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            MY_PERMISSIONS_REQUEST_PROCESS_CALLS
                    ));
            alertDialog.show();
        }else{
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},MY_PERMISSIONS_REQUEST_PROCESS_CALLS);
        }
    }
    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_PROCESS_CALLS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    MY_PERMISSIONS_REQUEST_RESULT = 1;
                } else {
                    Log.d("mobile", "permission denied");
                }
                return;
            }
        }
    }
}

