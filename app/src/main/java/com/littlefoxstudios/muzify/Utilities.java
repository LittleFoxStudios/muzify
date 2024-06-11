package com.littlefoxstudios.muzify;

import static android.view.View.GONE;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.button.MaterialButton;
import com.littlefoxstudios.muzify.accounts.Spotify;
import com.littlefoxstudios.muzify.accounts.YoutubeMusic;
import com.littlefoxstudios.muzify.homescreenfragments.transfer.TransferAuthenticationInterface;
import com.littlefoxstudios.muzify.homescreenfragments.transfer.datalayout.ReceivePlaylistFragment;
import com.littlefoxstudios.muzify.internet.InternetTestingViewModel;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class Utilities {

    public static boolean isTestMode = false;

    public static final String GOOGLE_SERVER_AUTH = Constants.MUZIFY_CLIENT_ID;

    public static class ServiceCode{
        public static final int GOOGLE_ACCOUNT = 1000;
        public static final int YOUTUBE_MUSIC = 1001;
        public static final int SPOTIFY = 1002;
        public static final int MUZI_SHARE = 1003;
    }

    private enum ErrorCode
    {
        IE001("Unable to store data"),
        IE002("Unable to login due to some error"),
        IE003("Unable to load history"),
        IE004("Unable to upload playlist to cloud"),
        IE005("Unable to create share code");

        private String errorMessage;
        ErrorCode(String errorMessage){
            this.errorMessage = errorMessage;
        }

        public String getErrorMessage(){
            return this.errorMessage;
        }
    }

    public enum ErrorDetailer
    {
        USER_DATA_SAVE_ERROR(ErrorCode.IE001),
        UNABLE_TO_LOGIN(ErrorCode.IE002),
        UNABLE_TO_LOAD_HISTORY(ErrorCode.IE003),
        UNABLE_TO_UPLOAD_CARD(ErrorCode.IE004),
        UNABLE_TO_CREATE_SHARE_CODE(ErrorCode.IE005);

        private String errorMessage;

        ErrorDetailer(ErrorCode errorCode){
          this.errorMessage = errorCode.getErrorMessage();
        }

        public String getErrorMessage(){
            return this.errorMessage;
        }
    }

    public enum MusicService
    {
        //don't forget to add in getAllService()
        YOUTUBE_MUSIC(ServiceCode.YOUTUBE_MUSIC, YoutubeMusic.class.getSimpleName().toUpperCase(), R.drawable.youtube_music_icon, "YouTube Music", false, R.color.orange),
        SPOTIFY(ServiceCode.SPOTIFY, Spotify.class.getSimpleName().toUpperCase(), R.drawable.spotify_icon, "Spotify", false, R.color.green),
        MUZI_SHARE(ServiceCode.MUZI_SHARE, "MUZISHARE", R.drawable.muzi_share, "Muzi Share", true, R.color.nav_btn_selected);

        private int serviceCode;
        private String serviceName;
        int logoDrawableID;
        String accountNameText;
        String formattedServiceName;
        private boolean isConsumerAccess;
        int themeColor;

        MusicService(int serviceCode, String serviceName, int logoDrawableID, String formattedServiceName, boolean isConsumerAccess, int themeColor){
            this.serviceCode = serviceCode;
            this.serviceName = serviceName;
            this.logoDrawableID = logoDrawableID;
            this.accountNameText = formattedServiceName+" Account";
            this.formattedServiceName = formattedServiceName;
            this.isConsumerAccess = isConsumerAccess;
            this.themeColor = themeColor;
        }

        public static ArrayList<MusicService> getAllService()
        {
            ArrayList<MusicService> list = new ArrayList<>();
            list.add(YOUTUBE_MUSIC);
            list.add(SPOTIFY);
            list.add(MUZI_SHARE);
            return list;
        }

        public boolean isConsumerAccess(){
            return this.isConsumerAccess;
        }


        public String getFormattedServiceName()
        {
            return this.formattedServiceName;
        }

        public static MusicService getMusicServiceFromFormattedName(String formattedServiceName){
            for(MusicService service : values()){
                if( service.formattedServiceName.equals(formattedServiceName)){
                    return service;
                }
            }
            return null;
        }


        public static MusicService getMusicServiceFromCode(int serviceCode){
            for(MusicService service : values()){
                if( service.serviceCode == serviceCode){
                    return service;
                }
            }
            return null;
        }

        public String getAccountString(){
            return accountNameText;
        }

        public int getLogoDrawableID() { return logoDrawableID; }

        public int getCode()
        {
            return serviceCode;
        }

        public int getThemeColor(Context context){
            return context.getResources().getColor(this.themeColor);
        }

    }


    public static String getColoredSpanned(String text, String color) {
        String input = "<font color=" + color + ">" + text + "</font>";
        return input;
    }

    public static boolean isEmpty(Object data)
    {
        if(data == null) {
            return true;
        }
        if(data instanceof String) {
            return ((String) data).equals("");
        }
        if(data instanceof ArrayList) {
            return ((ArrayList) data).size() == 0;
        }
        if(data instanceof HashMap) {
            return ((HashMap) data).isEmpty();
        }
        if(data instanceof Hashtable) {
            return ((Hashtable) data).isEmpty();
        }
        return true;
    }

    public static boolean isNotEmpty(Object data)
    {
        return !isEmpty(data);
    }

    public static int getRandomNumber(int startRange, int endRange)
    {
        Random random = new Random();
        int rand = startRange-1;
        while (true){
            rand = random.nextInt(endRange+1);
            if(rand != 0 && rand >= startRange) break;
        }
        return rand;
    }

    public static int getRandomDrawableIDForThumbnail()
    {
        int number = Utilities.getRandomNumber(1,8);
        switch (number)
        {
            case 1 : return R.drawable.music_icon_1;
            case 2 : return R.drawable.music_icon_2;
            case 3 : return R.drawable.music_icon_3;
            case 4 : return R.drawable.music_icon_4;
            case 5 : return R.drawable.music_icon_5;
            case 6 : return R.drawable.music_icon_6;
            case 7 : return R.drawable.music_icon_7;
            case 8 : return R.drawable.music_icon_8;
        }
        return R.drawable.music_icon_1;
    }


    public static class Loggers
    {
        private static void showToast(Context context, String toastMessage, int duration)
        {
            Toast.makeText(context, toastMessage, duration).show();
        }
        public static void showLongToast(Context context, String toastMessage){
            showToast(context, toastMessage, Toast.LENGTH_LONG);
        }
        public static void showShortToast(Context context, String toastMessage){
            showToast(context, toastMessage, Toast.LENGTH_SHORT);
        }

        public static void postErrorLog(String message){
            postInfoLog("FATAL_ERROR_EXCEPTION", message);
        }

        public static void postInfoLog(String logTag, String message)
        {
            Log.i("INFO_"+logTag, message);
        }
    }

    public static String convertListToString(ArrayList list){
        String s = "";
        if(list.size() == 0){
            return "";
        }
        for(int i=0;i<list.size();i++){
            s += list.get(i);
            if(i != list.size()-1){
                s += ",";
            }
        }
        return s;
    }

    public static ArrayList convertStringToList(String str){
        if(str == null || str.equals("")){
            return new ArrayList();
        }
        ArrayList<String> list = new ArrayList(Arrays.asList(str.split("\\s*,\\s*")));
        ArrayList result = new ArrayList<>();
        for(String item : list){
            try{
                //some items will come up as string.. so catching number format exception and processing them separately
                result.add(Long.parseLong(item));
            }catch(Exception e){
                result.add(item);
            }
        }
        return result;
    }

    public static class Encryptor{
        public String encryptString(String data){
            //TODO
            return data;
        }
        public String decryptString(String data){
            //TODO
            return data;
        }
    }

    public static String convertTimeStampToDate(long timeStamp){
        //return new java.text.SimpleDateFormat("MM/dd/yyyy").format(new java.util.Date (timeStamp));
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM YYY, HH:mm");
        return dateFormat.format(timeStamp);
    }

    public static String getAlphaNumericString(int n)
    {
        // length is bounded by 256 Character
        byte[] array = new byte[256];
        new Random().nextBytes(array);

        String randomString
                = new String(array, Charset.forName("UTF-8"));

        // Create a StringBuffer to store the result
        StringBuffer r = new StringBuffer();

        // Append first 20 alphanumeric characters
        // from the generated random String into the result
        for (int k = 0; k < randomString.length(); k++) {

            char ch = randomString.charAt(k);

            if (((ch >= 'a' && ch <= 'z')
                    || (ch >= 'A' && ch <= 'Z')
                    || (ch >= '0' && ch <= '9'))
                    && (n > 0)) {

                r.append(ch);
                n--;
            }
        }

        // return the resultant string
        return r.toString().toUpperCase();
    }

    public static String encodeUserEmail(String userEmail) {
        return userEmail.replace(".", ",");
    }

    public static String[] convertSetToStringArray(Set<String> setOfString)
    {

        // Create String[] of size of setOfString
        String[] arrayOfString = new String[setOfString.size()];

        // Copy elements from set to string array
        // using advanced for loop
        int index = 0;
        for (String str : setOfString)
            arrayOfString[index++] = str;

        // return the formed String[]
        return arrayOfString;
    }



    public static class Alert
    {
        private Activity activity;
        private AlertDialog alertDialog;
        private int chosenLayout;
        private View mView;
        private String code;
        private boolean buttonClickedOnce;
        public String tempStringData;
        public boolean tempFlag = false;

        public static final String UPDATE_AVAILABLE = "updateAvailable";
        public static final String NO_INTERNET = "noInternet";
        public static final String LOADING = "loading";
        public static final String INFO = "info";
        public static final String ERROR = "error";
        public static final String CHECK_TICK = "checkTick";
        public static final String MUSIC_SERVICE_ACCESS_REQUEST = "musicServiceAccessRequest";
        public static final String UPLOAD_HISTORY_ONLY = "uploadHistoryOnly";

        //public static final String ACCOUNT_BLOCKED = "accountBlocked";

        private int setChosenLayout(String code) {
            switch (code)
            {
                case MUSIC_SERVICE_ACCESS_REQUEST: return R.layout.app_alert_dialog_for_music_request_access;
                default: return R.layout.app_alert_dialog;
            }
        }

        public Alert(Activity activity, String code) {
            this.activity = activity;
            this.code = code;
            this.chosenLayout = setChosenLayout(code);
            this.mView = activity.getLayoutInflater().inflate(chosenLayout, null);
        }

        //FOR UPDATE

        public void uploadFinished() throws Exception
        {
            if(!code.equals(UPDATE_AVAILABLE)){
                throw new Exception("Method does not support the given layout");
            }
            ConstraintLayout closeButtonLayout = (ConstraintLayout) mView.findViewById(R.id.appAlertDialogCloseButtonLayout);
            ConstraintLayout uploadingDataLayout = (ConstraintLayout) mView.findViewById(R.id.appAlertDialogUploadLayout);
            uploadingDataLayout.setVisibility(GONE);
            closeButtonLayout.setVisibility(View.VISIBLE);
        }

        public void startUpdateDialog(boolean isUploadNeeded){
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            TextView appTitle = (TextView) mView.findViewById(R.id.appAlertDialogTitle);
            TextView appMessage = (TextView) mView.findViewById(R.id.appAlertDialogMessage);
            ImageView appImage = (ImageView) mView.findViewById(R.id.appAlertDialogImage);
            MaterialButton appButton = (MaterialButton) mView.findViewById(R.id.appAlertDialogCloseButton);
            ConstraintLayout closeButtonLayout = (ConstraintLayout) mView.findViewById(R.id.appAlertDialogCloseButtonLayout);
            ConstraintLayout uploadingDataLayout = (ConstraintLayout) mView.findViewById(R.id.appAlertDialogUploadLayout);

            appTitle.setText(R.string.update_available);
            appMessage.setText(R.string.update_available_message);
            appImage.setImageResource(R.drawable.app_update_image);
            appButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    activity.finishAndRemoveTask();
                }
            });

            if(isUploadNeeded)
            {
                closeButtonLayout.setVisibility(GONE);
                uploadingDataLayout.setVisibility(View.VISIBLE);
            }

            builder.setView(mView);
            builder.setCancelable(false);
            alertDialog = builder.create();
            alertDialog.show();
        }

        //END OF UPDATE

        //FOR INTERNET CHECK
        public void showNoInternetDialog(InternetTestingViewModel internetTestingViewModel){
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            TextView appTitle = (TextView) mView.findViewById(R.id.appAlertDialogTitle);
            TextView appMessage = (TextView) mView.findViewById(R.id.appAlertDialogMessage);
            ImageView appImage = (ImageView) mView.findViewById(R.id.appAlertDialogImage);
            appTitle.setText(R.string.no_internet);
            appMessage.setText(R.string.no_internet_message);
            appImage.setImageResource(R.drawable.wifi_off);
            MaterialButton appButton = (MaterialButton) mView.findViewById(R.id.appAlertDialogCloseButton);
            if(internetTestingViewModel != null){
                appButton.setText("REFRESH");
            }
            appButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                  if(internetTestingViewModel != null){
                      if (internetTestingViewModel.internetConnectionAvailable(2000)) {
                          internetConnected();
                      }else{
                          Loggers.showShortToast(activity.getApplicationContext(), "Not connected! Try again");
                      }
                  }
                }
            });


            if(mView.getParent() == null){
                builder.setView(mView);
                builder.setCancelable(false);
                alertDialog = builder.create();
            }
            alertDialog.show();
        }

        public void internetConnected(){
            stopDialog();
        }

        //EMD OF INTERNET CHECK

        //START OF LOADING ALERT

        public void startLoading(String title, String message){
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            TextView appTitle = (TextView) mView.findViewById(R.id.appAlertDialogTitle);
            TextView appMessage = (TextView) mView.findViewById(R.id.appAlertDialogMessage);

            appTitle.setText(title);
            appMessage.setText(message);
            showLoadingInsteadOfImage();
            showOnlyTitleAndMessage();

            if(mView.getParent() == null){
                builder.setView(mView);
                builder.setCancelable(false);
                alertDialog = builder.create();
            }
            alertDialog.show();
        }

        //END OF LOADING ALERT

        //START OF INFO ALERT

        public void showInfoResult(String type, String title, String message, AlertButtonInterface callingFragment){
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            TextView appTitle = (TextView) mView.findViewById(R.id.appAlertDialogTitle);
            TextView appMessage = (TextView) mView.findViewById(R.id.appAlertDialogMessage);
            ImageView appImage = (ImageView) mView.findViewById(R.id.appAlertDialogImage);
            MaterialButton appButton = (MaterialButton) mView.findViewById(R.id.appAlertDialogCloseButton);
            ConstraintLayout closeButtonLayout = (ConstraintLayout) mView.findViewById(R.id.appAlertDialogCloseButtonLayout);
            ConstraintLayout uploadingDataLayout = (ConstraintLayout) mView.findViewById(R.id.appAlertDialogUploadLayout);
            uploadingDataLayout.setVisibility(GONE);
            appButton.setText("OK");
            appTitle.setText(title);
            appMessage.setText(message);
            switch (type) {
                case INFO:
                    appImage.setImageResource(R.drawable.ic_baseline_info_24);
                    break;
                case ERROR:
                    appImage.setImageResource(R.drawable.error_icon);
                    break;
                case CHECK_TICK:
                    appImage.setImageResource(R.drawable.check_tick);
                    break;
            }
            appButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    callingFragment.infoAlertButtonClicked();
                }
            });

            if(mView.getParent() == null){
                builder.setView(mView);
                builder.setCancelable(false);
                alertDialog = builder.create();
            }

            alertDialog.show();
        }

        //END OF INFO ALERT


        private void showOnlyTitleAndMessage()
        {
            MaterialButton appButton = (MaterialButton) mView.findViewById(R.id.appAlertDialogCloseButton);
            ConstraintLayout closeButtonLayout = (ConstraintLayout) mView.findViewById(R.id.appAlertDialogCloseButtonLayout);
            ConstraintLayout uploadingDataLayout = (ConstraintLayout) mView.findViewById(R.id.appAlertDialogUploadLayout);
            appButton.setVisibility(GONE);
            closeButtonLayout.setVisibility(GONE);
            uploadingDataLayout.setVisibility(GONE);
        }

        private void showLoadingInsteadOfImage()
        {
            ImageView appImage = (ImageView) mView.findViewById(R.id.appAlertDialogImage);
            appImage.setVisibility(GONE);
            ProgressBar progressBar = (ProgressBar) mView.findViewById(R.id.appAlertDialogLoadingProgress);
            progressBar.setVisibility(View.VISIBLE);
        }

        public void stopDialog(){
           if(alertDialog != null){
               alertDialog.dismiss();
           }
        }

        public void showServiceAccessRequestDialog(MusicService service, String title, String body, String hint, TransferAuthenticationInterface callingActivity) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            TextView appTitle = (TextView) mView.findViewById(R.id.musicRequestTitle);
            TextView appMessage = (TextView) mView.findViewById(R.id.musicRequestDetails);
            ImageView appImage = (ImageView) mView.findViewById(R.id.musicRequestSourceImage);
            MaterialButton appButton = (MaterialButton) mView.findViewById(R.id.musicServiceButton);
            EditText emailID = (EditText) mView.findViewById(R.id.musicRequestEmail);
            MaterialButton cancelButton = (MaterialButton) mView.findViewById(R.id.musicServiceCancelButton);

            appTitle.setText(title);
            appMessage.setText(body);
            appImage.setImageResource(service.getLogoDrawableID());
            emailID.setHint(hint);

            appButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(validEmail(emailID.getText().toString())){
                        if(buttonClickedOnce){
                            Loggers.showShortToast(activity.getApplicationContext(), "Please wait..");
                            return;
                        }
                        buttonClickedOnce = true;
                        tempStringData = emailID.getText().toString();
                        callingActivity.handleButtonOperation(TransferAuthenticationInterface.REQUEST_ACCESS_CLICKED);
                    }else{
                        Loggers.showLongToast(activity.getApplicationContext(), "Please enter a valid email");
                    }
                }
            });

            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    callingActivity.handleButtonOperation(TransferAuthenticationInterface.CANCEL_REQUEST_CLICKED);
                }
            });

            if(mView.getParent() == null){
                builder.setView(mView);
                builder.setCancelable(false);
                alertDialog = builder.create();
            }

            alertDialog.show();
        }
    }


    public static boolean validEmail(String email)
    {
        if (email == null || email.equals(""))
            return false;
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        return pat.matcher(email).matches();
    }



    static void fadingAnimation(String text, TextView block, int time, int animID, Context context){
        fadingAnimation(text, block, time, animID, context, null);
    }

    static void fadingAnimation(String text, TextView block, int time, int animID, Context context, Long duration){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!text.equals("")){
                    block.setText(text);
                }
                Animation anim = AnimationUtils.loadAnimation(context, animID);
                if(duration != null){
                    anim.setDuration(duration);
                }
                anim.setRepeatMode(Animation.INFINITE);
                anim.setRepeatMode(Animation.REVERSE);
                block.startAnimation(anim);
            }
        }, time);
        if(text.equals("")){
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    block.setText(text);
                }
            }, time+1000);
        }
    }

    public static void fadeIn(String text, TextView block, int time, Context context){
        fadeIn(text, block, time, context, null);
    }

    public static void fadeIn(String text, TextView block, int time, Context context, Long duration){
        fadingAnimation(text, block, time, R.anim.fade, context, duration);
    }

    public static void fadeOut(TextView block, int time, Context context){
        fadingAnimation("", block, time, R.anim.fade_out, context);
    }

    public static void vibrate(Context context, int seconds){
        Vibrator vibe = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibe.vibrate(seconds * 1000);
    }

    public static String getTimerString(long millis){
         return String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
    }

    public static long[] convertToPrimitiveLongArray(ArrayList<Long> list){
        long[] result = new long[list.size()];
        for(int i=0;i<result.length;i++){
            result[i] = list.get(i);
        }
        return result;
    }

    public static String validateNullString(String text){
        if(text == null){
            return "";
        }
        return text;
    }

    public static void setClipboard(Context context, String text) {
        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(text);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("MuzifyAppClip", text);
            clipboard.setPrimaryClip(clip);
        }
    }

}