package com.littlefoxstudios.muzify;

import static com.littlefoxstudios.muzify.Utilities.fadeIn;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.littlefoxstudios.muzify.accounts.MuzifyAccount;
import com.littlefoxstudios.muzify.datastorage.CloudStorage;
import com.littlefoxstudios.muzify.datastorage.LocalStorage;
import com.littlefoxstudios.muzify.datastorage.MuzifyViewModel;
import com.squareup.picasso.Picasso;


import java.util.List;

public class WelcomeActivity extends AppCompatActivity {

    TextView block1;
    TextView block2;
    TextView block3;
    SignInButton signInButton;

    MuzifyAccount account;
    GoogleSignInClient gsc;
    GoogleSignInOptions gso;

    MuzifyViewModel muzifyViewModel;
    MuzifySharedMemory muzifySharedMemory;

    TunePlayer tunePlayer;
    boolean changeAccountEnabled = false;

    private Uri uri;
    private String deeplinkShareCode;

    @Override
    protected void onResume() {
        super.onResume();
        //handle on resume deeplink?
    }

    private void handleMuzishareDeeplink()
    {
        uri = getIntent().getData();
        if(uri != null){
            String code = uri.getQueryParameter("code");
            if(code == null){
                return;
            }
            deeplinkShareCode = code;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        initialize();
        handleMuzishareDeeplink();
        LiveData<List<LocalStorage.UserData>> allUserData = muzifyViewModel.getAllUserData();
        allUserData.observe(this, new Observer<List<LocalStorage.UserData>>() {
            @Override
            public void onChanged(@Nullable List<LocalStorage.UserData> userData){
                allUserData.removeObserver(this);
                if(userData == null || userData.size() == 0 || changeAccountEnabled){
                    //no user present - new user flow
                    startWelcomeNewUserAnimation();
                    muzifySharedMemory.setIsHistoryAvailable(false);
                    handleNewUserFlow();
                }else{
                   if(muzifySharedMemory.getDefaultAccount() == null){
                       handleUserSelected(userData.get(0).getEmailID());
                   }else{
                       handleUserSelected(muzifySharedMemory.getDefaultAccount());
                   }
                }
            }
        });
    }


    void initialize()
    {
        block1 = findViewById(R.id.txt_block_1);
        block2 = findViewById(R.id.txt_block_2);
        block3 = findViewById(R.id.txt_block_3);
        signInButton = findViewById(R.id.userSignInButton);
        account = new MuzifyAccount();
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestProfile()
                .requestServerAuthCode(Utilities.GOOGLE_SERVER_AUTH)
                .requestEmail().build();
        gsc = GoogleSignIn.getClient(this, gso);
        muzifyViewModel = new MuzifyViewModel(getApplication());
        muzifySharedMemory = new MuzifySharedMemory(WelcomeActivity.this);
        changeAccountEnabled = muzifySharedMemory.isAccountSwitchNeeded();
        muzifySharedMemory.disableSwitchAccount();
    }


    void startWelcomeNewUserAnimation() {
        /*
        fadeIn("WELCOME", block1, 1000, getApplicationContext());
        fadeIn("TO", block2, 2000, getApplicationContext());
        fadeIn("MUZIFY", block3, 4000, getApplicationContext());

        fadeOut(block1, 5000, getApplicationContext());
        fadeOut(block2, 6000, getApplicationContext());
        fadeOut(block3, 7000, getApplicationContext());

        fadeIn("SIGN IN", block1, 9000, getApplicationContext());
        fadeIn("TO", block2, 10000, getApplicationContext());
        fadeIn("START!", block3, 13000, getApplicationContext());

        fadeOut(block1, 11000, getApplicationContext());
        fadeOut(block2, 12000, getApplicationContext());
        */
        if(MuzifyConfigs.INCLUDE_STARTUP_MUSIC){
            tunePlayer = new TunePlayer(getApplicationContext(), R.raw.series_x_startup);
            tunePlayer.playTune();
        }
        fadeIn(Constants.APP_NAME, block3, 1000, getApplicationContext(), 5000l);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                animateSignInButton();
            }
        }, 7000);
    }

    void animateSignInButton()
    {
        signInButton.setVisibility(View.VISIBLE);
        signInButton.setColorScheme(SignInButton.COLOR_DARK);
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade);
        animation.setDuration(1000);
        signInButton.startAnimation(animation);
    }

    void handleNewUserFlow(){
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
    }


    void handleUserSelected(String emailID){
        muzifySharedMemory.setDefaultAccount(emailID);
        Intent i = new Intent(WelcomeActivity.this, HomeScreenActivity.class);
        i.putExtra("emailID", emailID);
        i.putExtra("shareCode", deeplinkShareCode);
        startActivity(i);
        finish();
    }


    public void signIn()
    {
        GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if(googleSignInAccount != null){
            gsc.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    continueSigningIn();
                }
            });
        }else {
            continueSigningIn();
        }
    }

    private void continueSigningIn(){
        Intent signInIntent = gsc.getSignInIntent();
        signInIntent.setFlags(0);
        startActivityForResult(signInIntent, Utilities.ServiceCode.GOOGLE_ACCOUNT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Utilities.ServiceCode.GOOGLE_ACCOUNT && resultCode == RESULT_OK){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {

                task.getResult(ApiException.class);
                GoogleSignInAccount googleAccount = GoogleSignIn.getLastSignedInAccount(this);
                account.setEmailID(googleAccount.getEmail());
                account.setUserName(googleAccount.getDisplayName());
                account.setProfilePictureURL(googleAccount.getPhotoUrl() != null ? googleAccount.getPhotoUrl().toString() : "0");

                Utilities.Loggers.postInfoLog("GOOGLE_SIGN_IN", "AUTHCODE xx: "+googleAccount.getServerAuthCode());

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
                                            googleAccount.getServerAuthCode(),
                                            "")  // Specify the same redirect URI that you use with your web
                                            // app. If you don't have a web version of your app, you can
                                            // specify an empty string.
                                            .execute();

                            Utilities.Loggers.postInfoLog("GOOGLE_SIGN_IN", "Access token : "+tokenResponse.getAccessToken());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                thread.start();

                loginSuccess();
            } catch (ApiException e) {
                Utilities.Loggers.postInfoLog("ERROR", Utilities.ErrorDetailer.UNABLE_TO_LOGIN.getErrorMessage());
            } catch (Exception e) {
                Utilities.Loggers.showLongToast(this, Utilities.ErrorDetailer.USER_DATA_SAVE_ERROR.getErrorMessage());
            }
        }
    }

    void loginSuccess() throws Exception {

        LiveData<List<LocalStorage.UserData>> userData = muzifyViewModel.getUserDataForSpecificUser(account.getEmailID());
        userData.observe(this, new Observer<List<LocalStorage.UserData>>() {
            @Override
            public void onChanged(List<LocalStorage.UserData> data) {
                userData.removeObserver(this);
                boolean existingUser = data.size() == 1;
                try{
                    if(existingUser){
                        muzifyViewModel.update(new LocalStorage.UserData(account.getEmailID(), account.getUserName(), account.getProfilePictureURL()));
                        CloudStorage.updateAccount(account);
                    }else{
                        muzifyViewModel.insert(new LocalStorage.UserData(account.getEmailID(), account.getUserName(), account.getProfilePictureURL()));
                        CloudStorage.addNewAccount(account, getApplicationContext());
                    }
                    startActivity(new Intent(WelcomeActivity.this, HomeScreenActivity.class));
                    //finish();
                    handleUserSelected(account.getEmailID());
                }catch (Exception e){
                    finish();
                }
            }
        });
    }

    @Override
    protected void onStop(){
        super.onStop();
        if(tunePlayer != null){
           tunePlayer.stopTune();
        }
    }

}