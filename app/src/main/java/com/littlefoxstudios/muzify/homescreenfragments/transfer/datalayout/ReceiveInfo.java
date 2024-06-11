package com.littlefoxstudios.muzify.homescreenfragments.transfer.datalayout;

import android.graphics.Color;

import com.littlefoxstudios.muzify.R;

import java.util.ArrayList;

public class ReceiveInfo {

    public static final int ERROR = Color.RED;
    public static final int SUCCESS = Color.GREEN;

    public static final int CREATE_PLAYLIST = 0;
    public static final int PLAYLIST_CREATION_SUCCESS = 1;
    public static final int PLAYLIST_CREATION_FAILED = 2;
    public static final int SONG_SEARCHING = 3;
    public static final int SONG_FOUND = 4;
    public static final int SONG_NOT_FOUND = 5;
    public static final int SONG_ADD_SUCCESS = 6;
    public static final int SONG_ADD_FAILED = 7;
    public static final int SONG_ON_HOLD = 8;

    private String itemTitle;
    private String itemInfo;
    private int itemInfoColour;
    private int itemImage;
    private int itemStatusImage;
    private boolean isLoading;
    private String albumImageURL;
    public boolean showImage = false;


    ReceiveInfo(int statusCode, String title, String albumImageURL){
        this.itemTitle = title;
        this.albumImageURL = albumImageURL;
        switch (statusCode)
        {
            case CREATE_PLAYLIST: {
                createPlaylist();
            }break;
            case PLAYLIST_CREATION_SUCCESS: {
                 playlistCreationSuccess();
            }break;
            case PLAYLIST_CREATION_FAILED: {
                 playlistCreationFailed();
            }break;
            case SONG_SEARCHING: {
                 songSearching();
            }break;
            case SONG_FOUND: {
                 songFound();
            }break;
            case SONG_NOT_FOUND: {
                 songNotFound();
            }break;
            case SONG_ADD_SUCCESS: {
                 songAddSuccess();
            }break;
            case SONG_ADD_FAILED: {
                 songAddFailed();
            }break;
            case SONG_ON_HOLD: {
                songOnHold();
            }
        }
    }


    private void setter(String itemInfo, boolean isLoading, int itemImage, int itemInfoColour){
        setter(itemInfo, isLoading, itemImage, itemInfoColour, 0, albumImageURL);
    }

    private void setter(String itemInfo, boolean isLoading, int itemImage, int itemInfoColour, int itemStatusImage){
        setter(itemInfo, isLoading, itemImage, itemInfoColour, itemStatusImage, albumImageURL);
    }

    private void setter(String itemInfo, boolean isLoading, int itemImage, int itemInfoColour, int itemStatusImage, String albumImageURL)
    {
        this.itemInfo = itemInfo;
        this.isLoading = isLoading;
        this.itemImage = itemImage;
        this.itemInfoColour = itemInfoColour;
        this.itemStatusImage = itemStatusImage;
        this.albumImageURL = albumImageURL;
    }


    private void createPlaylist()
    {
        setter("Creating Playlist", true, R.drawable.playlist_add_load_icon, Color.WHITE);
    }

    private void playlistCreationSuccess()
    {
        setter("Playlist created", false, R.drawable.playlist_add_success_icon, SUCCESS, R.drawable.song_added_to_playlist_icon);
    }

    private void playlistCreationFailed()
    {
        setter("Unable to create playlist", false, R.drawable.playlist_add_failed_icon, ERROR, R.drawable.song_failed_playlist_icon);
    }

    private void songOnHold()
    {
        setter("On hold", false, R.drawable.song_searching_icon, Color.GRAY, R.drawable.waiting_to_load_icon);
    }

    private void songSearching()
    {
        setter("Searching...", true, R.drawable.song_searching_icon, Color.WHITE);
    }

    private void songFound()
    {
        setter("Song found, uploading to playlist...", true, R.drawable.song_found_icon, Color.WHITE);
        this.showImage = true;
    }

    private void songNotFound()
    {
        setter("Unable to find song", false, R.drawable.song_not_found_icon, ERROR, R.drawable.song_failed_playlist_icon);
    }

    private void songAddSuccess()
    {
        setter("Song uploaded to playlist!", false, R.drawable.song_found_icon, SUCCESS, R.drawable.song_success_playlist_icon);
        this.showImage = true;
    }

    private void songAddFailed()
    {
        setter("Error while uploading song", false, R.drawable.song_not_found_icon, ERROR, R.drawable.song_failed_playlist_icon);
    }


    public static ArrayList<ReceiveInfo> updateItem(ArrayList<ReceiveInfo> infoList, int newStatusCode, int index){
        if(infoList != null && infoList.size() > 0){
            ReceiveInfo oldInfo = infoList.remove(index);
            infoList.add(index, new ReceiveInfo(newStatusCode, oldInfo.itemTitle, oldInfo.albumImageURL));
        }
        return infoList;
    }

    public static ArrayList<ReceiveInfo> generateInitialPlaylistInfo(Playlist playlist){
        ArrayList<ReceiveInfo> receiveInfoList = new ArrayList<>();
        receiveInfoList.add(new ReceiveInfo(CREATE_PLAYLIST, playlist.getPlaylistTitle(), null));
        for(PlaylistItem playlistItem : playlist.getPlaylistItems()){
            receiveInfoList.add(new ReceiveInfo(SONG_ON_HOLD, playlistItem.getSongName(), playlistItem.getThumbnailImageURL()));
        }
        return receiveInfoList;
    }

    public static ArrayList<ReceiveInfo> playlistCreationFailed(ArrayList<ReceiveInfo> receiveInfoList){
        for(int index = 0; index < receiveInfoList.size(); index++){
            if(index == 0){
                updateItem(receiveInfoList, PLAYLIST_CREATION_FAILED, 0);
                continue;
            }
            updateItem(receiveInfoList, SONG_ADD_FAILED, index);
        }
        return receiveInfoList;
    }

    public String getItemTitle() {
        return itemTitle;
    }

    public String getItemInfo() {
        return itemInfo;
    }

    public int getItemInfoColour() {
        return itemInfoColour;
    }

    public int getItemImage() {
        return itemImage;
    }

    public int getItemStatusImage() {
        return itemStatusImage;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public String getAlbumImageURL() {
        return albumImageURL;
    }
}

