package com.littlefoxstudios.muzify.homescreenfragments.transfer;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;

import com.google.android.material.button.MaterialButton;
import com.littlefoxstudios.muzify.MuzifyConfigs;
import com.littlefoxstudios.muzify.MuzifySharedMemory;
import com.littlefoxstudios.muzify.R;
import com.littlefoxstudios.muzify.Utilities;
import com.littlefoxstudios.muzify.datastorage.CloudStorage;
import com.littlefoxstudios.muzify.datastorage.LocalStorage;
import com.littlefoxstudios.muzify.datastorage.MuzifyViewModel;
import com.littlefoxstudios.muzify.homescreenfragments.TransferFragment;
import com.littlefoxstudios.muzify.homescreenfragments.transfer.datalayout.TransferInfo;
import com.littlefoxstudios.muzify.internet.InternetTestingViewModel;

public class MuziShareTransferAuthentication extends Fragment {

    private InternetTestingViewModel internetTestingViewModel;
    private Utilities.Alert alert;
    private TransferInfo transferInfo;
    private MuzifySharedMemory muzifySharedMemory;
    private MuzifyViewModel muzifyViewModel;

    private MaterialButton startButton;
    private EditText shareCodeBox;

    public MuziShareTransferAuthentication(TransferInfo transferInfo){
        this.transferInfo = transferInfo;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_muzify_transfer_authentication, container, false);
        initializeVars(view);
        handleDeepLinkShare();
        return view;
    }

    public void handleDeepLinkShare()
    {
        if(transferInfo.deepLinkShareCode != null){
            String shareCode = transferInfo.deepLinkShareCode;
            transferInfo.deepLinkShareCode = null;
            handleShare(shareCode);
        }
    }

    private void initializeVars(View view){
        internetTestingViewModel = new InternetTestingViewModel(getActivity().getApplication());
        alert = new Utilities.Alert(getActivity(), Utilities.Alert.LOADING);
        startButton = view.findViewById(R.id.muziShareStartButton);
        shareCodeBox = view.findViewById(R.id.muziShareCodeEditText);
        muzifySharedMemory = new MuzifySharedMemory(getActivity());
        muzifyViewModel = new MuzifyViewModel(getActivity().getApplication());
        loadStartButton();
    }

    private void loadStartButton()
    {
        startButton.setVisibility(View.GONE);
        shareCodeBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.toString().length() >= MuzifyConfigs.MUZI_SHARE_CODE_LENGTH_MIN){
                    startButton.setVisibility(View.VISIBLE);
                    Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.fade);
                    animation.setDuration(1000);
                    startButton.setAnimation(animation);
                }else{
                    startButton.setVisibility(View.GONE);
                    Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out);
                    animation.setDuration(1000);
                    startButton.setAnimation(animation);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleShare(null);
            }
        });
    }

    private void handleShare(String shareCode)
    {
        if(muzifySharedMemory.getMuziShareRemainingAttempts() <= 0){
            Utilities.Loggers.showLongToast(getContext(), "You can use this feature in "+Utilities.getTimerString(muzifySharedMemory.getMuziShareAttemptRefreshTime() - System.currentTimeMillis()));
            return;
        }
        if(shareCode != null){
            shareCodeBox.setText(shareCode);
        }
        shareCode = (shareCode == null) ? shareCodeBox.getText().toString() : shareCode;
        alert.startLoading("Loading", "Fetching shared playlist details...");

        if(!transferInfo.isShareCodeAvailable(shareCode)){
            CloudStorage.getShare(shareCode, MuziShareTransferAuthentication.this);
            return;
        }

        if(transferInfo.isInvalidShareCode(shareCode)){
            handleGetShare(null, false, shareCode);
            return;
        }

        handleGetShare(transferInfo.getSharedCardDetails(shareCode), false, shareCode);
    }


    public void handleGetShare(LocalStorage.Card card, boolean isDownloadFailed, String shareCode)
    {
        alert.stopDialog();
        if(isDownloadFailed){
            Utilities.Loggers.showLongToast(getContext(), "Sorry, Unable to get playlist details.");
            return;
        }

        if(card == null){
            transferInfo.addInvalidShareCode(shareCode);
            int remainingAttempts = muzifySharedMemory.muziShareAttemptUsed();
            if(remainingAttempts == 0){
                Utilities.Loggers.showLongToast(getContext(), "You have used all the remaining attempts");
            }else{
                Utilities.Loggers.showLongToast(getContext(),"Invalid Code! You have "+remainingAttempts+" attempts remaining");
            }
        }else{
            muzifySharedMemory.clearMuziShareAttempts();
            transferInfo.initializeSharedPlaylist(shareCode, card);
            transferInfo.setSourceAccountUserProfilePicture(card.getSourceAccountProfilePictureURL());
            switchToNextFragment();
        }
    }

    private void switchToNextFragment()
    {
        Fragment nextFragment = new TransferFragment(transferInfo, TransferInfo.RECEIVER_SIDE);
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.replace(R.id.frame_layout, nextFragment);
        fragmentTransaction.commit();
    }
}