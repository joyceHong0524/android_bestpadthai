package com.example.jung.bestpadthai;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;


import com.example.jung.bestpadthai.item.FoodInfoItem;
import com.example.jung.bestpadthai.item.GeoItem;
import com.example.jung.bestpadthai.lib.GoLib;
import com.example.jung.bestpadthai.lib.MyLog;


public class BestPadthaiRegisterActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();
    public static FoodInfoItem currentItem = null;

    Context context;

    //sets basic information to run BestPadthaiLocationFragment
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bestpadthai_register);

        context = this;

        int memberSeq = ((MyApp) getApplication()).getMemberSeq();

        //saves basic infos for passing down to BestPadthaiRegisterLocationFragment
        FoodInfoItem infoItem = new FoodInfoItem();

        infoItem.memberSeq = memberSeq;
        infoItem.latitude = GeoItem.getKnownLocation().latitude;
        infoItem.longitude = GeoItem.getKnownLocation().longitude;

        MyLog.d(TAG,"infoItem "+ infoItem.toString());

        setToolbar();

//        //shows BestPadthaiRegisterLocationFragment
        GoLib.getInstance().goFragment(getSupportFragmentManager(),R.id.content_main, BestPadthaiRegisterLocationFragment.newInstance(infoItem));
    }

    private void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.bestfood_register);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_close, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
            case R.id.action_close:
                finish();
                break;
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, data); //액티비티에서 먼저 받은 것을 프래그먼트로 넘겨준다고 생각하면 됨.
        }
    }
}
