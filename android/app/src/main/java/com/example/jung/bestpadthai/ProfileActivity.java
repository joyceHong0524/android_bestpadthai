package com.example.jung.bestpadthai;

// 1. If there is a profile already exists, show the information and allows to edit.
// 2. If there is a no profile, let them fill the blank box.
// 3. Should go to profile setting activity if they clicks the camera button.

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import android.view.Menu;
import android.view.MenuItem;

import com.example.jung.bestpadthai.item.MemberInfoItem;
import com.example.jung.bestpadthai.MyApp;
import com.example.jung.bestpadthai.lib.EtcLib;
import com.example.jung.bestpadthai.lib.MyLog;
import com.example.jung.bestpadthai.lib.MyToast;
import com.example.jung.bestpadthai.lib.StringLib;
import com.example.jung.bestpadthai.remote.RemoteService;
import com.example.jung.bestpadthai.remote.ServiceGenerator;
import com.squareup.picasso.Picasso;
import com.example.jung.bestpadthai.lib.MyToast;
import java.util.Calendar;
import java.util.GregorianCalendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = this.getClass().getSimpleName();
    Context context;

    ImageView profileIconImage;
    ImageView profileIconChangeImage;
    EditText nameEdit;
    EditText sextypeEdit;
    EditText birthEdit;
    EditText phoneEdit;

    MemberInfoItem currentItem;

    // Create the activity and compose the elements in it.
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        context = this;

        currentItem = ((MyApp) getApplication()).getMemberInfoItem();

        setToolbar();
        setView();

    }

    protected void onResume() {
        super.onResume();

        MyLog.d(TAG,RemoteService.MEMBER_ICON_URL+currentItem.memberIconFilename);

        if(StringLib.getInstance().isBlank(currentItem.memberIconFilename)){
            Picasso.with(this).load(R.drawable.ic_person).into(profileIconImage);
        }else {
            Picasso.with(this).load(RemoteService.MEMBER_ICON_URL+currentItem.memberIconFilename).into(profileIconImage);
        }
    }

    //Set the activity toolbar

    private void setToolbar() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null ){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.profile_setting);
        }
    }

    //set activity view

    private void setView() {
        profileIconImage = (ImageView) findViewById(R.id.profile_icon);
        profileIconImage.setOnClickListener(this); //you can see onClick method below.

        profileIconChangeImage = (ImageView) findViewById(R.id.profile_icon_change);
        profileIconChangeImage.setOnClickListener(this);

        nameEdit = (EditText) findViewById(R.id.profile_name);
        nameEdit.setText(currentItem.name);

        sextypeEdit = (EditText) findViewById(R.id.profile_sextype);
        sextypeEdit.setText(currentItem.sextype);
        sextypeEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSexTypeDialog();
            }
        });

        birthEdit = (EditText) findViewById(R.id.profile_birth);
        birthEdit.setText(currentItem.birthday);
        birthEdit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                setBirthdayDialog();
            }
        });

        String phoneNumber = EtcLib.getInstance().getPhoneNumber(context);

        phoneEdit = (EditText) findViewById(R.id.profile_phone);
        phoneEdit.setText(phoneNumber);

        TextView phoneStateEdit = (TextView) findViewById(R.id.phone_state);
        if(phoneNumber.startsWith("0")){
            phoneStateEdit.setText("("+getResources().getString(R.string.device_number)+")");
        }else {
            phoneStateEdit.setText("("+getResources().getString(R.string.phone_number)+")");        }

    }

    //shows a dialog for selecting sextype.
    private void setSexTypeDialog() {
        final String[] sexTypes = new String[2];
        sexTypes[0] = getResources().getString(R.string.sex_man);
        sexTypes[1] = getResources().getString(R.string.sex_woman);

        new AlertDialog.Builder(this)
                .setItems(sexTypes, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        if (which >= 0){
                            sextypeEdit.setText(sexTypes[which]);
                        }
                        dialog.dismiss();

                    }
                }).show();
    }

    //shows a dialog for selecting birthday.
    private void setBirthdayDialog() {
        GregorianCalendar calendar = new GregorianCalendar();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                String myMonth;
                if (month + 1 < 10) {
                    myMonth = "0" + (month + 1);
                } else {
                    myMonth = "" + month;
                }

                String myDay;
                if (dayOfMonth < 10) {
                    myDay = "0" + dayOfMonth;
                } else {
                    myDay = "" + dayOfMonth;
                }

                String date = year + " " + myMonth + " " + myDay;
                birthEdit.setText(date);
            }
        }, year, month, day).show();
    }

        public boolean onCreateOptionsMenu(Menu menu) {
            getMenuInflater().inflate(R.menu.menu_submit,menu);
            return true;
        }

        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()){
                case android.R.id.home:
                    close();
                    break;

                case R.id.action_submit:
                    save();
                    break;

            }

            return true;
        }


        //get information of member from form.
        private MemberInfoItem getMemberInfoItem() {
            MemberInfoItem item = new MemberInfoItem();

            item.phone = EtcLib.getInstance().getPhoneNumber(context);
            item.name = nameEdit.getText().toString();
            item.sextype = sextypeEdit.getText().toString();
            item.birthday = birthEdit.getText().toString().replace(" ","");

            return item;

        }

        //compares to currentItem with newItem if there's any change.
        private boolean isChanged(MemberInfoItem newItem){

        if (newItem.name.trim().equals(currentItem.name) &&
                newItem.sextype.trim().equals(currentItem.sextype) &&
                newItem.birthday.trim().equals(currentItem.birthday)){
            Log.d(TAG,"return"+false);
            return false;
        } else {
            return true;
            }
        }

        //checks there the name is blank or not
        private boolean isNoName(MemberInfoItem newItem){
            if(StringLib.getInstance().isBlank(newItem.name)){
                return true;
            } else {
                return false;
            }
        }

        //checks any differences between the old item and the new item
        //if there is any change, save it
        //if there is no change, close it.

        private void close() {
            MemberInfoItem newItem = getMemberInfoItem();
            
            if(!isChanged(newItem)&&!isNoName(newItem)){
                finish();
            } else if (isNoName(newItem)){
                MyToast.s(context,R.string.name_need);
                finish();
            } else {
                new AlertDialog.Builder(this).setTitle(R.string.change_save)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                save();
                            }
                        }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).show();
            }

        }

        //saves user's information.

        private void save() {
            final MemberInfoItem newItem = getMemberInfoItem();

            if(!isChanged(newItem)){
                MyToast.s(this,R.string.no_change);
                finish();
                return;
            }

            MyLog.d(TAG,"insertItem " + newItem.toString());

            RemoteService remoteService = ServiceGenerator.createService(RemoteService.class);

            Call<String> call = remoteService.insertMemberInfo(newItem);
            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if(response.isSuccessful()){
                        String seq = response.body();
                        try{
                            currentItem.seq = Integer.parseInt(seq);
                            if(currentItem.seq == 0){
                                MyToast.s(context,R.string.member_insert_fail_message);
                                return;
                            }
                        } catch (Exception e) {
                            MyToast.s(context,R.string.member_insert_fail_message);
                            return;
                        }
                        currentItem.name = newItem.name;
                        currentItem.sextype = newItem.sextype;
                        currentItem.birthday = newItem.birthday;
                        finish();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {

                }
            });
        }

        //if back button pressed, call close()
        @Override
        public void onBackPressed(){
            close();
        }

        //프로필 아이콘이나 프로필 아이콘 변경 뷰를 클릭했을 때 프로필 아이콘을 변경할 수 있도록
        //startProfileIconChange() 메소드를 불러온다.
        @Override
        public void onClick(View v){
            if(v.getId() == R.id.profile_icon || v.getId() == R.id.profile_icon_change){
                startProfileIconChange();
            }
        }

        //activate profileIconActivity to change profile picture
        private void startProfileIconChange() {
          Intent intent = new Intent(this,ProfileIconActivity.class);
          startActivity(intent);
        }


}
