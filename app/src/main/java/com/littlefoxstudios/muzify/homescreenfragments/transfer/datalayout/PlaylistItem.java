package com.littlefoxstudios.muzify.homescreenfragments.transfer.datalayout;

import com.littlefoxstudios.muzify.Utilities;
import com.littlefoxstudios.muzify.datastorage.LocalStorage;
import com.littlefoxstudios.muzify.homescreenfragments.history.innercard.Album;

import java.util.ArrayList;

public class PlaylistItem {

    private String songName;
    private String artistName;
    private String thumbnailImageURL;
    private int processedBy;
    private String songID;
    private int index;
    //V1.2
    private String sourceSongID;

    public PlaylistItem(String songName,String sourceSongID, String artistName, String thumbnailImageURL, int index) {
        this.songName = songName;
        this.artistName = artistName;
        this.thumbnailImageURL = thumbnailImageURL;
        this.index = index;
        this.sourceSongID = sourceSongID;
    }

    public PlaylistItem(String songName, String sourceSongID, String artistName, String thumbnailImageURL, int processedBy, String songID, int index) {
        this.songName = songName;
        this.artistName = artistName;
        this.thumbnailImageURL = thumbnailImageURL;
        this.processedBy = processedBy;
        this.songID = songID;//destinationSongID
        this.index = index;
        this.sourceSongID = sourceSongID;
    }

    public int getIndex(){
        return index;
    }

    public String getSongID(){
        return songID;
    }

    public String getSourceSongID(){return sourceSongID;}

    public String getSongName() {
        return songName;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getThumbnailImageURL() {
        return thumbnailImageURL;
    }

    public static int getFailedItemsCount(ArrayList<PlaylistItem> playlistItems){
        if(playlistItems == null || playlistItems.size() == 0 ){
            return 0;
        }
        int count = 0;
        for(PlaylistItem playlistItem : playlistItems){
            if(Album.ProcessedBy.isFailedSong(playlistItem.processedBy)){
                count++;
            }
        }
        return count;
    }

    public static String getPlaylistThumbnailURLS(ArrayList<PlaylistItem> items){
        ArrayList<String> list = new ArrayList<>();
        int count = 0;
        for(int i=0;i<items.size();i++){
            String url = items.get(i).getThumbnailImageURL();
            if(url != null)
            {
              list.add(url);
              count++;
            }else{
               continue;
            }
            if(count == 4){
                break;
            }
        }
        return Utilities.convertListToString(list);
    }

    public int getProcessedBy(){
        return processedBy;
    }
}
