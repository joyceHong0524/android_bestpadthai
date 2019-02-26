package com.example.jung.bestpadthai;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jung.bestpadthai.custom.EndlessRecyclerViewScrollListener;
import com.example.jung.bestpadthai.item.FoodInfoItem;
import com.example.jung.bestpadthai.item.GeoItem;
import com.example.jung.bestpadthai.lib.MyLog;
import com.example.jung.bestpadthai.remote.RemoteService;
import com.example.jung.bestpadthai.remote.ServiceGenerator;
import com.google.android.gms.maps.model.LatLng;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BestPadthaiListFragment extends Fragment implements View.OnClickListener {

    private final String TAG = this.getClass().getSimpleName();

    Context context;

    int memberSeq;

    RecyclerView bestFoodList;
    TextView noDataText;

    TextView orderMeter;
    TextView orderFavorite;
    TextView orderRecent;

    ImageView listType;

    InfoListAdapter infoListAdapter;
    StaggeredGridLayoutManager layoutManager;
    EndlessRecyclerViewScrollListener scrollListner;

    int listTypeValue = 2;
    String orderType;

    //generates BestPadthaiListFragment instance
    public static BestPadthaiListFragment newInstance() {
        BestPadthaiListFragment f = new BestPadthaiListFragment();
        return f;
    }

    //generates view based on fragment_bestpadthai_list.xml

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        context = this.getActivity();
        memberSeq = ((MyApp) this.getActivity().getApplication()).getMemberSeq();
        View layout = inflater.inflate(R.layout.fragment_bestpadthai_list, container, false);

        return layout;
    }

    @Override
    public void onResume(){
        super.onResume();

        MyApp myApp = ((MyApp) getActivity().getApplication());
        FoodInfoItem currentInfoItem = myApp.getFoodInfoItem();

        if (infolistAdapter != null && currentInfoItem != null){
            infoListAdapter.setItem(currentInfoItem);
            myApp.setFoodInfoItem(null);

        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        ((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.nav_list);

        orderType = Constant.ORDER_TYPE_METER;

        bestFoodList = (RecyclerView) view.findViewById(R.id.list);
        noDataText = (TextView) view.findViewById(R.id.no_data);
        listType = (ImageView) view.findViewById(R.id.list_type);

        orderMeter = (TextView) view.findViewById(R.id.order_meter);
        orderFavorite = (TextView) view.findViewById(R.id.order_favorite);
        orderRecent = (TextView) view.findViewById(R.id.order_recent);

        orderMeter.setOnClickListener(this);
        orderFavorite.setOnClickListener(this);
        orderRecent.setOnClickListener(this);
        listType.setOnClickListener(this);

        setRecyclerView();

        listInfo(memberSeq, GeoItem.getKnownLocation(), orderType, 0);

    }

    //맛집 정보 리스트를 스태거드그리드레이아웃으로 보여주도록 설정한다.
    private void setLayoutManager(int row){
        layoutManager = new StaggeredGridLayoutManager(row, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        bestFoodList.setLayoutManager(layoutManager);

    }

    //sets recycler view and add scroll listener.
    private void setRecyclerView(){
        setLayoutManager(listTypeValue);

        infoListAdapter = new InfoListAdapter(context, R.layout.row_bestpadthai_list,new ArrayList<FoodInfoItem>());
        bestFoodList.setAdapter(infoListAdapter);

        scrollListner = new EndlessRecyclerViewScrollListener(layoutManager){
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view){
                listInfo(memberSeq, GeoItem.getKnownLocation(),orderType, page);
            }
        };

        bestFoodList.addOnScrollListener(scrollListner);
    }

    //important
    //inquires food info through server.
    private void listInfo(int memberSeq, LatLng userLatLng, String orderType, final int currentPage){
        RemoteService remoteService = ServiceGenerator.createService(RemoteService.class);

        Call<ArrayList<FoodInfoItem>> call = remoteService.listFoodInfo(memberSeq, userLatLng.latitude, userLatLng.longitude, orderType, currentPage);
        call.enqueue(new Callback<ArrayList<FoodInfoItem>>(){
            @Override
            public void onResponse(Call<ArrayList<FoodInfoItem>> call, Response<ArrayList<FoodInfoItem>> response){
                ArrayList<FoodInfoItem> list = response.body();

                if(response.isSuccessful() && list != null){
                infoListAdapter.addItemList(list);

                if (infoListAdapter.getItemCount() == 0){
                    noDataText.setVisibility(View.VISIBLE);
                } else{
                    noDataText.setVisibility(View.GONE);
                }
                }
            }

            @Override
            public void onFailure(Call<ArrayList<FoodInfoItem>> call, Throwable t){
                MyLog.d("TAG", "no internet connectivity");
                MyLog.d("TAG", t.toString());
            }
            });
    }

    //manages click events.
    @Override
    public void onClick(View v){
        if (v.getId() == R.id.list_type){
            changeListType();
        } else {
            if (v.getId() == R.id.order_meter) {
                orderType = Constant.ORDER_TYPE_METER;
                setOrderTextColor(R.color.text_color_green,R.color.text_color_black,R.color.text_color_black);
            }
            else if (v.getId() == R.id.order_favorite) {
                orderType = Constant.ORDER_TYPE_METER;
                setOrderTextColor(R.color.text_color_black,R.color.text_color_green,R.color.text_color_black);
            }
            else if (v.getId() == R.id.order_recent) {
                orderType = Constant.ORDER_TYPE_METER;
                setOrderTextColor(R.color.text_color_black,R.color.text_color_black,R.color.text_color_green);
            }

            setRecyclerView();
            listInfo(memberSeq, GeoItem.getKnownLocation(), orderType, 0);
        }
    }

    //sets color of the order types.
    private void setOrderTextColor (int a, int b, int c) {
        orderMeter.setTextColor(ContextCompat.getColor(context,a));
        orderFavorite.setTextColor(ContextCompat.getColor(context,b));
        orderRecent.setTextColor(ContextCompat.getColor(context,c));
    }

    //changes the type of view of the recycler view.
    private void changeListType(){
        if(listTypeValue == 1){
            listTypeValue = 2;
            listType.setImageResource(R.drawable.ic_list2);
        } else {
            listTypeValue =1;
            listType.setImageResource(R.drawable.ic_list);
        }
    }
}
