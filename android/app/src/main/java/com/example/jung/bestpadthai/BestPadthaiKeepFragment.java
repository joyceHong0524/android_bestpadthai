package com.example.jung.bestpadthai;

import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.jung.bestpadthai.item.FoodInfoItem;
import com.example.jung.bestpadthai.item.GeoItem;
import com.example.jung.bestpadthai.item.KeepItem;
import com.example.jung.bestpadthai.lib.MyLog;
import com.example.jung.bestpadthai.remote.RemoteService;
import com.example.jung.bestpadthai.remote.ServiceGenerator;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BestPadthaiKeepFragment extends Fragment {
    private final String TAG = this.getClass().getSimpleName();

    Context context;
    int memberSeq;

    RecyclerView keepRecyclerView;
    TextView noDataText;

    KeepListAdapter keepListAdapter;

    ArrayList<KeepItem> keepList = new ArrayList<>();

    public static BestPadthaiKeepFragment newInstance(){
        BestPadthaiKeepFragment f = new BestPadthaiKeepFragment();
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        context = this.getActivity();

        memberSeq = ((MyApp) getActivity().getApplication()).getMemberSeq();

        View layout = inflater.inflate(R.layout.fragment_bestpadthai_keep,container,false);

        return layout;
    }



    @Override
    public void onResume() {
        super.onResume();

        MyApp myApp = ((MyApp) getActivity().getApplication());
        FoodInfoItem currentInfoItem = myApp.getFoodInfoItem();

        if(keepListAdapter != null && currentInfoItem != null){
            keepListAdapter.setItem(currentInfoItem);
            myApp.setFoodInfoItem(null);

            if (keepListAdapter.getItemCount() == 0){
                noDataText.setVisibility(View.VISIBLE);
            }
        }


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.nav_keep);

        keepRecyclerView = (RecyclerView) view.findViewById(R.id.keep_list);
        noDataText = (TextView) view.findViewById(R.id.no_keep);

        keepListAdapter = new KeepListAdapter(context,R.layout.row_bestpadthai_keep,keepList,memberSeq);

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);

        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        keepRecyclerView.setLayoutManager(layoutManager);
        keepRecyclerView.setAdapter(keepListAdapter);

        listKeep(memberSeq, GeoItem.getKnownLocation());
    }

    private void listKeep(int memberSeq, LatLng userLatLng){
        RemoteService remoteService = ServiceGenerator.createService(RemoteService.class);

        Call<ArrayList<KeepItem>> call = remoteService.listKeep(memberSeq, userLatLng.latitude, userLatLng.longitude);

        call.enqueue(new Callback<ArrayList<KeepItem>>() {
            @Override
            public void onResponse(Call<ArrayList<KeepItem>> call, Response<ArrayList<KeepItem>> response) {
                ArrayList<KeepItem> list = response.body();

                if (list == null){
                    list = new ArrayList<>();
                }

                noDataText.setVisibility(View.GONE);

                if (response.isSuccessful()){
                    if(list.size() == 0){
                        noDataText.setVisibility(View.VISIBLE);
                    } else{
                        keepListAdapter.setItemList(list);
                    }
                }else{
                    MyLog.d(TAG,"Failed to get a keepItemList");
                }
            }

            @Override
            public void onFailure(Call<ArrayList<KeepItem>> call, Throwable t) {
                MyLog.d(TAG,"no internet connectivity") ;
                MyLog.d(TAG,t.toString());
            }
        });
    }
}
