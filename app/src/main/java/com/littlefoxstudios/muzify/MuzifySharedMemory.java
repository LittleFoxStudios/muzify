package com.littlefoxstudios.muzify;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.littlefoxstudios.muzify.accounts.Spotify;


public class MuzifySharedMemory {
    public static final String SHARED_PREFERENCE = "MuzifySharedPreference";
    public static final String SWITCH_ACCOUNT = "switchAccount";
    public static final String IS_UPDATE_NEEDED = "isUpdateNeeded";
    public static final String NEXT_DOWNLOAD_TIME = "nextDownloadTime";
    public static final String IS_UPLOAD_NEEDED = "isUploadNeeded";
    public static final String IS_DELETE_NEEDED = "isDeleteNeeded";
    public static final String DEFAULT_ACCOUNT = "defaultAccount";
    public static final String IS_HISTORY_AVAILABLE = "isHistoryAvailable";
    public static final String MUZI_SHARE_MAX_ATTEMPTS = "muzifyShareMaxAttempts";
    public static final String MUZI_SHARE_MAX_ATTEMPTS_EXPIRY = MUZI_SHARE_MAX_ATTEMPTS+"Expiry";

    public static final String GOOGLE_ACCESS_TOKEN = "_googleAccessToken";
    public static final String GOOGLE_ACCESS_TOKEN_EXPIRY = "_googleAccessTokenExpiry";
    public static final String GOOGLE_AUTH_CODE = "_googleAuthCode";
    public static final String GOOGLE_SCOPE = "google_scope";

    public static final String SPOTIFY_ACCESS_TOKEN = "spotifyAccessToken";
    public static final String SPOTIFY_ACCESS_TOKEN_EXPIRY = SPOTIFY_ACCESS_TOKEN+"Expiry";
    public static final String SPOTIFY_USER_ID = "spotifyUserID";

    private static final String MUSIC_SERVICE = "musicService";

    private static final String HISTORY_AUTO_UPLOAD = "historyAutoUpload";

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor sharedPreferenceEditor;

    private String getMusicSericeKey(int serviceCode, String accountEmail){
        return MUSIC_SERVICE+"_"+serviceCode+"_"+accountEmail;
    }

    public MuzifySharedMemory(Activity activity)
    {
        sharedPreferences = activity.getSharedPreferences(SHARED_PREFERENCE, Context.MODE_PRIVATE);
        sharedPreferenceEditor = sharedPreferences.edit();
    }

    public void setUpdateNeeded(boolean isUpdateNeeded){
        sharedPreferenceEditor.putBoolean(IS_UPDATE_NEEDED, isUpdateNeeded);
    }

    public void setNextDownloadTime(long nextDownloadTime){
        sharedPreferenceEditor.putLong(NEXT_DOWNLOAD_TIME, nextDownloadTime);
        sharedPreferenceEditor.apply();
    }


    public boolean getUpdateNeeded(){
       return sharedPreferences.getBoolean(IS_UPDATE_NEEDED, true);
    }


    public long getNextDownloadTime(){
        return sharedPreferences.getLong(NEXT_DOWNLOAD_TIME, 0l);
    }

    public String getDefaultAccount(){
        return sharedPreferences.getString(DEFAULT_ACCOUNT, null);
    }

    public void setDefaultAccount(String emailID){
        sharedPreferenceEditor.putString(DEFAULT_ACCOUNT, emailID);
        sharedPreferenceEditor.apply();
    }

    public boolean getIsHistoryAvailable(){
        return sharedPreferences.getBoolean(IS_HISTORY_AVAILABLE, false);
    }

    public void setIsHistoryAvailable(boolean isHistoryAvailable){
        sharedPreferenceEditor.putBoolean(IS_HISTORY_AVAILABLE, isHistoryAvailable);
        sharedPreferenceEditor.apply();
    }

    public long getGoogleAccessTokenExpiry(String gmail) {
        return sharedPreferences.getLong(gmail+GOOGLE_ACCESS_TOKEN_EXPIRY, 0l);
    }

    public void setGoogleAccessTokenDetails(String accessToken, long expiryTime, String gmail) {
        sharedPreferenceEditor.putString(gmail+GOOGLE_ACCESS_TOKEN, accessToken);
        sharedPreferenceEditor.putLong(gmail+GOOGLE_ACCESS_TOKEN_EXPIRY, expiryTime);
        sharedPreferenceEditor.apply();
    }

    public void clearGoogleAccessToken(String gmail){
        sharedPreferenceEditor.putLong(gmail+GOOGLE_ACCESS_TOKEN_EXPIRY, 0);
        sharedPreferenceEditor.apply();
    }

    public String getGoogleAuthCode(String gmail) {
        return sharedPreferences.getString(gmail+GOOGLE_AUTH_CODE, "");
    }

    public void setGoogleAuthCode(String authCode, String gmail) {
        sharedPreferenceEditor.putString(gmail+GOOGLE_AUTH_CODE, authCode);
        sharedPreferenceEditor.apply();
    }

    public String getGoogleAccessToken(String gmail)
    {
       if(System.currentTimeMillis() >= getGoogleAccessTokenExpiry(gmail)){
           return null;
       }
       return sharedPreferences.getString(gmail+GOOGLE_ACCESS_TOKEN, null);
    }

    public String getGoogleScope(){
        return sharedPreferences.getString(GOOGLE_SCOPE, null);
    }

    public void setGoogleScope(String scope){
        sharedPreferenceEditor.putString(GOOGLE_SCOPE, scope);
        sharedPreferenceEditor.apply();
    }

    public void clearGoogleScope(){
        setGoogleScope(null);
    }

    public void setSpotifyAccessToken(String accessToken, long expiry){
        sharedPreferenceEditor.putString(SPOTIFY_ACCESS_TOKEN, accessToken);
        sharedPreferenceEditor.putLong(SPOTIFY_ACCESS_TOKEN_EXPIRY, expiry);
        sharedPreferenceEditor.apply();
    }

    public long getSpotifyAccessTokenExpiry()
    {
        return sharedPreferences.getLong(SPOTIFY_ACCESS_TOKEN_EXPIRY, 0L);
    }

    public String getSpotifyAccessToken()
    {
        if(System.currentTimeMillis() >= getSpotifyAccessTokenExpiry()){
            return null;
        }
        return sharedPreferences.getString(SPOTIFY_ACCESS_TOKEN, null);
    }

    public void setSpotifyUserID(String userID){
        sharedPreferenceEditor.putString(SPOTIFY_USER_ID, userID);
        sharedPreferenceEditor.apply();
    }

    public String getSpotifyUserID(){
        return sharedPreferences.getString(SPOTIFY_USER_ID, null);
    }


    public long getMuziShareAttemptRefreshTime(){
        return sharedPreferences.getLong(MUZI_SHARE_MAX_ATTEMPTS_EXPIRY, 0L);
    }

    public void setMuziShareMaxAttemptsExpiry(){
        sharedPreferenceEditor.putLong(MUZI_SHARE_MAX_ATTEMPTS_EXPIRY, System.currentTimeMillis() + MuzifyConfigs.MUZI_SHARE_REFRESH_TIME);
        sharedPreferenceEditor.apply();
    }

    public void clearMuziShareAttempts(){
        sharedPreferenceEditor.putInt(MUZI_SHARE_MAX_ATTEMPTS, MuzifyConfigs.MUZI_SHARE_MAX_ATTEMPTS);
        sharedPreferenceEditor.apply();
    }

    public int getMuziShareRemainingAttempts(){
        int attempts = sharedPreferences.getInt(MUZI_SHARE_MAX_ATTEMPTS, MuzifyConfigs.MUZI_SHARE_MAX_ATTEMPTS);
        long rt = getMuziShareAttemptRefreshTime();
        if(System.currentTimeMillis() >= getMuziShareAttemptRefreshTime()){
            clearMuziShareAttempts();
            return MuzifyConfigs.MUZI_SHARE_MAX_ATTEMPTS;
        }else{
            return attempts;
        }
    }


    public int muziShareAttemptUsed(){
        int attempts = getMuziShareRemainingAttempts();
        attempts = attempts > 0 ? attempts-1 : 0;
        sharedPreferenceEditor.putInt(MUZI_SHARE_MAX_ATTEMPTS, attempts);
        sharedPreferenceEditor.apply();
        setMuziShareMaxAttemptsExpiry();
        return attempts;
    }

    public String getMusicServiceConnectedAccountEmail(Utilities.MusicService service, String accountEmail){
        return sharedPreferences.getString(getMusicSericeKey(service.getCode(), accountEmail), null);
    }

    public boolean checkMusicServiceEnabled(Utilities.MusicService service, String accountEmail) {
         return getMusicServiceConnectedAccountEmail(service, accountEmail) != null;
    }

    public void enableMusicService(Utilities.MusicService service, String accountEmail, String connectedAccountEmail){
        sharedPreferenceEditor.putString(getMusicSericeKey(service.getCode(), accountEmail), connectedAccountEmail);
        sharedPreferenceEditor.apply();
    }

    public void enableSwitchAccount()
    {
        sharedPreferenceEditor.putBoolean(SWITCH_ACCOUNT, true);
        sharedPreferenceEditor.apply();
    }

    public void disableSwitchAccount()
    {
        sharedPreferenceEditor.putBoolean(SWITCH_ACCOUNT, false);
        sharedPreferenceEditor.apply();
    }

    public boolean isAccountSwitchNeeded()
    {
        return sharedPreferences.getBoolean(SWITCH_ACCOUNT, false);
    }

    public boolean autoUploadHistorySelected(String emailID)
    {
        return sharedPreferences.getBoolean(HISTORY_AUTO_UPLOAD+"_"+emailID, true);
    }

    public boolean toggleHistoryAutoUpload(String emailID)
    {
        boolean res = !autoUploadHistorySelected(emailID);
        sharedPreferenceEditor.putBoolean(HISTORY_AUTO_UPLOAD+"_"+emailID, res);
        sharedPreferenceEditor.apply();
        return res;
    }
}
