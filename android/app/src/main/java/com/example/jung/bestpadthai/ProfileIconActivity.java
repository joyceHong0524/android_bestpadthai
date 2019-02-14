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

    public void onClick(View v) {

        setProfileIconFile();

        if (v.getId() == R.id.album ){
            getImageFromAlbum();
        }
        else if (v.getId() == R.id.camera) {
            getImageFromCamera();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_close, menu);
        return true;
    }

    //왼쪽 상단의 android.R.id.home을 선택했을 때와
    //오른쪽 상단의 닫기 메뉴를 클릭했을 때의 동작을 지정한다.
    //여기서는 모든 클릭이 액티비티를 종료한다.
    //메뉴를 처리시 true, 못 했을 시에는 false를 return
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_close:
                finish();
                break;
        }

        return true;

    }

    //takes picture using camera app
    private void getImageFromCamera(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(profileIconFile));
        startActivityForResult(intent, PICK_FROM_CAMERA);

    }

    //get picture using album app
    private void getImageFromAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }

    //return intent to crop the given image
    private Intent getCropIntent(Uri inputUri, Uri outputUri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(inputUri,"image/*");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY",1);
        intent.putExtra("outputX",200);
        intent.putExtra("outputY",200);
        intent.putExtra("scale",true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());

        return intent;

    }


    private void cropImageFromCamera() {
        Uri uri = Uri.fromFile(profileIconFile); //현재 profileIconFile 에서 주소를 가져온다
        Intent intent = getCropIntent(uri, uri); //input uri, output uri 이런 거 없고 그냥 덮어쓴다 이거임
        startActivityForResult(intent, CROP_FROM_CAMERA);
    }

    private void cropImageFromAlbum(Uri inputUri) {
        Uri outputUri = Uri.fromFile(profileIconFile);

        MyLog.d(TAG, "startPickFromAlbum uri"+ inputUri.toString());
        Intent intent = getCropIntent(inputUri, outputUri);
        startActivityForResult(intent,CROP_FROM_ALBUM);

    }




    //IMPORTANT
    //manages activities called by startActivityForResult()

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
        MyLog.d(TAG, "onActivityResult "+ intent);

        if(resultCode != RESULT_OK) return;

        if(requestCode == PICK_FROM_CAMERA) {
            cropImageFromCamera();
        } else if (requestCode == CROP_FROM_CAMERA){
            Picasso.with(this).load(profileIconFile).into(profileIconImage);
            uploadProfileIcon();
        } else if (requestCode == PICK_FROM_ALBUM && intent != null) {
            Uri dataUri = intent.getData();
            if (dataUri != null){
                cropImageFromAlbum(dataUri);
            }
        } else if (requestCode == CROP_FROM_ALBUM && intent != null) {
            Picasso.with(this).load(profileIconFile).into(profileIconImage);
            uploadProfileIcon();
        }

    }

    //uploads profile image on server
    private void uploadProfileIcon() {
        RemoteLib.getInstance().uploadMemberIcon(memberInfoItem.seq,profileIconFile);

        memberInfoItem.memberIconFilename = profileIconFilename + ".png";

    }

}
