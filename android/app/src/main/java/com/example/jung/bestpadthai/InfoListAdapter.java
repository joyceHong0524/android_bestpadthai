package com.example.jung.bestpadthai;

import android.content.Context;
import android.os.Message;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jung.bestpadthai.item.FoodInfoItem;
import com.example.jung.bestpadthai.item.MemberInfoItem;
import com.example.jung.bestpadthai.lib.DialogLib;
import com.example.jung.bestpadthai.lib.GoLib;
import com.example.jung.bestpadthai.lib.MyLog;
import com.example.jung.bestpadthai.lib.StringLib;
import com.example.jung.bestpadthai.remote.RemoteService;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;


public class InfoListAdapter extends RecyclerView.Adapter<InfoListAdapter.ViewHolder> {

    private final String TAG = this.getClass().getSimpleName();

    private Context context;
    private int resource;
    private ArrayList<FoodInfoItem> itemList;
    private MemberInfoItem memberInfoItem;

    //adapter constructor

    public InfoListAdapter(Context context, int resource, ArrayList<FoodInfoItem> itemList){
        this.context = context;
        this.resource = resource;
        this.itemList = itemList;

        memberInfoItem = ((MyApp) context.getApplicationContext()).getMemberInfoItem();
    }



    //특정 아이템의 변경사항을 적용하기 위해 기본아이템을 새로운 아이템으로 변경.
    public void setItem(FoodInfoItem newItem) {
        for (int i = 0; i < itemList.size(); i++){
            FoodInfoItem item = itemList.get(i); //i번째 아이템을 갖는다고 생각하면 된다.

            if(item.seq == newItem.seq){
                itemList.set(i,newItem);
                notifyItemChanged(i);
                break;
            }
        }
    }

    //add new item "LIST". not an item.
    public void addItemList(ArrayList<FoodInfoItem> itemList){
        this.itemList.addAll(itemList);
        notifyDataSetChanged();
    }

    //chages status of keep
    public void changeItemKeep(int seq, boolean keep){
        for(int i=0;i<itemList.size();i++){
            if(itemList.get(i).seq == seq){
                itemList.get(i).isKeep = keep;
                notifyItemChanged(i);
                break;
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //뷰홀더를 생성하기 위해 자동으로 호출되는데 리스트가 10개라면 10번 호출되어 10개의 뷰홀더 객체를 만든다고 생각하면 된다.
        View v = LayoutInflater.from(parent.getContext()).inflate(resource,parent,false);
        MyLog.d("onCreateViewHolder has been called.");
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull InfoListAdapter.ViewHolder holder, int position) {
        final FoodInfoItem item = itemList.get(position);
        MyLog.d(TAG,"getView" + item);

        if(item.isKeep){
            holder.keep.setImageResource(R.drawable.ic_keep_on);
        } else{
            holder.keep.setImageResource(R.drawable.ic_keep_off);
        }

        holder.name.setText(item.name);
        holder.description.setText(StringLib.getInstance().getSubString(context,item.description,Constant.MAX_LENGTH_DESCRIPTION)); //50자 이상으로는 보이지 않게함.

        setImage(holder.image,item.imageFilename);

        holder.itemView.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                GoLib.getInstance().goBestPadthaiInfoActivity(context,item.seq);
            }
        });

        holder.keep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(item.isKeep){
                    DialogLib.getInstance().showKeepDeleteDialog(context,keepDeleteHander,memberInfoItem.seq, item.seq); //memberinfoItem.seq가 들어가는 이유는
                                                                                                                            //즐겨찾기는 개개인이 따로 해 놓는 것이기때문
                }else{
                    DialogLib.getInstance().showKeepInsertDialog(context,keepInsertHandler,memberInfoItem.seq,item.seq);
                }

            }
        });

    }

    private void setImage(ImageView imageView, String fileName){
        if(StringLib.getInstance().isBlank(fileName)){
            Picasso.with(context).load(R.drawable.bg_bestfood_drawer).into(imageView);
        }else{
            Picasso.with(context).load(RemoteService.IMAGE_URL + fileName).into(imageView);
        }
    }

    //즐겨찾기 추가 성공 핸들러
    Handler keepInsertHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);

            changeItemKeep(msg.what, true);
        }
    };

    Handler keepDeleteHander = new Handler() {
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);

            changeItemKeep(msg.what, false);
        }
    };



    @Override
    public int getItemCount() {
        return this.itemList.size();//must be this. otherwise recyclerview shows nothing.

    }

    //ViewHolder class which you need to implement for sure.
    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        ImageView keep;
        TextView name;
        TextView description;

        public ViewHolder(View itemView){
            super(itemView);

            image = (ImageView) itemView.findViewById(R.id.image);
            keep = (ImageView) itemView.findViewById(R.id.keep);
            name = (TextView) itemView.findViewById(R.id.name);
            description = (TextView) itemView.findViewById(R.id.description);
        }
    }


}
