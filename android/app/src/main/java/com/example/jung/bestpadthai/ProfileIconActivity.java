package com.example.jung.bestpadthai;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.example.jung.bestpadthai.item.MemberInfoItem;

import java.io.File;

public class ProfileIconActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = getClass().getSimpleName();

    private static final int PICK_FROM_CAMERA = 0;
    private static final int PICK_FROM_ALBUM = 1;
    private static final int CROP_FROM_CAMERA = 2;
    private static final int CROP_FROM_ALBUM = 3;

    Context context;

    ImageView profileIconImage;

    MemberInfoItem memberInfoItem;

    File profileIconFile;
    String profileIconFilename;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_icon);

        context = this;

        memberInfoItem = ((MyApp) getApplication()).getMemberInfoItem();

        setToolbar();
        setView();
        setProfileIcon();
    }

    //sets Toolbar

    private void setToolbar(){
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();

        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.profile_setting);
        }
    }

}
