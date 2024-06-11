package com.littlefoxstudios.muzify.homescreenfragments.transfer.datalayout;

import static com.littlefoxstudios.muzify.Utilities.getRandomDrawableIDForThumbnail;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.littlefoxstudios.muzify.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class TransferPlaylistItemsRecyclerView extends RecyclerView.Adapter<TransferPlaylistItemsRecyclerView.MyViewHolder>  {

    private Context context;
    private ArrayList<PlaylistItem> playlistItems;
    private ItemClickListener itemClickListener;

    public TransferPlaylistItemsRecyclerView(Context context, ArrayList<PlaylistItem> playlistItems){
        this.context = context;
        this.playlistItems = playlistItems;
    }

    public void update(ArrayList<PlaylistItem> playlistItems){
        this.playlistItems = playlistItems;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View views = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.transfer_playlist_items_block, parent,false);
        return new TransferPlaylistItemsRecyclerView.MyViewHolder(views);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        PlaylistItem item = playlistItems.get(position);
        holder.title.setText(item.getSongName());
        holder.artist.setText(item.getArtistName());
        Picasso.get().load(item.getThumbnailImageURL()).placeholder(getRandomDrawableIDForThumbnail()).into(holder.thumbnail);
        holder.title.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        holder.title.setSelected(true);
        holder.artist.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        holder.artist.setSelected(true);
    }

    @Override
    public int getItemCount() {
        return playlistItems.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ShapeableImageView thumbnail;
        TextView title;
        TextView artist;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = (ShapeableImageView) itemView.findViewById(R.id.transferPlaylistItemImage);
            title = (TextView) itemView.findViewById(R.id.transferPlaylistItemSongName);
            artist = (TextView) itemView.findViewById(R.id.transferPlaylistItemArtistName);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(view, getAdapterPosition());
            }
        }
    }

    public PlaylistItem getItem(int position)
    {
        return playlistItems.get(position);
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
