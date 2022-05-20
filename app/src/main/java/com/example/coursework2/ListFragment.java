package com.example.coursework2;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class ListFragment extends Fragment implements RecyclerViewAdapter.onItemClickListener{
    private static final String TAG = "mobile";
    private MainActivityViewModel mViewModel;
    private RecyclerView recyclerView;
    private FloatingActionButton bt1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = ((MainActivity)getActivity()).getViewModel();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.fragment_list, container, false);

        bt1 = view.findViewById(R.id.floatingActionButton3);

        recyclerView = view.findViewById(R.id.recycle_view);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(((MainActivity)getActivity()) , null,this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(((MainActivity)getActivity())));
        recyclerView.addItemDecoration(new DividerItemDecoration(((MainActivity)getActivity()).getBaseContext()
                ,DividerItemDecoration.VERTICAL));

        mViewModel.getRecords().observe(((MainActivity)getActivity()), new Observer<List<Record>>() {
            @Override
            public void onChanged(List<Record> records) {
                if(records != null){
                    adapter.setData(records);
                    adapter.notifyItemInserted(records.size());
                }
            }
        });

        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(((MainActivity)getActivity()).MY_PERMISSIONS_REQUEST_RESULT == 0){
                    ((MainActivity)getActivity()).attemptRegisterBroadcast();
                }else{
                    Record lastRecord = mViewModel.getLastRecord();
                    if(lastRecord != null){
                        String lastRecordStatus = lastRecord.getStatus();

                        if(lastRecordStatus.equals("finish")){
                            ((MainActivity)getActivity()).createRecordAlertBox();
                        }else{
                            ((MainActivity)getActivity()).confirmAlertBox(lastRecord);
                        }
                    }else{
                        ((MainActivity)getActivity()).createRecordAlertBox();
                    }
                }



            }
        });
        return view;
    }

    @Override
    public void onItemClick(int position) {
        Record record =  mViewModel.getRecords().getValue().get(position);
        String recordStatus = record.getStatus();
        int recordId = record.getId();
        Intent intent;
        if(recordStatus.equals("ongoing")){
            intent = new Intent(((MainActivity) getActivity()), RecordActivity.class);
        }else{
            intent = new Intent(((MainActivity) getActivity()), CompletedRecord.class);
        }
        intent.putExtra("id",recordId);
        startActivity(intent);
    }
}