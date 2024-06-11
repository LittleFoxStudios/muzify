package com.littlefoxstudios.muzify.accounts;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;


import com.android.volley.RequestQueue;
import com.littlefoxstudios.muzify.Constants;
import com.littlefoxstudios.muzify.MuzifySharedMemory;
import com.littlefoxstudios.muzify.Utilities;

import com.littlefoxstudios.muzify.apis.SpotifyAPI;
import com.littlefoxstudios.muzify.homescreenfragments.history.innercard.ContentLinkInterface;
import com.littlefoxstudios.muzify.homescreenfragments.transfer.SpotifyTransferAuthentication;
import com.littlefoxstudios.muzify.homescreenfragments.transfer.datalayout.TransferInfo;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

public class Spotify implements AccountOperationInterface, ContentLinkInterface {



    private int transferCode;
    private SpotifyTransferAuthentication callingActivity;
    private MuzifySharedMemory muzifySharedMemory;
    private RequestQueue queue;
    private Activity activity;
    private Context context;

    public Spotify(Context context){
        this.context = context;
    }

    public Spotify(int transferCode, Activity activity, SpotifyTransferAuthentication callingActivity, RequestQueue queue)
    {
        this.transferCode = transferCode;
        this.activity = activity;
        this.context = activity.getApplicationContext();
        this.callingActivity = callingActivity;
        this.muzifySharedMemory = new MuzifySharedMemory(activity);
        this.queue = queue;
    }

    private String getScope()
    {
        if(TransferInfo.RECEIVER_SIDE == transferCode){
            return "playlist-modify-private,user-read-email";
        }else{
            return "playlist-read-private,playlist-read-collaborative,user-read-private,user-read-email,user-library-read";
        }
    }

    private void authenticateSpotify() {
        AuthorizationRequest.Builder builder = new AuthorizationRequest.Builder(Constants.SPOTIFY_CLIENT_ID, AuthorizationResponse.Type.TOKEN ,Constants.SPOTIFY_REDIRECT_URI);
        builder.setScopes(new String[]{getScope()});
        AuthorizationRequest request = builder.build();
        //AuthorizationClient.openLoginActivity(activity, Utilities.ServiceCode.SPOTIFY, request);
        Intent intent = AuthorizationClient.createLoginActivityIntent(activity, request);
        callingActivity.startActivityForResult(intent, Utilities.ServiceCode.SPOTIFY);
    }


    @Override
    public void signIn() {
        authenticateSpotify();
    }

    @Override
    public void signOut() {

    }

    @Override
    public void listenStartActivityForResult(int requestCode, int resultCode, Intent data) {
        // Check if result comes from the correct activity
        if (requestCode == Utilities.ServiceCode.SPOTIFY) {
            AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, data);

            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    muzifySharedMemory.setSpotifyAccessToken(response.getAccessToken(), System.currentTimeMillis() + (response.getExpiresIn() * 1000L));
                    initializeUserDetails(response.getAccessToken());
                    break;

                // Auth flow returned an error
                case ERROR:
                    Utilities.Loggers.showLongToast(context, "Unable to login - Please make sure your Spotify App is up to date!");
                    break;

                // Most likely auth flow was cancelled
                default:
                   Utilities.Loggers.showLongToast(context, "Unable to access Spotify service");
            }
        }
    }

    private void initializeUserDetails(String accessToken)
    {
        SpotifyAPI.getUserDetails(context, queue, accessToken, callingActivity, muzifySharedMemory);
    }

    @Override
    public void openPlaylistInApp(String playlistID) {
        if(!isAppInstalled(context)){
            Utilities.Loggers.showLongToast(context, "Please install Spotify app on your device first!");
            openSpotifyAndroidMarket(context);
            return;
        }
        openContent("https://open.spotify.com/playlist/"+playlistID, context);
    }

    private static void openContent(String spotifyContent, Context context){
        Utilities.Loggers.postInfoLog("CONTENT_LINK", spotifyContent);
        final String branchLink = "https://spotify.link/content_linking?~campaign=" + context.getPackageName() + "&$deeplink_path=" + spotifyContent + "&$fallback_url=" + spotifyContent;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(branchLink));
        context.startActivity(intent);
    }

    private String filterID(String id){
       try{
           String[] splitID = id.split(":");
           return splitID[2];
       }catch (Exception e){
           return id;
       }
    }

    @Override
    public void openPlaylistItemInApp(String playlistItemID) {
        if(!isAppInstalled(context)){
            Utilities.Loggers.showLongToast(context, "Please install Spotify app on your device first!");
            openSpotifyAndroidMarket(context);
            return;
        }
        openContent("https://open.spotify.com/track/"+filterID(playlistItemID), context);
    }

    @Override
    public boolean isAppInstalled(Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo("com.spotify.music", 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private static void openSpotifyAndroidMarket(Context context)
    {
        final String branchLink = Uri.encode("https://spotify.link/content_linking?~campaign=" + context.getPackageName());
        final String appPackageName = "com.spotify.music";
        final String referrer = "_branch_link=" + branchLink;

        try {
            Uri uri = Uri.parse("market://details")
                    .buildUpon()
                    .appendQueryParameter("id", appPackageName)
                    .appendQueryParameter("referrer", referrer)
                    .build();
            context.startActivity(new Intent(Intent.ACTION_VIEW, uri));
        } catch (android.content.ActivityNotFoundException ignored) {
            Uri uri = Uri.parse("https://play.google.com/store/apps/details")
                    .buildUpon()
                    .appendQueryParameter("id", appPackageName)
                    .appendQueryParameter("referrer", referrer)
                    .build();
            context.startActivity(new Intent(Intent.ACTION_VIEW, uri));
        }
    }
}
