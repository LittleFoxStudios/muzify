package com.littlefoxstudios.muzify.apis;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Credentials;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.PlaylistListResponse;
import com.littlefoxstudios.muzify.Constants;
import com.littlefoxstudios.muzify.HomeScreenActivity;
import com.littlefoxstudios.muzify.MuzifyConfigs;
import com.littlefoxstudios.muzify.MuzifySharedMemory;
import com.littlefoxstudios.muzify.Utilities;
import com.littlefoxstudios.muzify.WelcomeActivity;
import com.littlefoxstudios.muzify.accounts.YoutubeMusic;
import com.littlefoxstudios.muzify.homescreenfragments.history.innercard.Album;
import com.littlefoxstudios.muzify.homescreenfragments.history.innercard.ContentLinkInterface;
import com.littlefoxstudios.muzify.homescreenfragments.transfer.YoutubeMusicTransferAuthentication;
import com.littlefoxstudios.muzify.homescreenfragments.transfer.datalayout.Playlist;
import com.littlefoxstudios.muzify.homescreenfragments.transfer.datalayout.PlaylistFragment;
import com.littlefoxstudios.muzify.homescreenfragments.transfer.datalayout.PlaylistItem;
import com.littlefoxstudios.muzify.homescreenfragments.transfer.datalayout.PlaylistItemsFragment;
import com.littlefoxstudios.muzify.homescreenfragments.transfer.datalayout.ReceiveInfo;
import com.littlefoxstudios.muzify.homescreenfragments.transfer.datalayout.ReceivePlaylistFragment;
import com.littlefoxstudios.muzify.homescreenfragments.transfer.datalayout.TransferDataCache;
import com.littlefoxstudios.muzify.homescreenfragments.transfer.datalayout.TransferInfo;
import com.littlefoxstudios.muzify.internet.AccessToken;
import com.littlefoxstudios.muzify.internet.ApiCaller;
import com.littlefoxstudios.muzify.internet.VolleyCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class YoutubeMusicAPI implements API {
    private RequestQueue queue;
    private ApiCaller apiCaller;
    private Context context;
    private Activity activity;
    private MuzifySharedMemory muzifySharedMemory;
    private TransferInfo transferInfo;

    public static final String READ_SCOPE = "read_";
    public static final String WRITE_SCOPE = "write_";


    String accessToken = "";
    Long expiry = 0l;

    private int currentIndex;
    private int playlistItemsCounter = 0;

    public static final String ALL_PLAYLISTS_URI = "https://youtube.googleapis.com/youtube/v3/playlists";
    public static final String PLAYLIST_ITEMS_URI = "https://youtube.googleapis.com/youtube/v3/playlistItems";
    public static final String CREATE_PLAYLIST_URI = "https://www.googleapis.com/youtube/v3/playlists";
    public static final String INSERT_ITEM_IN_PLAYLIST_URI = "https://www.googleapis.com/youtube/v3/playlistItems";

    private static final String PART = "part";
    private static final String PART_SNIPPET = "snippet";
    private static final String PART_CONTENT_DETAILS = "contentDetails";
    private static final String PART_STATUS = "status";

    private static final String QUOTA_EXHAUSTED = "Sorry, YouTube quota exhausted!";

    public static final String YOUR_LIKES_PLAYLIST = "LM";

    private static HashMap<String, String> addMine(HashMap<String,String> params)
    {
        params.put("mine", "true");
        return params;
    }

    private static HashMap<String, String> addPart(String part, HashMap<String,String> params){
        if(params.containsKey(PART)){
            params.put(PART, params.get(PART)+","+part);
        }else{
            params.put(PART, part);
        }
        return params;
    }

    private static HashMap<String,String> addPlaylistID(String playlistID, HashMap<String,String> params){
        params.put("playlistId", playlistID);
        return params;
    }

    private static HashMap<String,String> addNextPageToken(String nextPageToken, HashMap<String,String> params){
        params.put("pageToken", nextPageToken);
        return params;
    }

    public static HashMap<String,String> getInitialParams(String uri){
        HashMap<String,String> params = new HashMap<>();
        params.put("key", YoutubeMusic.API_KEY);
        switch (uri) {
            case ALL_PLAYLISTS_URI:
                addMine(params);
                addPart(PART_SNIPPET, params);
                addPart(PART_CONTENT_DETAILS, params);
                params.put("maxResults", "50");
                break;
            case PLAYLIST_ITEMS_URI:
                addMine(params);
                addPart(PART_SNIPPET, params);
                params.put("maxResults", "50");
                break;
            case CREATE_PLAYLIST_URI:
                addPart(PART_SNIPPET, params);
                addPart(PART_STATUS, params);
                break;
            case INSERT_ITEM_IN_PLAYLIST_URI:
                addPart(PART_SNIPPET, params);
                break;
        }

        return params;
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

    private static String searchSongUri(String songName){
        //first item based on relevance
        return "https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=1&q="+songName+"&type=video&key="+YoutubeMusic.API_KEY;
    }


    public YoutubeMusicAPI(RequestQueue requestQueue, Activity activity, TransferInfo transferInfo){
        this.queue = requestQueue;
        this.apiCaller = new ApiCaller(context);
        this.context = activity.getApplicationContext();
        this.activity = activity;
        this.muzifySharedMemory = new MuzifySharedMemory(activity);
        this.currentIndex = 0;
        this.transferInfo = transferInfo;
    }


    public void getAllPlaylists(Utilities.Alert alert, PlaylistFragment fragment, HashMap params) throws Exception {
        queue.add(apiCaller.doGet(constructUrlWithParams(ALL_PLAYLISTS_URI, params), getAccessToken(fragment.getCurrentEmailID(), READ_SCOPE), new VolleyCallback() {
            @Override
            public void onSuccess(JSONObject result) {

                updatePlaylistUI(result, fragment);
            }

            @Override
            public void onError(String result) {
                if(alert != null) {
                    alert.stopDialog();
                }
                Utilities.Loggers.showLongToast(context, QUOTA_EXHAUSTED);
                switchToTransferFragment();
            }
        }));
    }


    public void getAllPlaylistItems(Utilities.Alert alert, PlaylistItemsFragment fragment, String playlistID, HashMap<String,String> params, String gmail) throws Exception{
        addPlaylistID(playlistID, params);
        queue.add(apiCaller.doGet(constructUrlWithParams(PLAYLIST_ITEMS_URI, params), getAccessToken(gmail, READ_SCOPE), new VolleyCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                updatePlaylistItemsUI(result, fragment, playlistID, gmail);
            }

            @Override
            public void onError(String result) {
                if(alert != null) {
                    alert.stopDialog();
                }
                Utilities.Loggers.showLongToast(context, QUOTA_EXHAUSTED);
                switchToTransferFragment();
            }
        }));
    }

    @Override
    public HashMap<String, String> getAllPlaylistItemsInitialParams() {
        return getInitialParams(YoutubeMusicAPI.PLAYLIST_ITEMS_URI);
    }

    @Override
    public HashMap<String, String> getAllPlaylistInitialParams() {
        return getInitialParams(ALL_PLAYLISTS_URI);
    }

    private void loadNextPlaylistBatch(String nextPageToken, PlaylistFragment fragment) throws Exception {
        HashMap<String,String> params = getInitialParams(ALL_PLAYLISTS_URI);
        addNextPageToken(nextPageToken, params);
        getAllPlaylists(null, fragment, params);
    }

    private void loadNextPlaylistItemsBatch(String nextPageToken, PlaylistItemsFragment fragment, String playlistID, String gmail) throws Exception
    {
        HashMap<String,String> params = getInitialParams(PLAYLIST_ITEMS_URI);
        addPlaylistID(playlistID, params);
        addNextPageToken(nextPageToken, params);
        getAllPlaylistItems(null, fragment, playlistID, params, gmail);
    }


    private void updatePlaylistUI(JSONObject result, PlaylistFragment fragment) {
        try
        {
            updatePlaylistSection(result, fragment);
        }catch (Exception e)
        {
            Utilities.Loggers.showLongToast(context, "Unable to fetch playlists");
            switchToTransferFragment();
        }
    }

    private void updatePlaylistItemsUI(JSONObject result, PlaylistItemsFragment fragment, String playlistID, String gmail)
    {
        try{
            updatePlaylistItemsSection(result, fragment, playlistID, gmail);
        }catch (Exception e){
            Utilities.Loggers.showLongToast(context, "Unable to fetch songs from playlist");
        }
    }

    private void notifySongInserted(JSONObject result) {
    }

    private void notifySongSearch(JSONObject result) {
    }

    private void notifyPlaylistCreated(JSONObject result) {
    }

    private void updatePlaylistItemsSection(JSONObject result, PlaylistItemsFragment fragment, String playlistID, String gmail) throws Exception{
        String nextPageToken = result.optString("nextPageToken");
        boolean downloadFinished = nextPageToken.equals("");
        JSONArray array = result.optJSONArray("items");
        ArrayList<PlaylistItem> playlistItems = new ArrayList<>();
        for(int i=0;i<array.length();i++){
            JSONObject snippet = array.getJSONObject(i).getJSONObject("snippet");
            //Utilities.Loggers.postInfoLog("SNIPPET_YOUTUBE", snippet.toString());
            String songName = snippet.getString("title");
            if(songName.equals("Private video")){
                continue;
            }
            if(!snippet.has("videoOwnerChannelTitle")){
                continue;
            }
            String artistName = snippet.optString("videoOwnerChannelTitle");
            String thumbnail = snippet.getJSONObject("thumbnails").getJSONObject("high").getString("url");
            String sourceSongID = snippet.getJSONObject("resourceId").getString("videoId");
            PlaylistItem playlistItem = new PlaylistItem(songName, sourceSongID, artistName, thumbnail, playlistItemsCounter++);
            playlistItems.add(playlistItem);
        }
        fragment.updateUI(playlistItems, downloadFinished);
        if(!downloadFinished){
            try{
                loadNextPlaylistItemsBatch(nextPageToken, fragment, playlistID, gmail);
            }catch (Exception e){
                Utilities.Loggers.postInfoLog("YOUTUBE_PLAYLIST_ITEMS_NEXT_PAGE", "Unable to get next page data : "+e.getMessage());
                Utilities.Loggers.showLongToast(context, "Sorry, Unable to load next 50 songs");
                throw  e;
            }
        }
    }

    private void updatePlaylistSection(JSONObject result, PlaylistFragment fragment) throws Exception {
        try {
            String nextPageToken = result.optString("nextPageToken");
            boolean downloadFinished = nextPageToken.equals("");
            JSONArray array = result.optJSONArray("items");
            ArrayList<Playlist> playlists = new ArrayList<>();
            for(int i=0;i<array.length();i++){
                JSONObject object = array.getJSONObject(i);
                String playlistID = object.getString("id");
                JSONObject snippet = object.getJSONObject("snippet");
                String playListTitle = snippet.getString("title");
                String playListThumbnail = snippet.getJSONObject("thumbnails").getJSONObject("high").getString("url");
                int itemsCount = object.getJSONObject("contentDetails").getInt("itemCount");
                Playlist playlist = new Playlist(playlistID, playListTitle, playListThumbnail, itemsCount);
                playlists.add(playlist);
            }
            fragment.updateUI(playlists, downloadFinished);
            if(!downloadFinished){
                try{
                    loadNextPlaylistBatch(nextPageToken, fragment);
                }catch (Exception e){
                    Utilities.Loggers.postInfoLog("YOUTUBE_PLAYLIST_NEXT_PAGE", "Unable to get next page data : "+e.getMessage());
                    Utilities.Loggers.showLongToast(context, "Sorry, Unable to load next 50 playlists");
                    throw  e;
                }
            }
        }catch (Exception e){
            Utilities.Loggers.postInfoLog("YOUTUBE_MUSIC_PLAYLIST_SECTION", "Error : "+e.getMessage());
            throw e;
        }
    }


    @Override
    public void uploadPlaylist(ReceivePlaylistFragment callingFragment) {
        callingFragment.updateItem(ReceiveInfo.CREATE_PLAYLIST, 0);
        TransferInfo transferInfo = callingFragment.getTransferInfo();
        String gmail = transferInfo.getDestinationAccountEmailID();
        Playlist playlist = transferInfo.getPlaylist();
        try
        {
            createPlaylist(playlist.getPlaylistTitle(), callingFragment, gmail);
        }catch (Exception e)
        {
            Utilities.Loggers.postInfoLog("YOUTUBE_PLAYLIST_UPLOAD_ERROR", e.getMessage());
            callingFragment.onErrorComplete();
        }
    }

    private HashMap<String,Object> constructCreatePlaylistRequestBody(String playlistTitle)
    {
        HashMap<String,String> snippet = new HashMap<>();
        HashMap<String,String> status = new HashMap<>();
        snippet.put("title", playlistTitle);
        snippet.put("description", "This playlist is created by using Muzify App :)");
        if(transferInfo.isSharedTransfer()){
            snippet.put("description", "This playlist is created by using Muzi share via Muzify app :) Muzi share code : "+transferInfo.getSelectedPlaylistID());
        }
        status.put("privacyStatus", "private");
        HashMap<String,HashMap> requestBody = new HashMap<>();
        requestBody.put("snippet", snippet);
        requestBody.put("status", status);
        return new HashMap<String,Object>(requestBody);
    }

    private void createPlaylist(String playlistTitle, ReceivePlaylistFragment callingFragment, String gmail) throws InterruptedException {
        HashMap requestBody = constructCreatePlaylistRequestBody(playlistTitle);
        String accessToken = getAccessToken(gmail, WRITE_SCOPE);
        queue.add(apiCaller.doPost(constructUrlWithParams(CREATE_PLAYLIST_URI, getInitialParams(CREATE_PLAYLIST_URI)), accessToken, new VolleyCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                callingFragment.updateItem(ReceiveInfo.PLAYLIST_CREATION_SUCCESS, 0);
                String createdPlaylistID = result.optString("id");
                transferInfo.setDestinationPlaylistURL(createdPlaylistID);
                ArrayList<PlaylistItem> playlistItems = callingFragment.getTransferInfo().getPlaylist().getPlaylistItems();
                try{
                    searchAndUploadSong(createdPlaylistID, playlistItems, callingFragment, gmail);
                }catch (Exception e){
                    Utilities.Loggers.showLongToast(context, "Error occurred while adding songs");
                    callingFragment.onErrorComplete();
                }
            }

            @Override
            public void onError(String result) {
                callingFragment.playlistCreationFailed();
                callingFragment.onErrorComplete();
            }
        }, new JSONObject(requestBody)));
    }

    private HashMap<String,Object> constructInsertSongRequestBody(String playlistID, String videoID){
        HashMap<String,Object> snippet = new HashMap<>();
        HashMap<String,Object> hash = new HashMap<>();
        HashMap<String,String> resourceID = new HashMap<>();
        resourceID.put("kind", "youtube#video");
        resourceID.put("videoId", videoID);
        hash.put("playlistId", playlistID);
        hash.put("resourceId", resourceID);
        snippet.put("snippet", hash);
        return snippet;
    }

    private void searchAndUploadSong(String playlistID, ArrayList<PlaylistItem> playlistItems, ReceivePlaylistFragment callingFragment, String gmail) throws InterruptedException {
        PlaylistItem item = playlistItems.get(currentIndex);
        callingFragment.updateItem(ReceiveInfo.SONG_SEARCHING, currentIndex+1);

        //if it is shared transfer and the existing shared source is youtube, then use the existing video ids
        //else search and upload

        if(transferInfo.isSharedTransfer() && transferInfo.getSharedOriginalSourceCode() == Utilities.ServiceCode.YOUTUBE_MUSIC){
            currentIndex++;
            int index = currentIndex;
            try
            {
                if(item.getSongID() == null || item.getSongID().equals("")){
                    callingFragment.updateItem(ReceiveInfo.SONG_NOT_FOUND, index);
                    transferInfo.addTransferredPlaylistItem(item.getSongName(), item.getSourceSongID(), item.getArtistName(), item.getThumbnailImageURL(), Album.ProcessedBy.NOT_AVAILABLE.getProcessCode(), "", item.getIndex());
                }else{
                    String videoID = item.getSongID();
                    callingFragment.updateItem(ReceiveInfo.SONG_FOUND, index);
                    insertSong(item, callingFragment, playlistItems, playlistID, gmail, index, videoID);
                }

            }catch (Exception e){
                Utilities.Loggers.postInfoLog("YOUTUBE_SEARCH_AND_UPLOAD_VIA_SHARE", "Error : "+e.getMessage());
                callingFragment.onErrorComplete();
            }
            return;
        }

        queue.add(apiCaller.doGet(searchSongUri(item.getArtistName() + " " + item.getSongName()), getAccessToken(gmail, WRITE_SCOPE), new VolleyCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                try {
                    currentIndex++;
                    int index = currentIndex;
                    JSONArray array = result.optJSONArray("items");
                    if(array == null || array.length() == 0){
                        //Song not found
                        callingFragment.updateItem(ReceiveInfo.SONG_NOT_FOUND, index);
                        transferInfo.addTransferredPlaylistItem(item.getSongName(), item.getSourceSongID(), item.getArtistName(), item.getThumbnailImageURL(), Album.ProcessedBy.NOT_AVAILABLE.getProcessCode(), "", item.getIndex());
                    }else{
                        String videoID = array.getJSONObject(0).getJSONObject("id").getString("videoId");
                        callingFragment.updateItem(ReceiveInfo.SONG_FOUND, index);
                        //calling insert playlist with time delay
                        insertSong(item, callingFragment, playlistItems, playlistID, gmail, index, videoID);
                    }

                }catch (Exception e){
                    Utilities.Loggers.postInfoLog("YOUTUBE_SEARCH_AND_UPLOAD", "Error : "+e.getMessage());
                    callingFragment.onErrorComplete();
                }
            }

            @Override
            public void onError(String result) {
                callingFragment.onErrorComplete();
            }
        }));
    }

    private void insertSong(PlaylistItem item, ReceivePlaylistFragment callingFragment, ArrayList<PlaylistItem> playlistItems, String playlistID, String gmail, int index, String videoID) throws Exception
    {
        try{ Thread.sleep(MuzifyConfigs.YOUTUBE_INSERT_SONG_INTERVAL); }catch (Exception ignored){ }
        queue.add(apiCaller.doPost(constructUrlWithParams(INSERT_ITEM_IN_PLAYLIST_URI, getInitialParams(INSERT_ITEM_IN_PLAYLIST_URI)), getAccessToken(gmail, WRITE_SCOPE), new VolleyCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                callingFragment.updateItem(ReceiveInfo.SONG_ADD_SUCCESS, index);
                transferInfo.addTransferredPlaylistItem(item.getSongName(), item.getSourceSongID(), item.getArtistName(), item.getThumbnailImageURL(), Album.ProcessedBy.MUSIC_ARTIST.getProcessCode(), videoID, item.getIndex());
                try{
                    if(playlistItems.size() != currentIndex){
                        searchAndUploadSong(playlistID, playlistItems, callingFragment, gmail);
                    }else{
                        callingFragment.onComplete();
                    }
                }catch (Exception e){
                    callingFragment.onErrorComplete();
                }
            }

            @Override
            public void onError(String result) {
                callingFragment.updateItem(ReceiveInfo.SONG_ADD_FAILED, index);
                transferInfo.addTransferredPlaylistItem(item.getSongName(), item.getSourceSongID(), item.getArtistName(), item.getThumbnailImageURL(), Album.ProcessedBy.ERROR_WHILE_UPLOADING.getProcessCode(), videoID, item.getIndex());
                try{
                    if(playlistItems.size() != currentIndex){
                        searchAndUploadSong(playlistID, playlistItems, callingFragment, gmail);
                    }else{
                        callingFragment.onComplete();
                    }
                }catch (Exception e){
                    callingFragment.onErrorComplete();
                }
            }
        }, new JSONObject(constructInsertSongRequestBody(playlistID, videoID))));
    }








    private void switchToTransferFragment() {
        transferInfo.clearTransfer();
        activity.startActivity(new Intent(activity, HomeScreenActivity.class));
        activity.finish();
    }

    private String getAccessToken(String gmail, String scope) throws InterruptedException {
        gmail = scope+gmail;
       String accessToken = muzifySharedMemory.getGoogleAccessToken(gmail);
       if(accessToken == null){
           AccessToken obj = generateAccessToken(gmail);
           muzifySharedMemory.setGoogleAccessTokenDetails(obj.getAccessToken(), obj.getExpiryTime(), gmail);
          accessToken = obj.getAccessToken();
       }
       return "Bearer "+accessToken;
    }

    private AccessToken generateAccessToken(String gmail) throws InterruptedException {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try  {
                    GoogleTokenResponse tokenResponse =
                            new GoogleAuthorizationCodeTokenRequest(
                                    new NetHttpTransport(),
                                    JacksonFactory.getDefaultInstance(),
                                    Constants.GOOGLE_TOKEN_SERVER_ENCODED_LINK,
                                    Constants.MUZIFY_CLIENT_ID,
                                    Constants.MUZIFY_CLIENT_SECRET,
                                    muzifySharedMemory.getGoogleAuthCode(gmail),
                                    "")  // Specify the same redirect URI that you use with your web
                                    // app. If you don't have a web version of your app, you can
                                    // specify an empty string.
                                    .execute();

                    long expiryTime = (tokenResponse.getExpiresInSeconds() - 30) * 1000;
                    accessToken = tokenResponse.getAccessToken();
                    expiry = expiryTime + System.currentTimeMillis();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
        thread.join();
        return new AccessToken(accessToken, expiry);
    }
}
