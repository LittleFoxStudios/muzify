package com.littlefoxstudios.muzify.homescreenfragments.history.innercard;

import androidx.lifecycle.LiveData;

import com.littlefoxstudios.muzify.Utilities;
import com.littlefoxstudios.muzify.datastorage.LocalStorage;

import java.util.ArrayList;
import java.util.List;

public class Album {
    private String albumName;
    private String albumArtist;
    private String albumCoverURL;
    private Boolean isSongFailed;

    private int processedBy;

    class FilterType
    {
        public static final int SHOW_ALL = 1;
        public static final int FAILED_ITEMS = 2;
    }

    public Album(String albumCoverURL, String albumName, String albumArtist, Boolean isSongFailed, int processedBy) {
        this.albumCoverURL = albumCoverURL;
        this.albumName = albumName;
        this.albumArtist = albumArtist;
        this.isSongFailed = isSongFailed;
        this.processedBy = processedBy;
    }

    public enum ProcessedBy
    {
        NOT_AVAILABLE(-1, "Sorry, We are unable to find matching music in the service"),
        MUSIC_ARTIST_TIME(0, "Music name, Artist and Time match"),
        MUSIC_ARTIST(1, "Music name and Artist match"),
        MUSIC_ONLY(2, "Music name match"),
        ERROR_WHILE_UPLOADING(3, "Unable to add song to the playlist");


        int processCode;
        String message;

        public int getProcessCode(){
            return processCode;
        }

        ProcessedBy(int processCode, String message){
            this.processCode = processCode;
            this.message = message;
        }

        public static boolean isFailedSong(int processCode){
          return processCode == NOT_AVAILABLE.getProcessCode() || processCode == ERROR_WHILE_UPLOADING.getProcessCode();
        }

        public static ProcessedBy getProcessedByObj(int code) throws Exception {
            for(ProcessedBy processedBy : values()){
                if(processedBy.processCode == code){
                    return processedBy;
                }
            }
            throw new Exception("Unsupported Processing code");
        }

        public String getProcessedByText()
        {
            return this.message;
        }
    }

    public String getAlbumArtist() {
        return albumArtist;
    }

    public Boolean getIsSongFailed()
    {
        return isSongFailed;
    }

    public void setAlbumArtist(String albumArtist) {
        this.albumArtist = albumArtist;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getAlbumCoverURL() {
        return albumCoverURL;
    }

    public void setAlbumCoverURL(String albumCoverURL) {
        this.albumCoverURL = albumCoverURL;
    }



    public static List<LocalStorage.Album> getFilteredAlbums(int filterType, List<LocalStorage.Album> albums)
    {
        switch (filterType){
            case FilterType.SHOW_ALL:{
                return albums;
            }
            case FilterType.FAILED_ITEMS:{
                List<LocalStorage.Album> newAlbums = new ArrayList<>();
                for(LocalStorage.Album album : albums){
                    if(ProcessedBy.isFailedSong(album.getProcessedBy())){
                        newAlbums.add(album);
                    }
                }
                return newAlbums;
            }
        }
        return albums;
    }

    public static int getAlbumSize(int filterType, List<LocalStorage.Album> albums){
        return getFilteredAlbums(filterType, albums).size();
    }

    public static List<LocalStorage.Album> sortAlbums(List<LocalStorage.Album> unSortedAlbums){
        List<LocalStorage.Album> successAlbums = new ArrayList<>();
        List<LocalStorage.Album> failedAlbums = new ArrayList<>();
        for(LocalStorage.Album album : unSortedAlbums){
            if(ProcessedBy.isFailedSong(album.getProcessedBy())){
                failedAlbums.add(album);
            }else{
                successAlbums.add(album);
            }
        }
        successAlbums.addAll(failedAlbums);
        return successAlbums;
    }

}