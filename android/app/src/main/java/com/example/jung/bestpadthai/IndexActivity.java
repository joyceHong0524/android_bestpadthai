package com.example.jung.bestpadthai;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.jung.bestpadthai.item.MemberInfoItem;
import com.example.jung.bestpadthai.lib.*;
import com.example.jung.bestpadthai.remote.RemoteService;
import com.example.jung.bestpadthai.remote.ServiceGenerator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IndexActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();

    Context context;

    //Set the layout and check if it is connected to internet. If it is not connected call showNoService();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);

        context = this;

        if (!RemoteLib.getInstance().isConnected(context)) {
            showNoService();
            return;
        }
    }

    //Search informations of user by calling startTask() after 1.2 seconds later
    protected void onStart(){
        super.onStart();

        Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run(){
                startTask();
            }
        },1200);
    }

    private void showNoService() {
        TextView messageText = (TextView) findViewById(R.id.massage);
        messageText.setVisibility(View.VISIBLE);

        Button closeButton = (Button) findViewById(R.id.close);
        closeButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                finish();
            }
        });
        closeButton.setVisibility(View.VISIBLE);
    }

    public void startTask(){
        String phone = EtcLib.getInstance().getPhoneNumber(this);

        selectMemberInfo(phone);
        GeoLib.getInstance().setLastKnownLocation(this);
    }

    public void selectMemberInfo(String phone){
        RemoteService remoteService = ServiceGenerator.createService(RemoteService.class);

        Call<MemberInfoItem> call = remoteService.selectMemberInfo(phone);
        /**
         * Asynchronously send the request and notify {@code callback} of its response or if an error
         * occurred talking to the server, creating the request, or processing the response.
         */
        call.enqueue(new Callback<MemberInfoItem>() {
            @Override
            public void onResponse(Call<MemberInfoItem> call, Response<MemberInfoItem> response) {
                MemberInfoItem item = response.body();

                if(response.isSuccessful() && !StringLib.getInstance().isBlank(item.name)){
                    MyLog.d(TAG,"success " + response.body().toString());
                    setMemberInfoItem(item);
                }else{
                    MyLog.d(TAG,"not success");
                    goProfileActivity(item);
                }
            }


            @Override
            public void onFailure(Call<MemberInfoItem> call, Throwable t) {
                MyLog.d(TAG,"no internet connectivity");
                MyLog.d(TAG,t.toString());
            }

        });
    }

    private void setMemberInfoItem(MemberInfoItem item){

        ((MyApp) getApplicationContext()).setMemberInfoItem(item);

        startMain();
    }

    // Run MainActivity, exit current activity.

    public void startMain() {
        Intent intent = new Intent(IndexActivity.this, MainActivity.class);
        startActivity(intent);

        finish();
    }

    // if it fails to retrieve user information from server run insertMemberPhone()

    private void goProfileActivity(MemberInfoItem item) {
        if (item == null || item.seq <= 0) {
            insertMemberPhone();
        }

        Intent intent = new Intent(IndexActivity.this, MainActivity.class);
        startActivity(intent);

        Intent intent2 = new Intent(IndexActivity.this, MainActivity.class);
        startActivity(intent2);

        finish(); //finish this activity
    }
}



