package com.littlefoxstudios.muzify.homescreenfragments.transfer.datalayout;

import static com.littlefoxstudios.muzify.Utilities.getRandomDrawableIDForThumbnail;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.littlefoxstudios.muzify.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ReceiveInfoRecyclerView extends RecyclerView.Adapter<ReceiveInfoRecyclerView.MyViewHolder> {
    private Context context;
    private ArrayList<ReceiveInfo> infoList;

    public ReceiveInfoRecyclerView(Context context, ArrayList<ReceiveInfo> infoList){
        this.context = context;
        this.infoList = infoList;
    }

    public void update(ArrayList<ReceiveInfo> infoList){
        this.infoList = infoList;
    }

    @NonNull
    @Override
    public ReceiveInfoRecyclerView.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View views = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.receiving_transfer_updation_block, parent,false);
        return new ReceiveInfoRecyclerView.MyViewHolder(views);
    }

    @Override
    public void onBindViewHolder(@NonNull ReceiveInfoRecyclerView.MyViewHolder holder, int position) {
        ReceiveInfo info = infoList.get(position);
        if(info.isLoading()){
            //hide status image and show progress bar
            holder.itemProgressBar.setVisibility(View.VISIBLE);
            holder.itemStatusImage.setVisibility(View.GONE);
        }else{
            //show status image and hide progress bar
            holder.itemProgressBar.setVisibility(View.GONE);
            holder.itemStatusImage.setVisibility(View.VISIBLE);
        }

        if(info.showImage){
            Picasso.get().load(info.getAlbumImageURL()).placeholder(info.getItemImage()).into(holder.itemImage);
        }else{
            holder.itemImage.setImageResource(info.getItemImage());
        }

       holder.itemStatusImage.setImageResource(info.getItemStatusImage());
       holder.itemTitle.setText(info.getItemTitle());
       holder.itemInfo.setText(info.getItemInfo());
       holder.itemInfo.setTextColor(info.getItemInfoColour());
    }

    @Override
    public int getItemCount() {
        return (infoList == null) ? 0 : infoList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView itemImage;
        TextView itemTitle;
        TextView itemInfo;
        ImageView itemStatusImage;
        ProgressBar itemProgressBar;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.receiveTransferItemImage);
            itemTitle = itemView.findViewById(R.id.receiveTransferItemTitle);
            itemInfo = itemView.findViewById(R.id.receiveTransferItemInfo);
            itemStatusImage = itemView.findViewById(R.id.receiveTransferItemStatusImage);
            itemProgressBar = itemView.findViewById(R.id.receiveTransferItemProgressBar);
        }
    }
}
