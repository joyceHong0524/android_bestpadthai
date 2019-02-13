package com.example.jung.bestpadthai;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.jung.bestpadthai.item.MemberInfoItem;
import com.example.jung.bestpadthai.lib.FileLib;
import com.example.jung.bestpadthai.lib.MyLog;
import com.example.jung.bestpadthai.lib.RemoteLib;
import com.example.jung.bestpadthai.lib.StringLib;
import com.example.jung.bestpadthai.remote.RemoteService;
import com.squareup.picasso.Picasso;

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

    //create Activity and compose screen
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_icon);

        context = this;

        memberInfoItem = ((MyApp) getApplication()).getMemberInfoItem();

        setToolbar();
        setView();
        setProfileIcon();

    }

    //sets toolbar
    private void setToolbar() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); // set support action bar
        final ActionBar actionBar = getSupportActionBar(); // make a instance of actionbar with the actionbar previously set

        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.profile_setting);
        }
    }

    //sets activity screen
    private void setView() {
        profileIconImage = (ImageView) findViewById(R.id.profile_icon);

        Button albumButton= (Button) findViewById(R.id.album);
        albumButton.setOnClickListener(this); //will attach onClick method later;

        Button cameraButton = (Button) findViewById(R.id.camera);
        cameraButton.setOnClickListener(this);
    }


    private void setProfileIcon() {
        MyLog.d(TAG, "onResume"+RemoteService.MEMBER_ICON_URL + memberInfoItem.memberIconFilename);

        if(StringLib.getInstance().isBlank(memberInfoItem.memberIconFilename)) {
            Picasso.with(this).load(R.drawable.ic_person).into(profileIconImage);
        } else {
            Picasso.with(this).load(RemoteService.MEMBER_ICON_URL + memberInfoItem.memberIconFilename)
                    .into(profileIconImage);
        }
    }


    private void setProfileIconFile() {
        profileIconFilename = memberInfoItem.seq + "_" + String.valueOf(System.currentTimeMillis()); // 이름 지정

        profileIconFile = FileLib.getInstance().getProfileIconFile(context,profileIconFilename); // 지정한 이름을 이용한 파일 객체를 반환함. 아직 파일이 들어있는 건 아님.
    }



}
