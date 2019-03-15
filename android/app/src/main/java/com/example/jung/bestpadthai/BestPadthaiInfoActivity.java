package com.example.jung.bestpadthai;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.jung.bestpadthai.custom.WorkaroundMapFragment;
import com.example.jung.bestpadthai.item.FoodInfoItem;
import com.example.jung.bestpadthai.lib.DialogLib;
import com.example.jung.bestpadthai.lib.EtcLib;
import com.example.jung.bestpadthai.lib.MyLog;
import com.example.jung.bestpadthai.lib.StringLib;
import com.example.jung.bestpadthai.remote.RemoteService;
import com.example.jung.bestpadthai.remote.ServiceGenerator;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import javax.security.auth.callback.PasswordCallback;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BestPadthaiInfoActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {
    private final String TAG = this.getClass().getSimpleName();
    public static final String INFO_SEQ = "INFO_SEQ";

    Context context;

    int memberSeq;
    int foodInfoSeq;

    FoodInfoItem item;
    GoogleMap map;


    View loadingText;
    ScrollView scrollView;
    ImageView keepImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_bestpadthai_info);

        context = this;

        loadingText = findViewById(R.id.loading_layout);

        memberSeq = ((MyApp) getApplication()).getMemberSeq();
        foodInfoSeq = getIntent().getIntExtra(INFO_SEQ,0);
        selectFoodInfo(foodInfoSeq, memberSeq);

        setToolbar();
    }

    //sets toolbar
    private void setToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();

        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("");
        }
    }

    //set a menu of right above. (go back button)

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_close,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch ( item.getItemId()){
            case android.R.id.home :
                finish();
                break;
            case R.id.action_close:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //search for foodInfoItem on the server.
    private void selectFoodInfo(int foodInfoSeq, int memberSeq) {
        RemoteService remoteService = ServiceGenerator.createService(RemoteService.class);

        Call<FoodInfoItem> call = remoteService.selectFoodInfo(foodInfoSeq, memberSeq);

        call.enqueue(new Callback<FoodInfoItem>(){
            @Override
            public void onResponse(Call<FoodInfoItem> call, Response<FoodInfoItem> response){
                FoodInfoItem infoItem = response.body();

                if (response.isSuccessful() && infoItem != null && infoItem.seq > 0) {
                    item = infoItem;
                    setView();
                    loadingText.setVisibility(View.GONE);
                } else{
                    loadingText.setVisibility(View.VISIBLE);
                    ((TextView) findViewById(R.id.loading_text)).setText(R.string.loading_not);
                }
            }

            @Override
            public void onFailure(Call<FoodInfoItem> call, Throwable t){
                MyLog.d(TAG, "No Internet Connectivity");
                MyLog.d(TAG, t.toString());
            }
        });
    }


    private void setView(){
        getSupportActionBar().setTitle(item.name);

        ImageView infoImage = (ImageView) findViewById(R.id.info_image);
        setImage(infoImage, item.imageFilename);

        TextView location = (TextView) findViewById(R.id.location);
        location.setOnClickListener(this);

        scrollView = (ScrollView) findViewById(R.id.scroll_view);

        FragmentManager fm = getSupportFragmentManager();
        WorkaroundMapFragment fragment = (WorkaroundMapFragment) fm.findFragmentById(R.id.map);

        if ( fragment == null ) {
            fragment = (WorkaroundMapFragment) SupportMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.content_main,fragment).commit(); //만약 사진 이미지가 없으면 메인에서 썼던 이미지로 교체하여라 이거임.
        }

        fragment.getMapAsync(this);

        fragment.setListener(new WorkaroundMapFragment.OnTouchListener() {
            @Override
            public void onTouch() {
                scrollView.requestDisallowInterceptTouchEvent(true);
            }
        });


        TextView nameText = (TextView) findViewById(R.id.name);

        if (!StringLib.getInstance().isBlank(item.name)){
            nameText.setText(item.name);
        } else{
            nameText.setText("NO NAME FOUND");
        }

        keepImage = (ImageView) findViewById(R.id.keep);
        keepImage.setOnClickListener(this);
        if(item.isKeep){ //즉 즐겨찾기에 등록이 되어있으면
            keepImage.setImageResource(R.drawable.ic_keep_on);
        } else{
            keepImage.setImageResource(R.drawable.ic_keep_off);
        }

        TextView address = (TextView) findViewById(R.id.address);

        if(!StringLib.getInstance().isBlank(item.address)){
            address.setText(item.address);
        } else{
            address.setVisibility(View.GONE);
        }

        TextView tel = (TextView) findViewById(R.id.tel);
        if(!StringLib.getInstance().isBlank(item.tel)){
            tel.setText(EtcLib.getInstance().getPhoneNumberText(item.tel));
            tel.setOnClickListener(this);
        } else{
            tel.setVisibility(View.GONE);
        }

        TextView description = (TextView) findViewById(R.id.description);
        if(!StringLib.getInstance().isBlank(item.description)){
            description.setText(item.description);
        }else {
            description.setText(R.string.no_text);
        }
    }

    @Override

    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return;
        }

        map.setMyLocationEnabled(true);

        UiSettings setting = map.getUiSettings();
        setting.setMyLocationButtonEnabled(true);
        setting.setCompassEnabled(true);
        setting.setZoomControlsEnabled(true);
        setting.setMapToolbarEnabled(true);

        MarkerOptions marker = new MarkerOptions();
        marker.position(new LatLng(item.latitude, item.longitude));
        marker.draggable(false);
        map.addMarker(marker);

        movePosition(new LatLng(item.latitude, item.longitude), Constant.MAP_ZOOM_LEVEL_DETAIL);
    }

    //manages when you clicked R.id.keep and R.id.location.

    @Override
    public void onClick(View view){
        if (view.getId() == R.id.keep) {
            if(item.isKeep){
                DialogLib.getInstance().showKeepDeleteDialog(context, keepHandler, memberSeq, item.seq);
                keepImage.setImageResource(R.drawable.ic_keep_off);
            } else{
                DialogLib.getInstance().showKeepInsertDialog(context, keepHandler, memberSeq, item.seq);
                keepImage.setImageResource(R.drawable.ic_keep_on);
            }
        } else if(view.getId()== R.id.location) {
            movePosition(new LatLng(item.latitude, item.longitude), Constant.MAP_ZOOM_LEVEL_DETAIL);
        }

    }

    Handler keepHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);

            item.isKeep = !item.isKeep;

            if(item.isKeep){
                keepImage.setImageResource(R.drawable.ic_keep_on);
            } else{
                keepImage.setImageResource(R.drawable.ic_keep_off);
            }
        }
    };

    //shows map according to the langtitude and longitude of the item selected.

    private void movePosition(LatLng latlng, float zoomLevel){
        CameraPosition cp = new CameraPosition.Builder().target((latlng)).zoom(zoomLevel).build();

        map.moveCamera(CameraUpdateFactory.newCameraPosition(cp));
    }

    //shows image on the imageView using Picasso.
    private void setImage(ImageView imageView, String fileName){
        if(StringLib.getInstance().isBlank(fileName)){ //if there is no imageFilename which means there is no registered image
            Picasso.with(context).load(R.drawable.bg_bestfood_drawer).into(imageView); //set the basic image of the drawer.
        } else{ //If the image is registered before.
            Picasso.with(context).load(RemoteService.IMAGE_URL+fileName).into(imageView);
        }
    }

    //called when the activity is on Pause mode. Saves the difference of the item.
    @Override
    protected void onPause(){
        super.onPause();
        ((MyApp) getApplication()).setFoodInfoItem(item);
    }
}
