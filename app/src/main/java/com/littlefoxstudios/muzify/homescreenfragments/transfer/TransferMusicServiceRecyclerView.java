package com.littlefoxstudios.muzify.homescreenfragments.transfer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.littlefoxstudios.muzify.R;


import java.util.ArrayList;


public class TransferMusicServiceRecyclerView extends RecyclerView.Adapter<TransferMusicServiceRecyclerView.MyViewHolder> {

    private Context context;
    private ArrayList<MusicServices> musicServices;
    private ItemClickListener itemClickListener;

    public TransferMusicServiceRecyclerView(Context context, ArrayList<MusicServices> musicServices)
    {
        this.context = context;
        this.musicServices = musicServices;
    }

    @NonNull
    @Override
    public TransferMusicServiceRecyclerView.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View views = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.transfer_music_service_block,parent,false);
        return new TransferMusicServiceRecyclerView.MyViewHolder(views);
    }

    @Override
    public void onBindViewHolder(@NonNull TransferMusicServiceRecyclerView.MyViewHolder holder, int position) {
        MusicServices ms = musicServices.get(position);
        holder.musicServiceName.setText(ms.getServiceName());
        holder.musicServiceImage.setImageResource(ms.getServiceImageCode());
    }

    @Override
    public int getItemCount() {
       return musicServices.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView musicServiceName;
        ImageView musicServiceImage;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            musicServiceName = itemView.findViewById(R.id.transferBlockMusicServiceName);
            musicServiceImage = itemView.findViewById(R.id.transferBlockMusicServiceImage);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (itemClickListener != null) itemClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    public MusicServices getItem(int position) {
        return musicServices.get(position);
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

}
