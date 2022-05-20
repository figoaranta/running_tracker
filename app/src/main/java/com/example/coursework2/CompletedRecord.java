package com.example.coursework2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.core.app.TaskStackBuilder;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class CompletedRecord extends AppCompatActivity {

    public final static String TAG = "mobile";
    private CompletedRecordViewModel completedRecordViewModel;
    private TextView tv1,tv2,tv3,tv4,tv5;
    private ImageView imageView;
    private Button bt;
    private static final int IMAGE_PICK_CODE = 1000;
    private static final int PERMISSION_CODE = 1001;
    private Record record;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completed_record);

        bt = findViewById(R.id.button5);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tv1 = findViewById(R.id.textView7);
        tv2 = findViewById(R.id.textView8);
        tv3 = findViewById(R.id.Date3);
        tv4 = findViewById(R.id.textView16);
        tv5 = findViewById(R.id.textView17);

        imageView = findViewById(R.id.imageView2);

        Bundle bundle = getIntent().getExtras();
        int recordId = bundle.getInt("id");

        completedRecordViewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication())).get(CompletedRecordViewModel.class);

        record =  completedRecordViewModel.getRecord(recordId);
        long totalTime =record.getTime();
        int totalDistance = record.getDistance();

        String minutes = String.valueOf((totalTime)/60);
        String seconds = String.format("%02d",(totalTime)%60);
        tv1.setText(minutes+":"+seconds);
        tv2.setText(String.valueOf(totalDistance)+" metres");
        tv3.setText(record.getDate());
        tv4.setText(record.getName());

        setNote();

        byte[] bitmap = record.getImage();
        if(bitmap != null){
            Bitmap image = BitmapFactory.decodeByteArray(bitmap, 0 , bitmap.length);
            imageView.setImageBitmap(image);
        }

        completedRecordViewModel.getHasImage().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean!=null){
                    if(aBoolean){
                        bt.setVisibility(View.VISIBLE);
                    }else{
                        bt.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    public void setNote(){
        if(record.getNote()== null || record.getNote().equals("")){
            tv5.setText("Add note here.");
        }else{
            tv5.setText(record.getNote());
        }
    }

    public void onButtonClick(View v){
        if(isTaskRoot()){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        finish();
    }

    public void onClickButton2(View v){
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

    public void onClickButton3(View v){
        record.setImage(null);
        imageView.setImageResource(R.mipmap.ic_launcher_round);

        imageView.getLayoutParams().height = 263;
        imageView.getLayoutParams().width = 263;
        completedRecordViewModel.updateRecord(record);
        completedRecordViewModel.setHasImage(false);
    }

    public void pickImageFromGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Image Picker"),IMAGE_PICK_CODE);
    }

    public void onClickButton4(View v){
        confirmAlertBox();
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
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG,100,byteArrayOutputStream);
                byte[] img = byteArrayOutputStream.toByteArray();
                record.setImage(img);

                completedRecordViewModel.updateRecord(record);
                completedRecordViewModel.setHasImage(true);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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

    //Reference: https://developer.android.com/guide/topics/ui/dialogs
    public void confirmAlertBox(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Do you really want to delete this running record?");
        builder.setMessage("Once it is deleted, record can no longer be retrieved.");
        // Set up the buttons
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int id = record.getId();
                completedRecordViewModel.deleteRecord(id);
                finish();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public void onClickButton5(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add a special note for this record.");
        builder.setMessage("Max 20 Characters");

        final EditText input = new EditText(this);
//        Reference: https://stackoverflow.com/questions/15593390/how-do-i-limit-the-number-of-characters-entered-in-an-alert-dialog-edittext/15593424
        InputFilter[] FilterArray = new InputFilter[1];
        FilterArray[0] = new InputFilter.LengthFilter(20);
        input.setFilters(FilterArray);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);

        if(record.getNote()!= null){
            input.setText(record.getNote());
        }

        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (input.getText().toString().length() == 0){
                    record.setNote(null);
                    completedRecordViewModel.updateRecord(record);
                    setNote();
                }else{
                    for (int i = 0; i < input.getText().toString().length(); i++) {
                        if (Character.isLetterOrDigit(input.getText().toString().charAt(i))){
                            record.setNote(input.getText().toString());
                            completedRecordViewModel.updateRecord(record);
                            setNote();
                            break;
                        }
                        if(i == input.getText().toString().length()-1){
                            record.setNote(null);
                            completedRecordViewModel.updateRecord(record);
                            setNote();
                        }
                    }
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


}