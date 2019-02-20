package com.example.jung.bestpadthai;

import android.content.Context;
import android.location.Address;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.jung.bestpadthai.lib.GeoLib;
import com.google.android.gms.maps.model.LatLng;
import com.example.jung.bestpadthai.item.FoodInfoItem;
import com.example.jung.bestpadthai.item.GeoItem;
import com.example.jung.bestpadthai.lib.GoLib;
import com.example.jung.bestpadthai.lib.MyLog;
import com.example.jung.bestpadthai.lib.StringLib;
import com.example.jung.bestpadthai.lib.MyToast;
import com.example.jung.bestpadthai.lib.EtcLib;
import com.example.jung.bestpadthai.remote.RemoteService;
import com.example.jung.bestpadthai.remote.ServiceGenerator;

import org.parceler.Parcels;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BestPadthaiRegisterInputFragment extends Fragment implements View.OnClickListener{

    public static final String INFO_ITEM = "INFO_ITEM";
    private final String TAG = this.getClass().getSimpleName();

    Context context;
    FoodInfoItem infoItem;
    Address address;

    EditText nameEdit;
    EditText telEdit;
    EditText descriptionEdit;
    TextView currentLength;

    //Returns instance which takes FoodInfoItem as a parameter.
    //This parameter will contain previous location information.
    //This is how you use fragment.
    public static BestPadthaiRegisterInputFragment newInstance(FoodInfoItem infoItem) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(INFO_ITEM, Parcels.wrap(infoItem));

        BestPadthaiRegisterInputFragment fragment = new BestPadthaiRegisterInputFragment();
        fragment.setArguments(bundle);

        return fragment;

    }

    //be called when new fragment instance is made
    //saves currentItem in BestPadthaiRegisterInstance.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            infoItem = Parcels.unwrap(getArguments().getParcelable(INFO_ITEM));
            if(infoItem.seq != 0){
                BestPadthaiRegisterActivity.currentItem = infoItem;
            }
        }

        MyLog.d(TAG,"infoItem " + infoItem);
    }

    //creates view based on fragment_bestpadthai_register_input.xml


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        context = this.getActivity();
        address = GeoLib.getInstance().getAddressString(context,new LatLng(infoItem.latitude, infoItem.longitude));
        MyLog.d(TAG,"address " + address);

        return inflater.inflate(R.layout.fragment_bestpadthai_register_input,container,false);

    }

    @Override
    public void onViewCreated(View view, Bundle s){
        super.onViewCreated(view, s);

        currentLength = (TextView) view.findViewById(R.id.current_length);
        nameEdit = (EditText) view.findViewById(R.id.bestfood_name);
        telEdit = (EditText) view.findViewById(R.id.bestfood_tel);
        descriptionEdit = (EditText) view.findViewById(R.id.bestfood_description);
        descriptionEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentLength.setText(String.valueOf(s.length()));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //sets address with given infoItem information.
        EditText addressEdit = (EditText) view.findViewById(R.id.bestfood_address);
        infoItem.address = GeoLib.getInstance().getAddressString(address);
        if(!StringLib.getInstance().isBlank(infoItem.address)){
            addressEdit.setText(infoItem.address);
        }

        //sets onClickListener of buttons.

        Button prevButton = (Button) view.findViewById(R.id.prev);
        prevButton.setOnClickListener(this);

        Button nextButton = (Button) view.findViewById(R.id.next);
        nextButton.setOnClickListener(this);
    }

    //manages click events.
    @Override
    public void onClick(View v) {
        infoItem.name = nameEdit.getText().toString();
        infoItem.tel = telEdit.getText().toString();
        infoItem.description = descriptionEdit.getText().toString();
        MyLog.d(TAG,"onClick imageItem "+ infoItem);

        if(v.getId() == R.id.prev){
            GoLib.getInstance().goFragment(getFragmentManager(),R.id.content_main,BestPadthaiRegisterLocationFragment.newInstance(infoItem));
        } else if (v.getId() == R.id.next){
            save();
        }

    }

    //checks the input information and save.
    private void save(){
        if(StringLib.getInstance().isBlank(infoItem.name)){
            MyToast.s(context, context.getResources().getString(R.string.input_bestfood_name));

            return;
        }

        if(StringLib.getInstance().isBlank(infoItem.tel) || !EtcLib.getInstance().isValidPhoneNumber(infoItem.tel)){

            if(StringLib.getInstance().isBlank(infoItem.tel)) {
                MyToast.s(context, context.getResources().getString(R.string.input_bestfood_tel));

            }
            if(!EtcLib.getInstance().isValidPhoneNumber(infoItem.tel)){
                MyToast.s(context, context.getResources().getString(R.string.not_valid_tel_number));
            }

            return;

        }

        insertFoodInfo();

    }

    //saves the input information in the server
    private void insertFoodInfo(){
        MyLog.d(TAG, infoItem.toString());
        RemoteService remoteService = ServiceGenerator.createService(RemoteService.class);

        Call<String> call = remoteService.insertFoodInfo(infoItem);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.isSuccessful()){
                    int seq = 0;
                    String seqString = response.body();

                    try{
                        seq = Integer.parseInt(seqString);
                    } catch (Exception e){
                        seq = 0;
                    }

                    if (seq ==0){
                        //it means fail
                    } else{
                        infoItem.seq = seq;
                        goNextPage();
                    }
                } else {
                    // it means fail
                    int statusCode = response.code();
                    ResponseBody errorBody = response.errorBody();
                    MyLog.d(TAG,"fail " + statusCode+ errorBody.toString());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                MyLog.d(TAG,"No internet Connectivity");
            }
        });

        }

        private void goNextPage() {
//        GoLib.getInstance().goFragmentBack(getFragmentManager(),r.id.content_main,BestPadthaiRegisterImageFragment.newInstance(infoItem.seq));
        }

}
