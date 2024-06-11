package com.littlefoxstudios.muzify.homescreenfragments.history.innercard;

import static com.littlefoxstudios.muzify.Utilities.getRandomDrawableIDForThumbnail;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.media.Image;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.littlefoxstudios.muzify.R;
import com.littlefoxstudios.muzify.Utilities;
import com.littlefoxstudios.muzify.datastorage.LocalStorage;
import com.littlefoxstudios.muzify.homescreenfragments.transfer.MusicServices;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CardRecyclerViewAdapter extends RecyclerView.Adapter<CardRecyclerViewAdapter.MyViewHolder> {

    private static final int PLAYLIST_ITEMS_SECTION = 0;
    private static final int MUZI_SHARE_USERS = 1;


    Context context;
    InnerCardObj innerCard;
    List<LocalStorage.Album> albums;
    CardInterface cardInterface;
    int code;
    int itemCount;

    List<LocalStorage.ShareInfo> shareInfos;
    String currentAccountEmail;

    private int border = R.drawable.border_thin_grey;

    public CardRecyclerViewAdapter()
    {
        //use this to reset rv
        this.itemCount = 0;
    }

    public CardRecyclerViewAdapter(Context context, InnerCardObj innerCard, List<LocalStorage.Album> albums, CardInterface cardInterface)
    {
        this.context = context;
        this.innerCard = innerCard;
        this.albums = albums;
        this.cardInterface = cardInterface;
        this.code = PLAYLIST_ITEMS_SECTION;
        this.itemCount = albums.size();
    }

    public CardRecyclerViewAdapter(List<LocalStorage.ShareInfo> shareInfos, Context context, String currentAccountEmail){
        this.shareInfos = shareInfos == null ? new ArrayList<>() : shareInfos;
        this.context = context;
        this.code = MUZI_SHARE_USERS;
        this.currentAccountEmail = currentAccountEmail == null ? "" : currentAccountEmail;
        this.itemCount = this.shareInfos.size();
    }



    @NonNull
    @Override
    public CardRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View views = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.history_innercard_playlist_items_recycler_view_v2,parent,false);
        return new CardRecyclerViewAdapter.MyViewHolder(views, cardInterface);
    }


    private void handleOnBindViewForPlaylistItems(@NonNull CardRecyclerViewAdapter.MyViewHolder holder, int position)
    {
        holder.innerCardPlaylistItemsLayout.setVisibility(View.VISIBLE);
        holder.innerCardShareBlockLayout.setVisibility(View.GONE);

        String albumTitle = albums.get(position).getAlbumSongName();
        String albumArtist = albums.get(position).getAlbumArtist();
        Boolean albumFailed = albums.get(position).getIsSongFailed();
        String albumImageURL = albums.get(position).getAlbumCoverURL();
        holder.albumTitle.setText(albumTitle);
        holder.albumArtist.setText(albumArtist);
        Picasso.get().load(albumImageURL).placeholder(getRandomDrawableIDForThumbnail()).into(holder.albumImage);
        if(albums.get(position).processedBy != Album.ProcessedBy.NOT_AVAILABLE.getProcessCode()){
            holder.processedBy.setText(albums.get(position).getProcessedByText());
        }else{
            holder.processedBy.setText("");
        }

        holder.sourcePlaylistItemExternalLinkSelector.setBackgroundResource(border);
        holder.destinationPlaylistItemExternalLinkSelector.setBackgroundResource(border);

        holder.externalContentLink.setColorFilter(context.getColor(R.color.white), PorterDuff.Mode.SRC_IN);
        selectPlaylistItemSelector(albums.get(position).isSourceSelected(), holder);

        holder.sourcePlaylistItemExternalLinkSelector.setImageResource(innerCard.originalSource.getLogoDrawableID());
        if(innerCard.originalSource.getCode() == innerCard.destination.getCode()){
            holder.destinationPlaylistItemExternalLinkSelector.setVisibility(View.INVISIBLE);
        }else{
            holder.destinationPlaylistItemExternalLinkSelector.setImageResource(innerCard.destination.getLogoDrawableID());
        }
        if(albumFailed){
            holder.externalContentLinkBlock.setVisibility(View.GONE);
            holder.albumFailImage.setImageResource(R.drawable.error_icon);
        }else{
            holder.externalContentLinkBlock.setVisibility(View.VISIBLE);
            holder.albumFailImage.setImageResource(0);
        }

        setWidthAndHeight(holder.sourcePlaylistItemExternalLinkSelector);
        setWidthAndHeight(holder.destinationPlaylistItemExternalLinkSelector);

    }

    private void handleOnBindViewForShareList(@NonNull CardRecyclerViewAdapter.MyViewHolder holder, int position){
        holder.innerCardPlaylistItemsLayout.setVisibility(View.GONE);
        holder.innerCardShareBlockLayout.setVisibility(View.VISIBLE);

        LocalStorage.ShareInfo shareInfo = shareInfos.get(position);
        Picasso.get().load(shareInfo.getProfilePictureURL()).placeholder(getRandomDrawableIDForThumbnail()).into(holder.profilePicture);
        holder.accountName.setText(shareInfo.getUserName());
        if(shareInfo.getOwnerEmailID().equals(currentAccountEmail)){
            holder.accountEmail.setText(shareInfo.getEmailID());
            holder.shareDate.setText(Utilities.convertTimeStampToDate(shareInfo.getSharedTime()));
        }else{
            holder.accountEmail.setText(Utilities.convertTimeStampToDate(shareInfo.getSharedTime()));
            holder.shareDate.setText("");
        }
        holder.serviceImage.setImageResource(Utilities.MusicService.getMusicServiceFromCode(shareInfo.getSharedDestinationCode()).getLogoDrawableID());
    }


    @Override
    public void onBindViewHolder(@NonNull CardRecyclerViewAdapter.MyViewHolder holder, int position) {
        if(code == PLAYLIST_ITEMS_SECTION){
            handleOnBindViewForPlaylistItems(holder, position);
        }else{
            handleOnBindViewForShareList(holder, position);
        }
    }


    private void setWidthAndHeight(ImageView image)
    {
        image.setMaxHeight(30);
        image.setMaxWidth(30);
        image.setPadding(5,5,5,5);
    }

    private int getGreyColor(Context context){
        return context.getColor(R.color.grey);
    }

    private static void changeExternalLinkBorderColor(ImageView image, int color)
    {
        int width = 3;
        ((GradientDrawable) image.getBackground()).setStroke(width, color);
    }


    private void selectPlaylistItemSelector(boolean isSourceSelected, @NonNull CardRecyclerViewAdapter.MyViewHolder holder){
        int themeColor;
        if(isSourceSelected){
            themeColor = (innerCard.originalSource.getThemeColor(context));
            changeExternalLinkBorderColor(holder.sourcePlaylistItemExternalLinkSelector, themeColor);
            changeExternalLinkBorderColor(holder.destinationPlaylistItemExternalLinkSelector, getGreyColor(context));
        }else{
            themeColor = (innerCard.destination.getThemeColor(context));
            changeExternalLinkBorderColor(holder.destinationPlaylistItemExternalLinkSelector, themeColor);
            changeExternalLinkBorderColor(holder.sourcePlaylistItemExternalLinkSelector, getGreyColor(context));
        }
        holder.externalContentLink.setColorFilter(themeColor, PorterDuff.Mode.SRC_IN);
    }

    @Override
    public int getItemCount() {
        return itemCount;
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder{

        ConstraintLayout innerCardPlaylistItemsLayout;
        ConstraintLayout innerCardShareBlockLayout;

        TextView albumTitle;
        TextView albumArtist;
        TextView processedBy;
        ImageView albumFailImage;
        ShapeableImageView albumImage;

        ImageView externalContentLink;
        ImageView sourcePlaylistItemExternalLinkSelector;
        ImageView destinationPlaylistItemExternalLinkSelector;
        RelativeLayout externalContentLinkBlock;

        View externalContentLinkShine;

        //share block
        ShapeableImageView profilePicture;
        TextView accountName;
        TextView accountEmail;
        TextView shareDate;
        ImageView serviceImage;

        void setWidthAndHeight(ImageView image)
        {
            image.setMaxHeight(30);
            image.setMaxWidth(30);
            image.setPadding(5,5,5,5);
        }

        private void selectPlaylistItemSelector(boolean isSourceSelected, int themeColor, int greyColor){
            if(isSourceSelected){
                changeExternalLinkBorderColor(sourcePlaylistItemExternalLinkSelector, themeColor);
                changeExternalLinkBorderColor(destinationPlaylistItemExternalLinkSelector, greyColor);
            }else{
                changeExternalLinkBorderColor(destinationPlaylistItemExternalLinkSelector, themeColor);
                changeExternalLinkBorderColor(sourcePlaylistItemExternalLinkSelector, greyColor);
            }
            externalContentLink.setColorFilter(themeColor, PorterDuff.Mode.SRC_IN);
        }

        public MyViewHolder(@NonNull View itemView, CardInterface cardInterface) {
            super(itemView);

            innerCardPlaylistItemsLayout = itemView.findViewById(R.id.innerCardPlaylistItemsRVBlock);
            innerCardShareBlockLayout = itemView.findViewById(R.id.icsb);

            albumTitle = itemView.findViewById(R.id.InnerCardSongTitleHolder);
            albumArtist = itemView.findViewById(R.id.InnerCardSongArtistHolder);
            albumImage = itemView.findViewById(R.id.InnerCardSongAlbumCoverHolder);
            albumFailImage = itemView.findViewById(R.id.InnerCardSongErrorHolder);
            processedBy = itemView.findViewById(R.id.InnerCardSongProcessedByHolder);
            externalContentLink = itemView.findViewById(R.id.playlistItemExternalContentLink);
            sourcePlaylistItemExternalLinkSelector = itemView.findViewById(R.id.sourcePlaylistItemExternalLinkSelector);
            destinationPlaylistItemExternalLinkSelector = itemView.findViewById(R.id.destinationPlaylistItemExternalLinkSelector);
            externalContentLinkShine = itemView.findViewById(R.id.innerCardPlaylistItemExternalContentShineEffect);
            externalContentLinkBlock = itemView.findViewById(R.id.externalContentLinkBlock);

            externalContentLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(cardInterface != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            cardInterface.handleExternalLinkClick(position);
                        }
                    }
                }
            });

            sourcePlaylistItemExternalLinkSelector.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    externalContentLinkShineAnimation(externalContentLinkShine, view.getContext());
                    setWidthAndHeight(sourcePlaylistItemExternalLinkSelector);
                    setWidthAndHeight(destinationPlaylistItemExternalLinkSelector);
                    selectPlaylistItemSelector(true, cardInterface.getServiceThemeColor(true), cardInterface.getSecondaryColor());
                    if(cardInterface != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            cardInterface.handleMusicServiceToggleClick(position, true);
                        }
                    }

                }
            });

            destinationPlaylistItemExternalLinkSelector.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    externalContentLinkShineAnimation(externalContentLinkShine, view.getContext());
                    setWidthAndHeight(sourcePlaylistItemExternalLinkSelector);
                    setWidthAndHeight(destinationPlaylistItemExternalLinkSelector);
                    selectPlaylistItemSelector(false, cardInterface.getServiceThemeColor(false), cardInterface.getSecondaryColor());
                    if(cardInterface != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            cardInterface.handleMusicServiceToggleClick(position, false);
                        }
                    }
                }
            });

            profilePicture = itemView.findViewById(R.id.icsb_profilePicture);
            accountName = itemView.findViewById(R.id.icsb_userName);
            accountEmail = itemView.findViewById(R.id.icsb_accountEmail);
            shareDate = itemView.findViewById(R.id.icsb_shareDate);
            serviceImage = itemView.findViewById(R.id.icsb_musicServiceImage);
        }

        private void externalContentLinkShineAnimation(View shine, Context context)
        {
            Animation anim = AnimationUtils.loadAnimation(context, R.anim.left_to_right_shine);
            shine.startAnimation(anim);
        }
    }

}
