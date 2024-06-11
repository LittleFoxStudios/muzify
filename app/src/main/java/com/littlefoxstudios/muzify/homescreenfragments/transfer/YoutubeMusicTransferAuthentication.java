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
import com.littlefoxstudios.muzify.R;
import com.littlefoxstudios.muzify.Utilities;
import com.littlefoxstudios.muzify.accounts.YoutubeMusic;
import com.littlefoxstudios.muzify.homescreenfragments.transfer.datalayout.PlaylistFragment;
import com.littlefoxstudios.muzify.homescreenfragments.transfer.datalayout.ReceivePlaylistFragment;
import com.littlefoxstudios.muzify.homescreenfragments.transfer.datalayout.TransferInfo;
import com.littlefoxstudios.muzify.internet.InternetTesterInterface;
import com.littlefoxstudios.muzify.internet.InternetTestingViewModel;


public class YoutubeMusicTransferAuthentication extends Fragment implements InternetTesterInterface {


    private YoutubeMusic youtubeMusic;
    private InternetTestingViewModel internetTestingViewModel;
    private Utilities.Alert alert;
    private Button signInButton;
    private int transferCode;
    private TransferInfo transferInfo;
    private TextView title;
    private ImageView image;

    RequestQueue queue;


    public YoutubeMusicTransferAuthentication(TransferInfo transferInfo, int code) {
        this.transferInfo = transferInfo;
        this.transferCode = code;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_youtube_music_transfer_authentication, container, false);
        internetTestingViewModel = initializeInternetTestingViewModel();
        alert = new Utilities.Alert(getActivity(), Utilities.Alert.NO_INTERNET);
        observeInternetConnection(internetTestingViewModel, this, alert);
        queue = Volley.newRequestQueue(view.getContext());
        youtubeMusic = new YoutubeMusic(transferCode, getActivity(), YoutubeMusicTransferAuthentication.this, queue);
        initializeSignInButton(view);
        initializeScreen(view);
        return view;
    }

    @Override
    public InternetTestingViewModel initializeInternetTestingViewModel() {
        return new InternetTestingViewModel(getActivity().getApplication());
    }

    private void initializeSignInButton(View view)
    {
        signInButton =  view.findViewById(R.id.transferServiceSignInButton);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                youtubeMusic.signIn();
            }
        });
    }

    private void initializeScreen(View view){
        title = (TextView) view.findViewById(R.id.transferServiceName);
        image = (ImageView) view.findViewById(R.id.transferServiceImage);
        title.setText(youtubeMusic.getServiceName());
        image.setImageResource(youtubeMusic.getServiceImageID());

        //transferInfo.backButtonClear(Utilities.MusicService.YOUTUBE_MUSIC.getCode());
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent result) {
        super.onActivityResult(requestCode, resultCode, result);
        youtubeMusic.listenStartActivityForResult(requestCode, resultCode, result);
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

}