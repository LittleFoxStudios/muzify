package com.littlefoxstudios.muzify.accounts;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.android.volley.RequestQueue;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.littlefoxstudios.muzify.Constants;
import com.littlefoxstudios.muzify.MuzifySharedMemory;
import com.littlefoxstudios.muzify.Utilities;
import com.littlefoxstudios.muzify.apis.YoutubeMusicAPI;
import com.littlefoxstudios.muzify.homescreenfragments.history.innercard.ContentLinkInterface;
import com.littlefoxstudios.muzify.homescreenfragments.transfer.YoutubeMusicTransferAuthentication;
import com.littlefoxstudios.muzify.homescreenfragments.transfer.datalayout.TransferInfo;

import java.util.ArrayList;
import java.util.Arrays;

public class YoutubeMusic implements AccountOperationInterface, ContentLinkInterface {

    public static final ArrayList<String> SCOPES = new ArrayList<>( Arrays.asList("https://www.googleapis.com/auth/youtube.readonly", "https://www.googleapis.com/auth/youtube"));

    public static final String API_KEY = Constants.YOUTUBE_API_KEY;

    private GoogleSignInOptions gso;
    private GoogleSignInClient gsc;
    private Activity activity;
    private Context context;
    private final String serviceName = Utilities.MusicService.YOUTUBE_MUSIC.getFormattedServiceName();
    private final int serviceImageID = Utilities.MusicService.YOUTUBE_MUSIC.getLogoDrawableID();

    private int transferCode;
    private YoutubeMusicTransferAuthentication callingActivity;
    MuzifySharedMemory muzifySharedMemory;
    RequestQueue queue;

    public YoutubeMusic(Context context){
        this.context = context;
    }

    public YoutubeMusic(int transferCode, Activity activity, YoutubeMusicTransferAuthentication callingActivity, RequestQueue queue)
    {
        this.transferCode = transferCode;
        this.activity = activity;
        this.context = activity.getApplicationContext();
        this.callingActivity = callingActivity;
        this.muzifySharedMemory = new MuzifySharedMemory(activity);
        this.queue = queue;
    }

    public String getServiceName()
    {
        return serviceName;
    }

    public int getServiceImageID()
    {
        return serviceImageID;
    }

    private Scope getReadOnlyScope()
    {
        return new Scope(SCOPES.get(0));
    }

    private Scope getWriteOnlyScope()
    {
        return new Scope(SCOPES.get(1));
    }

    @Override
    public void signIn() {
        String scope;
       if(TransferInfo.RECEIVER_SIDE == transferCode){
           gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                   .requestProfile()
                   .requestScopes(getWriteOnlyScope())
                   .requestServerAuthCode(Utilities.GOOGLE_SERVER_AUTH)
                   .requestEmail().build();
           scope = YoutubeMusicAPI.WRITE_SCOPE;
       }else{
           gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                   .requestProfile()
                   .requestScopes(getReadOnlyScope())
                   .requestServerAuthCode(Utilities.GOOGLE_SERVER_AUTH)
                   .requestEmail().build();
           scope = YoutubeMusicAPI.READ_SCOPE;
       }
        gsc = GoogleSignIn.getClient(context, gso);

       //sign out if already signed in
        GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(context);
        if(googleSignInAccount != null){
            gsc.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    continueSigningIn(scope);
                }
            });
        }else {
            continueSigningIn(scope);
        }
    }

    private void continueSigningIn(String scope){
        Intent signInIntent = gsc.getSignInIntent();
        signInIntent.setFlags(0);
        muzifySharedMemory.setGoogleScope(scope);
        callingActivity.startActivityForResult(signInIntent, Utilities.ServiceCode.GOOGLE_ACCOUNT);
    }

    @Override
    public void listenStartActivityForResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == Utilities.ServiceCode.GOOGLE_ACCOUNT && resultCode == activity.RESULT_OK){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                task.getResult(ApiException.class);
                GoogleSignInAccount googleAccount = GoogleSignIn.getLastSignedInAccount(context);
                String scope = muzifySharedMemory.getGoogleScope();
                muzifySharedMemory.clearGoogleScope();
                muzifySharedMemory.setGoogleAuthCode(googleAccount.getServerAuthCode(),scope+googleAccount.getEmail());
                callingActivity.switchToNextFragment(googleAccount.getEmail(), googleAccount.getDisplayName(), googleAccount.getPhotoUrl() != null ? googleAccount.getPhotoUrl().toString() : "");

            } catch (ApiException e) {
                Utilities.Loggers.postInfoLog("ERROR", Utilities.ErrorDetailer.UNABLE_TO_LOGIN.getErrorMessage());
            } catch (Exception e) {
                Utilities.Loggers.showLongToast(context, Utilities.ErrorDetailer.USER_DATA_SAVE_ERROR.getErrorMessage());
            }
        }
    }


    @Override
    public void signOut() {

    }

    private void openContent(String link, Context context)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(link));
        intent.setPackage("com.google.android.apps.youtube.music");
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }


    @Override
    public void openPlaylistInApp(String playlistID) {
        if(!isAppInstalled(context)){
            Utilities.Loggers.showLongToast(context, "Please install YouTube Music App on your device first");
            return;
        }
        openContent("https://music.youtube.com/playlist?list="+playlistID, context);
    }

    @Override
    public void openPlaylistItemInApp(String playlistItemID) {
        if(!isAppInstalled(context)){
            Utilities.Loggers.showLongToast(context, "Please install YouTube Music App on your device first");
            return;
        }
        openContent("https://music.youtube.com/watch?v="+playlistItemID, context);
    }

    @Override
    public boolean isAppInstalled(Context context) {
        /*
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo("com.google.android.apps.youtube.music", 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }

         */
        return true;
    }

}
