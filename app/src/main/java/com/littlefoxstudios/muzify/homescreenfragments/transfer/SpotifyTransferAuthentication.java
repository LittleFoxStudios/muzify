package com.littlefoxstudios.muzify.homescreenfragments.transfer;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.SignInButton;
import com.littlefoxstudios.muzify.AlertButtonInterface;
import com.littlefoxstudios.muzify.MuzifySharedMemory;
import com.littlefoxstudios.muzify.R;
import com.littlefoxstudios.muzify.Utilities;
import com.littlefoxstudios.muzify.accounts.Spotify;
import com.littlefoxstudios.muzify.datastorage.CloudStorage;
import com.littlefoxstudios.muzify.homescreenfragments.TransferFragment;
import com.littlefoxstudios.muzify.homescreenfragments.transfer.datalayout.PlaylistFragment;
import com.littlefoxstudios.muzify.homescreenfragments.transfer.datalayout.ReceivePlaylistFragment;
import com.littlefoxstudios.muzify.homescreenfragments.transfer.datalayout.TransferInfo;
import com.littlefoxstudios.muzify.internet.InternetTesterInterface;
import com.littlefoxstudios.muzify.internet.InternetTestingViewModel;


public class SpotifyTransferAuthentication extends Fragment implements InternetTesterInterface, TransferAuthenticationInterface, AlertButtonInterface {

    private Spotify spotify;
    private InternetTestingViewModel internetTestingViewModel;
    private Utilities.Alert alert;
    private Utilities.Alert loading;
    private Utilities.Alert serviceAccessRequestAlert;
    private Utilities.Alert info;
    private int transferCode;
    private TransferInfo transferInfo;

    private Button signInButton;
    private TextView serviceTitle;
    private ImageView serviceImage;

    MuzifySharedMemory muzifySharedMemory;
    RequestQueue queue;



    public SpotifyTransferAuthentication(TransferInfo transferInfo, int transferCode){
        this.transferCode = transferCode;
        this.transferInfo = transferInfo;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_spotify_transfer_authentication, container, false);
        internetTestingViewModel = initializeInternetTestingViewModel();
        alert = new Utilities.Alert(getActivity(), Utilities.Alert.NO_INTERNET);
        observeInternetConnection(internetTestingViewModel, this, alert);
        queue = Volley.newRequestQueue(view.getContext());
        spotify = new Spotify(transferCode, getActivity(), this, queue);
        muzifySharedMemory = new MuzifySharedMemory(getActivity());
        loading = new Utilities.Alert(getActivity(), Utilities.Alert.LOADING);
        serviceAccessRequestAlert = new Utilities.Alert(getActivity(), Utilities.Alert.MUSIC_SERVICE_ACCESS_REQUEST);
        info = new Utilities.Alert(getActivity(), Utilities.Alert.INFO);
        initializeView(view);
        return view;
    }


    private void startLoad()
    {
        loading.startLoading("Loading", "Please wait while we load..");
    }

    private void stopLoad()
    {
        loading.stopDialog();
    }

    private void performSignIn()
    {
        if(Utilities.MusicService.SPOTIFY.isConsumerAccess()){
            spotify.signIn();
            return;
        }

        if(muzifySharedMemory.checkMusicServiceEnabled(Utilities.MusicService.SPOTIFY, muzifySharedMemory.getDefaultAccount())){
            spotify.signIn();
            return;
        }
        startLoad();
        CloudStorage.checkMusicServiceEnabled(muzifySharedMemory, Utilities.MusicService.SPOTIFY, muzifySharedMemory.getDefaultAccount(), this);
    }

    @Override
    public void handleMusicServiceEnableCheck(boolean successResponse, boolean enabled, String accountEmail, String connectedAccountEmail)
    {
        if(enabled){
            stopLoad();
            spotify.signIn();
            return;
        }

        if(!successResponse){
            stopLoad();
            //Error occurred on firebase
            info.showInfoResult(Utilities.Alert.ERROR, "Unable to check your access to the service", "There seems to be a problem in our cloud server. Please try again later", this);
            return;
        }

        CloudStorage.checkIfServiceAccessRequestRaised(Utilities.MusicService.SPOTIFY, accountEmail, this);
    }

    @Override
    public void handleServiceAccessRequestRaiseCheck(boolean successResponse, boolean requestExists, String requestRaiseDate, String developerMessage) {
        stopLoad();
        if(!successResponse){
            //firebase error
            info.showInfoResult(Utilities.Alert.ERROR, "Unable to check your access to the service", "There seems to be a problem in our cloud server. Please try again later", this);
            return;
        }

        if(requestExists){
            if(developerMessage != null && !developerMessage.equals("") ){
                //developer notice
                info.showInfoResult(Utilities.Alert.INFO, "A Note from Developer", developerMessage, this);
            }else{
                //show requested info
                info.showInfoResult(Utilities.Alert.INFO, "We have received your request", "You have raised your request to access this service on "+requestRaiseDate, this);
            }
            return;
        }

        //new request
        String title = "Request Access to Spotify";
        String hint = "Enter your Spotify account email here...";
        String body = "As the app is in developer mode,\nWe need your spotify account email beforehand to let you access our service.\nOur team will provide access to ASAP once you have shared your account email with us.\n\nNOTE : Please note that only one spotify account can be used per user.\nYou cannot change your email once the access request has been raised!";
        serviceAccessRequestAlert.showServiceAccessRequestDialog(Utilities.MusicService.SPOTIFY, title, body, hint, this);
    }


    @Override
    public void handleAddServiceAccessRequest(boolean successResponse) {
        stopLoad();
        if(!successResponse){
            //firebase error
            info.showInfoResult(Utilities.Alert.ERROR, "Upload Error", "Sorry, We were not able to upload your email", this);
            return;
        }
        info.showInfoResult(Utilities.Alert.CHECK_TICK, "Upload Success!", "Your email has been uploaded. Our team will grant access to your account ASAP", this);
        Utilities.Loggers.showLongToast(getContext(), "Your request has been raised!");
    }

    @Override
    public void handleButtonOperation(int mode) {
        if(mode == REQUEST_ACCESS_CLICKED){
            loading.startLoading("Requesting Access", "Uploading your email...");
            CloudStorage.addMusicServiceRequest(Utilities.MusicService.SPOTIFY, muzifySharedMemory.getDefaultAccount(), serviceAccessRequestAlert.tempStringData, this);
            return;
        }
        if(mode == CANCEL_REQUEST_CLICKED){
            infoAlertButtonClicked();
        }
    }

    private void initializeView(View view)
    {
        signInButton = view.findViewById(R.id.transferServiceSignInButton);
        serviceImage = view.findViewById(R.id.transferServiceImage);
        serviceTitle = view.findViewById(R.id.transferServiceName);
        serviceTitle.setText(Utilities.MusicService.SPOTIFY.getFormattedServiceName());
        serviceImage.setImageResource(Utilities.MusicService.SPOTIFY.getLogoDrawableID());
        //signInButton.setVisibility(View.GONE);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performSignIn();
            }
        });
    }

    @Override
    public InternetTestingViewModel initializeInternetTestingViewModel() {
        return new InternetTestingViewModel(getActivity().getApplication());
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent result) {
        super.onActivityResult(requestCode, resultCode, result);
        spotify.listenStartActivityForResult(requestCode, resultCode, result);
    }

    public void switchToNextFragment(String emailID, String accountName, String profilePictureURL) {
        Fragment nextFragment;
        if(transferCode == TransferInfo.RECEIVER_SIDE){
            transferInfo.setDestinationAccountEmailID(emailID);
            transferInfo.setDestinationAccountName(accountName);
            transferInfo.setDestinationAccountUserProfilePicture(profilePictureURL);
            nextFragment = new ReceivePlaylistFragment(transferInfo);
        }else{
            transferInfo.setSourceAccountEmailID(emailID);
            transferInfo.setSourceAccountName(accountName);
            transferInfo.setSourceAccountUserProfilePicture(profilePictureURL);
            nextFragment = new PlaylistFragment(transferInfo);
        }

        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.replace(R.id.frame_layout, nextFragment);
        fragmentTransaction.commit();
    }

    public void onErrorClose(){
        transferInfo.clearTransfer();
        switchToTransferFragment();
    }

    private void switchToTransferFragment(){
        Fragment nextFragment = new TransferFragment(transferInfo, TransferInfo.TRANSFER_SIDE);
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, nextFragment);
        fragmentTransaction.commit();
    }

    @Override
    public void infoAlertButtonClicked() {
        info.stopDialog();
        serviceAccessRequestAlert.stopDialog();
        onErrorClose();
    }
}