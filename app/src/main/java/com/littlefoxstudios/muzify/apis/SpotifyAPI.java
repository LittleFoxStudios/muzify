package com.littlefoxstudios.muzify.apis;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.fragment.app.Fragment;

import com.android.volley.RequestQueue;
import com.google.api.services.youtube.YouTube;
import com.littlefoxstudios.muzify.HomeScreenActivity;
import com.littlefoxstudios.muzify.MuzifySharedMemory;
import com.littlefoxstudios.muzify.Utilities;
import com.littlefoxstudios.muzify.homescreenfragments.history.innercard.Album;
import com.littlefoxstudios.muzify.homescreenfragments.transfer.SpotifyTransferAuthentication;
import com.littlefoxstudios.muzify.homescreenfragments.transfer.datalayout.Playlist;
import com.littlefoxstudios.muzify.homescreenfragments.transfer.datalayout.PlaylistFragment;
import com.littlefoxstudios.muzify.homescreenfragments.transfer.datalayout.PlaylistItem;
import com.littlefoxstudios.muzify.homescreenfragments.transfer.datalayout.PlaylistItemsFragment;
import com.littlefoxstudios.muzify.homescreenfragments.transfer.datalayout.ReceiveInfo;
import com.littlefoxstudios.muzify.homescreenfragments.transfer.datalayout.ReceivePlaylistFragment;
import com.littlefoxstudios.muzify.homescreenfragments.transfer.datalayout.TransferInfo;
import com.littlefoxstudios.muzify.internet.ApiCaller;
import com.littlefoxstudios.muzify.internet.VolleyCallback;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class SpotifyAPI implements API{

    private static final int LIMIT = 50;
    public static final String YOUR_LIKES_PLAYLIST = "$yourLikes$";
    public static final String NEXT_BATCH = "nextBatch";
    private static final int MAX_UPLOAD_PER_BATCH = 100;
    private static final int MODE_SEARCH_SONG_ARTIST = 1;
    private static final int MODE_SEARCH_SONG_NAME = 2;

    private RequestQueue queue;
    private ApiCaller apiCaller;
    private Context context;
    private Activity activity;
    private MuzifySharedMemory muzifySharedMemory;
    private TransferInfo transferInfo;

    private ArrayList<PlaylistItem> selectedItems;
    private int playlistItemsCounter = 0;

    public SpotifyAPI(RequestQueue requestQueue, Activity activity, TransferInfo transferInfo){
        this.queue = requestQueue;
        this.apiCaller = new ApiCaller(context);
        this.context = activity.getApplicationContext();
        this.activity = activity;
        this.muzifySharedMemory = new MuzifySharedMemory(activity);
        this.transferInfo = transferInfo;
        selectedItems = new ArrayList<>();
    }

    public static String getUserDetailsEndpoint()
    {
        return "https://api.spotify.com/v1/me";
    }

    public static String getAllPlaylistEndpoint()
    {
        return "https://api.spotify.com/v1/me/playlists";
    }

    public static String getLikedSongsEndpoint()
    {
        return "https://api.spotify.com/v1/me/tracks";
    }

    public static String getAllPlaylistItemsEndpoint(String playlistID)
    {
        return "https://api.spotify.com/v1/playlists/"+playlistID+"/tracks";
    }

    public static String getCreatePlaylistEndpoint(String userID)
    {
        return "https://api.spotify.com/v1/users/"+userID+"/playlists";
    }

    public static String getSearchSongEndpoint()
    {
        return "https://api.spotify.com/v1/search";
    }

    public static String getInsertSongsToPlaylistEndpoint(String playlistID)
    {
        return "https://api.spotify.com/v1/playlists/"+playlistID+"/tracks";
    }

    private static String constructUrlWithParams(String url, HashMap<String,String> params){
        if(params == null || params.size() == 0){
            return url;
        }
        Set<String> set = params.keySet();
        url += "?";
        StringBuilder urlBuilder = new StringBuilder(url);
        for(String key : set){
            urlBuilder.append(key).append("=").append(params.get(key)).append("&");
        }
        return urlBuilder.toString();
    }



    /*
    get user details and opens the next fragment
     */
    public static void getUserDetails(Context context, RequestQueue queue, String accessToken, SpotifyTransferAuthentication callingActivity, MuzifySharedMemory muzifySharedMemory)
    {
        ApiCaller apiCaller = new ApiCaller(context);
        queue.add(apiCaller.doGet(getUserDetailsEndpoint(), "Bearer "+accessToken, new VolleyCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                String userName = result.optString("display_name");
                String userEmail = result.optString("email");
                muzifySharedMemory.setSpotifyUserID(result.optString("id"));
                String profilePictureURL = getProfilePicture(result);
                callingActivity.switchToNextFragment(userEmail, userName, profilePictureURL);
            }

            @Override
            public void onError(String result) {
                callingActivity.onErrorClose();
            }
        }));
    }

    private static String getProfilePicture(JSONObject obj){
        try{
            return obj.optJSONArray("images").getJSONObject(1).getString("url");
        }catch (Exception e){
            return "";
        }
    }

    private HashMap<String,String> constructCreatePlaylistRequestBody(String playlistTitle)
    {
        HashMap<String,String> requestBody = new HashMap<>();
        requestBody.put("name", playlistTitle);
        requestBody.put("description", "This playlist is created by using Muzify App :)");
        requestBody.put("public", "false");
        return new HashMap<String,String>(requestBody);
    }

    private String filterSongName(String songName, String artistName){
        songName = songName.replace(artistName, "");
        return songName;
    }

    private String songFilter(String songName){
        songName = songName.replace("Backstreet Boys", "");
        songName = songName.replace("Official Video", "");
        songName = songName.replace(" Official HD Video", "");
        songName = songName.replace("(Official HD Video)", "");
        return songName;
    }

    private String filterArtistName(String artistName)
    {
        artistName = artistName.replace(" - Topic", "");
        artistName = artistName.replace("BackstreetBoysVEVO", "Backstreet Boys");
        artistName = artistName.replace("VEVO", "");
        return artistName;
    }

    private String generateSongSearchQuery(String songName, String artistName, int searchMode) {
        songName = songFilter(songName);
        artistName = filterArtistName(artistName);
        String capArtistName = artistName.toUpperCase();
        String smallArtistName = artistName.toLowerCase();
        songName = songName.replaceAll("[^a-zA-Z0-9',-]", " ");
        songName = filterSongName(songName, capArtistName);
        songName = filterSongName(songName, smallArtistName);
        String artistWithoutSpace = artistName.replaceAll(" ", "");
        capArtistName = artistWithoutSpace.toUpperCase();
        smallArtistName = artistWithoutSpace.toLowerCase();
        songName = filterSongName(songName, artistName);
        songName = filterSongName(songName, capArtistName);
        songName = filterSongName(songName, smallArtistName);
        songName = songName.trim();
        if(searchMode == MODE_SEARCH_SONG_ARTIST){
           return getSearchSongEndpoint()+"?q=track:"+songName+"%20artist:"+artistName+"&type=track&limit=1";
       }
       return getSearchSongEndpoint()+"?q=track:"+songName+"&type=track&limit=1";
    }


    private void searchAndUploadSong(String playlistID, ArrayList<PlaylistItem> playlistItems, ReceivePlaylistFragment callingFragment, int counter){
        boolean isSharedTransfer = transferInfo.isSharedTransfer() && transferInfo.getSharedOriginalSourceCode() == Utilities.ServiceCode.SPOTIFY;
        searchAndUploadSong(playlistID, playlistItems, callingFragment, 0, MODE_SEARCH_SONG_ARTIST, isSharedTransfer);
    }

    private void nextItem(String playlistID, ArrayList<PlaylistItem> playlistItems, ReceivePlaylistFragment callingFragment, PlaylistItem playlistItem, boolean isSharedTransfer){
        searchAndUploadSong(playlistID, playlistItems, callingFragment, playlistItem.getIndex()+1, MODE_SEARCH_SONG_ARTIST, isSharedTransfer);
    }

    private void searchAndUploadSong(String playlistID, ArrayList<PlaylistItem> playlistItems, ReceivePlaylistFragment callingFragment, int counter, int searchMode, boolean isSharedTransfer)
    {
        if(playlistItems.size() == counter){
            if(selectedItems.size() == 0){
                onErrorComplete(callingFragment);
                return;
            }
            insertSongs(getSelectedSongIDs(), playlistID, 0, callingFragment, playlistItems);
            return;
        }

            PlaylistItem playlistItem = playlistItems.get(counter);

            if(isSharedTransfer){
                String itemID = playlistItem.getSongID();
                if(itemID == null || itemID.length() == 0){
                    transferInfo.addTransferredPlaylistItem(playlistItem.getSongName(), playlistItem.getSourceSongID(), playlistItem.getArtistName(), playlistItem.getThumbnailImageURL(), Album.ProcessedBy.NOT_AVAILABLE.getProcessCode(), "", playlistItem.getIndex());
                    callingFragment.updateItem(ReceiveInfo.SONG_NOT_FOUND, playlistItem.getIndex()+1);
                }else{
                    callingFragment.updateItem(ReceiveInfo.SONG_FOUND, playlistItem.getIndex()+1);
                    selectedItems.add( new PlaylistItem(playlistItem.getSongName(),playlistItem.getSourceSongID(), playlistItem.getArtistName(), playlistItem.getThumbnailImageURL(), playlistItem.getProcessedBy(), playlistItem.getSongID(), playlistItem.getIndex()));
                }
                nextItem(playlistID, playlistItems, callingFragment, playlistItem, true);
                return;
            }

            queue.add(apiCaller.doGet(generateSongSearchQuery(playlistItem.getSongName(), playlistItem.getArtistName(), searchMode), getAccessToken(), new VolleyCallback() {
                @Override
                public void onSuccess(JSONObject result) {

                    Utilities.Loggers.postInfoLog("COUNTER_SEARCH", "Counter : "+counter+" - "+playlistItem.getSongName());

                    JSONArray array = Objects.requireNonNull(result.optJSONObject("tracks")).optJSONArray("items");
                    if(array == null || array.length() == 0){
                        if(searchMode == MODE_SEARCH_SONG_NAME){
                            transferInfo.addTransferredPlaylistItem(playlistItem.getSongName(), playlistItem.getSourceSongID(), playlistItem.getArtistName(), playlistItem.getThumbnailImageURL(), Album.ProcessedBy.NOT_AVAILABLE.getProcessCode(), "", playlistItem.getIndex());
                            callingFragment.updateItem(ReceiveInfo.SONG_NOT_FOUND, playlistItem.getIndex()+1);
                            nextItem(playlistID, playlistItems, callingFragment, playlistItem, isSharedTransfer);
                            return;
                        }
                        searchAndUploadSong(playlistID, playlistItems, callingFragment, counter, MODE_SEARCH_SONG_NAME, isSharedTransfer);
                        return;
                    }
                    try {
                        callingFragment.updateItem(ReceiveInfo.SONG_FOUND, playlistItem.getIndex()+1);
                        String artist = (searchMode == MODE_SEARCH_SONG_ARTIST) ? playlistItem.getArtistName() : null;
                        selectedItems.add( new PlaylistItem(playlistItem.getSongName(),playlistItem.getSourceSongID() , artist, playlistItem.getThumbnailImageURL(), Album.ProcessedBy.MUSIC_ARTIST.getProcessCode(), array.getJSONObject(0).getString("uri"), playlistItem.getIndex()));
                    }catch (Exception e){}

                    nextItem(playlistID, playlistItems, callingFragment, playlistItem, isSharedTransfer);
                }

                @Override
                public void onError(String result) {
                    onErrorComplete(callingFragment);
                }
            }));
    }

    private HashMap<String,Object> getInsertSongsRequestBody(List<String> uris, int position)
    {
        HashMap<String,Object> requestBody = new HashMap<>();
        requestBody.put("position", position);
        requestBody.put("uris", uris);
        return requestBody;
    }

    private ArrayList<String> getSelectedSongIDs()
    {
        ArrayList<String> list = new ArrayList<>();
        for(PlaylistItem item : selectedItems){
            list.add(item.getSongID());
        }
        return list;
    }

    private int getProcessCode(String songID){
        for(PlaylistItem item : selectedItems){
            if(item.getSongID().equals(songID)){
                return item.getArtistName() == null ? Album.ProcessedBy.MUSIC_ONLY.getProcessCode() : Album.ProcessedBy.MUSIC_ARTIST.getProcessCode();
            }
        }
        return Album.ProcessedBy.NOT_AVAILABLE.getProcessCode();
    }

    private void insertSongs(ArrayList<String> selectedSongs, String playlistID, int position, ReceivePlaylistFragment callingFragment, ArrayList<PlaylistItem> playlistItems){
        Utilities.Loggers.postInfoLog("COUNTER_SEARCH", "INSERT SONG HIT");
        List<String> firstBatch = selectedSongs.stream().limit(MAX_UPLOAD_PER_BATCH).collect(Collectors.toList());
        HashMap<String,Object> requestBody = getInsertSongsRequestBody(firstBatch, position);
        queue.add(apiCaller.doPost(getInsertSongsToPlaylistEndpoint(playlistID), getAccessToken(), new VolleyCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                for(PlaylistItem item : selectedItems){
                    if(!firstBatch.contains(item.getSongID())){
                        continue;
                    }
                    callingFragment.updateItem(ReceiveInfo.SONG_ADD_SUCCESS, item.getIndex()+1);
                    int processCode =  getProcessCode(item.getSongID());
                    transferInfo.addTransferredPlaylistItem(item.getSongName(), item.getSourceSongID(), item.getArtistName(), item.getThumbnailImageURL(), processCode, item.getSongID(), item.getIndex());
                }

                if(firstBatch.size() == MAX_UPLOAD_PER_BATCH && MAX_UPLOAD_PER_BATCH - selectedSongs.size() < 0){
                    ArrayList<String> choppedList = new ArrayList<>(selectedSongs);
                    choppedList.subList(0, MAX_UPLOAD_PER_BATCH).clear();
                    insertSongs(choppedList, playlistID,position+MAX_UPLOAD_PER_BATCH-1, callingFragment, playlistItems);
                }else{
                   onSuccessComplete(callingFragment);
                }
            }

            @Override
            public void onError(String result) {
                onErrorComplete(callingFragment);
            }
        }, new JSONObject(requestBody)));
    }

    @Override
    public void uploadPlaylist(ReceivePlaylistFragment callingFragment) {
        try
        {
            callingFragment.updateItem(ReceiveInfo.CREATE_PLAYLIST, 0);
            TransferInfo transferInfo = callingFragment.getTransferInfo();
            Playlist playlist = transferInfo.getPlaylist();
            HashMap requestBody = constructCreatePlaylistRequestBody(playlist.getPlaylistTitle());
            queue.add(apiCaller.doPost(getCreatePlaylistEndpoint(muzifySharedMemory.getSpotifyUserID()), getAccessToken(), new VolleyCallback() {
                @Override
                public void onSuccess(JSONObject result) {
                    callingFragment.updateItem(ReceiveInfo.PLAYLIST_CREATION_SUCCESS, 0);
                    String createdPlaylistID = result.optString("id");
                    transferInfo.setDestinationPlaylistURL(createdPlaylistID);
                    ArrayList<PlaylistItem> playlistItems = playlist.getPlaylistItems();
                    try{
                        searchAndUploadSong(createdPlaylistID, playlistItems, callingFragment, 0);
                    }catch (Exception e){
                        Utilities.Loggers.showLongToast(context, "Error occurred while adding songs");
                       onErrorComplete(callingFragment);
                    }
                }

                @Override
                public void onError(String result) {
                    callingFragment.playlistCreationFailed();
                    onErrorComplete(callingFragment);
                }
            }, new JSONObject(requestBody)));
        }catch (Exception e)
        {
            Utilities.Loggers.postInfoLog("SPOTIFY_PLAYLIST_UPLOAD_ERROR", e.getMessage());
            callingFragment.onErrorComplete();
        }
    }

    @Override
    public void getAllPlaylistItems(Utilities.Alert alert, PlaylistItemsFragment fragment, String playlistID, HashMap<String, String> params, String extraString) throws Exception {
       String url = "";
       if(params != null && params.containsKey(NEXT_BATCH)){
           url = params.get(NEXT_BATCH);
       }else if(playlistID.equals(YOUR_LIKES_PLAYLIST)){
           url = constructUrlWithParams(getLikedSongsEndpoint(), params);
       }else{
           url = constructUrlWithParams(getAllPlaylistItemsEndpoint(playlistID), params);
       }

        queue.add(apiCaller.doGet(url, getAccessToken(), new VolleyCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                updatePlaylistItemsUI(result, alert, fragment, playlistID);
            }

            @Override
            public void onError(String result) {
                Utilities.Loggers.showLongToast(context, "Unable to fetch songs from playlist");
            }
        }));
    }

    private void updatePlaylistItemsUI(JSONObject result, Utilities.Alert alert, PlaylistItemsFragment fragment, String playlistID){
        try{
            String nextPageURL = result.optString("next");
            boolean downloadFinished = "".equals(nextPageURL) || "null".equals(nextPageURL);
            if(!downloadFinished){
                HashMap<String,String> params = new HashMap<>();
                params.put(NEXT_BATCH, nextPageURL);
                getAllPlaylistItems(alert, fragment, playlistID, params, "");
            }
            JSONArray array = result.optJSONArray("items");
            ArrayList<PlaylistItem> playlistItems = new ArrayList<>();
            for(int i=0;i<array.length();i++){
                String songName = array.getJSONObject(i).getJSONObject("track").getString("name");
                String artistName = getArtistName(array, i);
                String thumbnail = getThumbnailUrl(array, i);
                String sourceSongID = getSourceSongID(array, i);
                PlaylistItem playlistItem = new PlaylistItem(songName, sourceSongID, artistName, thumbnail, playlistItemsCounter++);
                playlistItems.add(playlistItem);
            }
            fragment.updateUI(playlistItems, downloadFinished);
        }catch (Exception e)
        {
            Utilities.Loggers.showLongToast(context, "Unable to fetch songs from playlist");
        }
    }

    private String getArtistName(JSONArray array, int i){
        try {
            return array.getJSONObject(i).getJSONObject("track").getJSONArray("artists").getJSONObject(0).getString("name");
        }catch (Exception e){
            return "";
        }
    }

    private String getThumbnailUrl(JSONArray array, int i){
        try{
            return array.getJSONObject(i).getJSONObject("track").getJSONObject("album").getJSONArray("images").getJSONObject(0).getString("url");
        }catch (Exception e){
            return "";
        }
    }

    private String getSourceSongID(JSONArray array, int i){
        try{
            return array.getJSONObject(i).getJSONObject("track").getString("uri");
        }catch (Exception ignored){
            return "";
        }
    }

    @Override
    public HashMap<String, String> getAllPlaylistItemsInitialParams() {
        HashMap<String,String> params = new HashMap<>();
        params.put("limit", LIMIT+"");
        return params;
    }

    @Override
    public void getAllPlaylists(Utilities.Alert alert, PlaylistFragment fragment, HashMap<String, String> params) throws Exception {
       String url = constructUrlWithParams(getAllPlaylistEndpoint(), params);
        if(params != null && params.containsKey(NEXT_BATCH)){
            url = params.get(NEXT_BATCH);
        }
        queue.add(apiCaller.doGet(url, getAccessToken(), new VolleyCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                updatePlaylistUI(result, fragment, alert);
            }

            @Override
            public void onError(String result) {
                if(alert != null) {
                    alert.stopDialog();
                }
                Utilities.Loggers.showLongToast(context, "Sorry! Unexpected error occurred!");
                switchToTransferFragment();
            }
        }));
    }



    private void updatePlaylistUI(JSONObject result, PlaylistFragment fragment, Utilities.Alert alert) {
        try
        {
            String nextPageURL = result.optString("next");
            boolean downloadFinished = "".equals(nextPageURL) || "null".equals(nextPageURL);
            if(!downloadFinished){
                HashMap<String,String> params = new HashMap<>();
                params.put(NEXT_BATCH, nextPageURL);
                getAllPlaylists(alert, fragment, params);
            }
            JSONArray array = result.optJSONArray("items");
            ArrayList<Playlist> playlists = new ArrayList<>();
            for(int i=0;i<array.length();i++){
                JSONObject object = array.getJSONObject(i);
                String playlistID = object.optString("id");
                JSONArray images = object.getJSONArray("images");
                String playListThumbnail = "";
                if(images.length() != 0){
                    playListThumbnail = images.getJSONObject(0).getString("url");
                }
                String playListTitle = object.optString("name");

                int itemsCount =  object.getJSONObject("tracks").getInt("total");
                Playlist playlist = new Playlist(playlistID, playListTitle, playListThumbnail, itemsCount);
                playlists.add(playlist);
            }
            fragment.updateUI(playlists, downloadFinished);
        }catch (Exception e){
            Utilities.Loggers.showLongToast(context, "Unable to fetch playlists");
            switchToTransferFragment();
        }
    }

    @Override
    public HashMap<String, String> getAllPlaylistInitialParams() {
        HashMap<String,String> params = new HashMap<>();
        params.put("limit", LIMIT+"");
        return params;
    }

    private void switchToTransferFragment() {
        transferInfo.clearTransfer();
        activity.startActivity(new Intent(activity, HomeScreenActivity.class));
        activity.finish();
    }

    private String getAccessToken()
    {
        return "Bearer "+muzifySharedMemory.getSpotifyAccessToken();
    }

    private void onErrorComplete(ReceivePlaylistFragment callingFragment){
        selectedItems.clear();
        callingFragment.onErrorComplete();
    }

    private void onSuccessComplete(ReceivePlaylistFragment callingFragment){
        selectedItems.clear();
        callingFragment.onComplete();
    }


}
