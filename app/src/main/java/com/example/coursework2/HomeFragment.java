package com.example.coursework2;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;


public class HomeFragment extends Fragment {
    private static final String TAG = "mobile";
    private MainActivityViewModel mViewModel;
    private List<Record> records;
    private TextView tv1,tv2,tv3,tv4,tv5;
    private int todayTotalDistance;
    private int yesterdayTotalDistance;
    private long todayTotalTime;
    private LocalDate today;
    private LocalDate yesterday ;
    private int lastDistanceCallback;
    private long lastTimeCallback;
    private int improvement;
    private long bestTimeToday;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = ((MainActivity)getActivity()).getViewModel();

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_home, container, false);

        today = LocalDate.now();
        yesterday = today.minus(Period.ofDays(1));


        tv1 = view.findViewById(R.id.textView12);
        tv2 = view.findViewById(R.id.textView13);
        tv3 = view.findViewById(R.id.textView14);
        tv4 = view.findViewById(R.id.description);
        tv5 = view.findViewById(R.id.description2);

        mViewModel.getDistance().observe(((MainActivity)getActivity()), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                String finalDistance = String.valueOf(todayTotalDistance+integer-lastDistanceCallback);
                tv2.setText(finalDistance+" metres");
                improvement = Math.max(Integer.parseInt(finalDistance) - yesterdayTotalDistance, 0);
                tv4.setText("You have improved "+ String.valueOf(improvement) +" metres from yesterday");
            }
        });
        mViewModel.getTime().observe(((MainActivity) getActivity()), new Observer<Long>() {
            @Override
            public void onChanged(Long aLong) {
                String finalTime = String.valueOf(todayTotalTime+ aLong-lastTimeCallback);
                String minutes = String.valueOf((todayTotalTime+ aLong-lastTimeCallback)/60);
                String seconds = String.format("%02d",(todayTotalTime+ aLong-lastTimeCallback)%60);
                tv3.setText(minutes+":"+seconds);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        todayTotalDistance = mViewModel.getTotalDistanceByDate(String.valueOf(today));
        yesterdayTotalDistance = mViewModel.getTotalDistanceByDate(String.valueOf(yesterday));
        todayTotalTime = mViewModel.getTotalTimeByDate(String.valueOf(today));


        if(mViewModel.getBinder().getValue() != null) {
            lastDistanceCallback = mViewModel.getBinder().getValue().getService().getTotalDistance();
            lastTimeCallback = mViewModel.getBinder().getValue().getService().getTotalTime();
            todayTotalDistance += lastDistanceCallback;
            todayTotalTime +=  lastTimeCallback;
        }

        tv1.setText(String.valueOf(yesterdayTotalDistance)+" metres");
        tv2.setText(String.valueOf(todayTotalDistance)+" metres");
        String minutes = String.valueOf((todayTotalTime)/60);
        String seconds = String.format("%02d",(todayTotalTime)%60);
        tv3.setText(minutes+":"+seconds);
        improvement = Math.max(todayTotalDistance - yesterdayTotalDistance, 0);
        tv4.setText("You have improved "+ String.valueOf(improvement) +" metres from yesterday");

        try {
            bestTimeToday = mViewModel.getRecordByMaxTime(String.valueOf(today)).getTime();
            tv5.setText("Your longest running time today is: "+String.valueOf(bestTimeToday/60)+" minutes and "+ String.valueOf(bestTimeToday%60) +" seconds");

        } catch (Exception e) {
            tv5.setText("Your longest running time today is: 0 seconds");
            e.printStackTrace();
        }

    }
}