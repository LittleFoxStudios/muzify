package com.littlefoxstudios.muzify.homescreenfragments.transfer.datalayout;

import java.util.ArrayList;

public class Playlist{
    private String playlistTitle;
    private String playlistThumbnailUrl;
    private String playlistID;
    private int playlistItemsSize;
    private ArrayList<PlaylistItem> playlistItems;

    public Playlist(String playlistID, String playlistTitle, String playlistThumbnailUrl, int itemsCount){
        this.playlistID = playlistID;
        this.playlistTitle = playlistTitle;
        this.playlistThumbnailUrl = playlistThumbnailUrl;
        this.playlistItemsSize = itemsCount;
    }

    public Playlist(String playlistID, String playlistTitle, String playlistThumbnailUrl){
        //used by likes playlist
        this.playlistID = playlistID;
        this.playlistTitle = playlistTitle;
        this.playlistThumbnailUrl = playlistThumbnailUrl;
    }

    public String getPlaylistTitle() {
        return playlistTitle;
    }

    public String getPlaylistThumbnailUrl() {
        return playlistThumbnailUrl;
    }

    public String getPlaylistID() {
        return playlistID;
    }

    public ArrayList<PlaylistItem> getPlaylistItems()
    {
        return this.playlistItems;
    }

    public void updatePlaylistItems(ArrayList<PlaylistItem> playlistItems){
//        if(this.playlistItems == null){
//            this.playlistItems = new ArrayList<>();
//        }
//        this.playlistItems.addAll(playlistItems);
        this.playlistItems = (playlistItems == null) ? new ArrayList<>() : playlistItems;
    }

    public int getPlaylistItemsSize() {
        return playlistItemsSize;
    }

    public void setPlaylistItemsSize(int playlistItemsSize) {
        this.playlistItemsSize = playlistItemsSize;
    }



}
