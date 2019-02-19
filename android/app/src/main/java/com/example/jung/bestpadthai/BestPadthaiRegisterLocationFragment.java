package com.example.jung.bestpadthai;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.jung.bestpadthai.lib.GeoLib;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import com.example.jung.bestpadthai.item.FoodInfoItem;
import com.example.jung.bestpadthai.item.GeoItem;
import com.example.jung.bestpadthai.lib.GoLib;
import com.example.jung.bestpadthai.lib.MyLog;
import com.example.jung.bestpadthai.lib.StringLib;
import com.google.android.gms.maps.model.MarkerOptions;

import org.parceler.Parcels;

public class BestPadthaiRegisterLocationFragment extends Fragment implements View.OnClickListener, OnMapReadyCallback,
        GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener {

    private static final int MAP_ZOOM_LEVEL_DEFAULT = 16;
    private static final int MAP_ZOOM_LEVEL_DETAIL = 18;
    public static final String INFO_ITEM = "INFO_ITEM";

    private final String TAG = this.getClass().getSimpleName();

    Context context;
    FoodInfoItem infoItem;
    GoogleMap map;

    TextView addressText;

    //FoodInfoItem 객체를 인자로 저장하는 BestFoodRegisterLocationFragment 인스턴스를 생성해서 반환한다

    public static BestPadthaiRegisterLocationFragment newInstance(FoodInfoItem infoItem) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(INFO_ITEM, Parcels.wrap(infoItem));

        BestPadthaiRegisterLocationFragment fragment = new BestPadthaiRegisterLocationFragment();
        fragment.setArguments(bundle);

        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null ) {
            infoItem = Parcels.unwrap(getArguments().getParcelable(INFO_ITEM));
            if(infoItem.seq != 0) { //food Info Item이 하나라도 등록된 것이 있다면
                BestPadthaiRegisterActivity.currentItem = infoItem;
            }
            MyLog.d(TAG, "infoItem " + infoItem);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inlater, ViewGroup container, Bundle savedInstanceState)
    {
        context = this.getActivity();
        View layout = inlater.inflate(R.layout.fragment_bestpathai_register_location,container,false);
        return layout;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentManager fm = getChildFragmentManager();
        SupportMapFragment fragment = (SupportMapFragment) fm.findFragmentById(R.id.map);

        if (fragment == null) {
            fragment = SupportMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.content_main, fragment).commit();
        }
        fragment.getMapAsync(this);

        addressText = (TextView) view.findViewById(R.id.bestfood_address);

        Button nextButton = (Button) view.findViewById(R.id.next);
        nextButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
//        GoLib.getInstance().goFragment(getFragmentManager(),
//                R.id.content_main, BestPadthaiRegisterInputFragment.newInstance(infoItem));
    }

    @Override
    public void onMapClick(LatLng latLng) {
        MyLog.d(TAG,"onMapClick "+ latLng);
        setCurrentLatLng(latLng);
        addMarker(latLng, map.getCameraPosition().zoom);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        movePosition(marker.getPosition(),MAP_ZOOM_LEVEL_DETAIL);

        return false;
    }

    private void movePosition(LatLng latlng, float zoomLevel) {
        CameraPosition cp = new CameraPosition.Builder().target((latlng)).zoom(zoomLevel).build();

        map.moveCamera(CameraUpdateFactory.newCameraPosition(cp));
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        setCurrentLatLng(marker.getPosition());

        MyLog.d(TAG,"onMarkerDragEnd infoItem "+infoItem);
    }

    //새로운 곳으로 커서가 드래그 되었을 때 호출되는 메소드 이다.
    private void setCurrentLatLng(LatLng latLng) {
        infoItem.latitude = latLng.latitude;
        infoItem.longitude = latLng.longitude;
        setAddressText(latLng);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;

        String fineLocationPermission = android.Manifest.permission.ACCESS_FINE_LOCATION;

        if(ActivityCompat.checkSelfPermission(context,fineLocationPermission) != PackageManager.PERMISSION_GRANTED ) return;

        map.setMyLocationEnabled(true);
        map.setOnMarkerClickListener(this);
        map.setOnMarkerDragListener(this);
        map.setOnMapClickListener(this);

        UiSettings setting = map.getUiSettings();
        setting.setMyLocationButtonEnabled(true);
        setting.setCompassEnabled(true);
        setting.setZoomControlsEnabled(true);

        LatLng firstLatLng = new LatLng(infoItem.latitude, infoItem.longitude);
        if(infoItem.latitude !=0) {
            addMarker(firstLatLng, MAP_ZOOM_LEVEL_DEFAULT);

        }

        setAddressText(firstLatLng);

    }

    //configures google map
    //generates marker and add to google map according to passed latitude, longtitude, and zoom level

    private void addMarker(LatLng latLng, float zoomLevel) {
        MarkerOptions marker = new MarkerOptions();
        marker.position(latLng);
        marker.title("current location");
        marker.draggable(true);

        map.clear();
        map.addMarker(marker);

        movePosition(latLng, zoomLevel);
    }

    private void setAddressText(LatLng latLng) {
        MyLog.d(TAG,"setAddressText "+latLng);
        Address address = GeoLib.getInstance().getAddressString(context, latLng);
        String addressStr =GeoLib.getInstance().getAddressString(address);

        if(!StringLib.getInstance().isBlank(addressStr)) {
            addressText.setText(addressStr);
        }

    }


}
