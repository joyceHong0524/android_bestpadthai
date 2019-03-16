package com.example.jung.bestpadthai;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jung.bestpadthai.item.FoodInfoItem;
import com.example.jung.bestpadthai.item.KeepItem;
import com.example.jung.bestpadthai.lib.DialogLib;
import com.example.jung.bestpadthai.lib.GoLib;
import com.example.jung.bestpadthai.lib.MyLog;
import com.example.jung.bestpadthai.lib.StringLib;
import com.example.jung.bestpadthai.remote.RemoteService;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class KeepListAdapter extends RecyclerView.Adapter<KeepListAdapter.ViewHolder> {

    private final String TAG = this.getClass().getSimpleName();

    private Context context;
    private int resource;
    private ArrayList<KeepItem> itemList;
    private int memberSeq;

    //adapter constructor
    public KeepListAdapter(Context context, int resource, ArrayList<KeepItem> itemList, int memberSeq){
        this.context = context;
        this.resource = resource;
        this.itemList = itemList;
        this.memberSeq = memberSeq;
    }

    public void setItemList(ArrayList<KeepItem> itemList){
        this.itemList = itemList;
        notifyDataSetChanged();
    }

    public void setItem(FoodInfoItem newItem){
        for (int i=0; i < itemList.size(); i++){
            KeepItem item = itemList.get(i);

            if(item.seq == newItem.seq && !newItem.isKeep){
                itemList.remove(i);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void removeItem(int seq){
        for (int i=0; i < itemList.size(); i++){
            if(itemList.get(i).seq == seq){
                itemList.remove(i);
                notifyItemChanged(i);
                break;
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //리스트의 개수대로 생성이 된다고 생각하면 된다.
        View v = LayoutInflater.from(parent.getContext()).inflate(resource, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final KeepItem item = itemList.get(position);
        MyLog.d(TAG, "getView "+ item + " position :" + position);

        if(item.isKeep) {
            holder.keep.setImageResource(R.drawable.ic_keep_on);
        }else{
            holder.keep.setImageResource(R.drawable.ic_keep_off);
        }

        holder.name.setText(item.name);
        holder.description.setText(
                StringLib.getInstance().getSubString(context, item.description, Constant.MAX_LENGTH_DESCRIPTION)
        );

        setImage(holder.image, item.imageFilename);

        holder.itemView.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                GoLib.getInstance().goBestPadthaiInfoActivity(context, item.seq);
            }
        });

        holder.keep.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                DialogLib.getInstance().showKeepDeleteDialog(context, keepHandler, memberSeq, item.seq);
            }
        });
    }

    private void setImage(ImageView view, String fileName){
        MyLog.d(TAG, "setImage fileName" + fileName);

        if (StringLib.getInstance().isBlank(fileName)){
            Picasso.with(context).load(R.drawable.bg_bestfood_drawer).into(view);
        } else{
            Picasso.with(context).load(RemoteService.IMAGE_URL + fileName).into(view);
        }
    }

    Handler keepHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
        super.handleMessage(msg);

        removeItem(msg.what);}
    };


    @Override
    public int getItemCount() {
        return this.itemList.size();
    }

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
