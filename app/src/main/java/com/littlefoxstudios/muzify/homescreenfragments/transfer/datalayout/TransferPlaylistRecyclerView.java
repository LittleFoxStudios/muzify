package com.littlefoxstudios.muzify.homescreenfragments.transfer.datalayout;

import static com.littlefoxstudios.muzify.Utilities.getRandomDrawableIDForThumbnail;

import android.content.ClipData;
import android.content.Context;
import android.graphics.drawable.shapes.Shape;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.littlefoxstudios.muzify.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class TransferPlaylistRecyclerView extends RecyclerView.Adapter<TransferPlaylistRecyclerView.MyViewHolder> {

    private Context context;
    private ArrayList<Playlist> playlists;
    private ItemClickListener itemClickListener;

    public TransferPlaylistRecyclerView(Context context, ArrayList<Playlist> playlists)
    {
        this.context = context;
        this.playlists = playlists;
    }

    public void update(ArrayList<Playlist> playlists){
        this.playlists = playlists;
    }

    @NonNull
    @Override
    public TransferPlaylistRecyclerView.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View views = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.transfer_playlist_block, parent,false);
        return new TransferPlaylistRecyclerView.MyViewHolder(views);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Playlist obj = playlists.get(position);
        if(obj.getPlaylistThumbnailUrl().length() == 0){
            holder.thumbnail.setImageResource(getRandomDrawableIDForThumbnail());
        }else{
            Picasso.get().load(obj.getPlaylistThumbnailUrl()).placeholder(getRandomDrawableIDForThumbnail()).into(holder.thumbnail);
        }
        holder.title.setText(obj.getPlaylistTitle());
        holder.title.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        holder.title.setSelected(true);
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ShapeableImageView thumbnail;
        TextView title;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = (ShapeableImageView) itemView.findViewById(R.id.transferPlaylistThumbnail);
            title = (TextView) itemView.findViewById(R.id.transferPlaylistTitle);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(view, getAdapterPosition());
            }
        }
    }

    public Playlist getItem(int position)
    {
        return playlists.get(position);
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

}
