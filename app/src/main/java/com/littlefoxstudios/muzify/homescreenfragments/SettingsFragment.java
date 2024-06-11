package com.littlefoxstudios.muzify.homescreenfragments;

import static com.littlefoxstudios.muzify.Utilities.getRandomDrawableIDForThumbnail;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.google.android.material.imageview.ShapeableImageView;
import com.littlefoxstudios.muzify.AlertButtonInterface;
import com.littlefoxstudios.muzify.MainActivity;
import com.littlefoxstudios.muzify.MuzifySharedMemory;
import com.littlefoxstudios.muzify.R;
import com.littlefoxstudios.muzify.Utilities;
import com.littlefoxstudios.muzify.WelcomeActivity;
import com.littlefoxstudios.muzify.datastorage.CloudStorage;
import com.littlefoxstudios.muzify.datastorage.LocalStorage;
import com.littlefoxstudios.muzify.datastorage.MuzifyViewModel;
import com.littlefoxstudios.muzify.homescreenfragments.transfer.datalayout.TransferInfo;
import com.squareup.picasso.Picasso;

import java.util.List;


public class SettingsFragment extends Fragment implements AlertButtonInterface {

    TransferInfo transferInfo;
    MuzifyViewModel muzifyViewModel;
    MuzifySharedMemory sharedMemory;

    ShapeableImageView profilePicture;
    TextView userName;

    ConstraintLayout switchAccountLayout;
    ConstraintLayout uploadHistoryLayout;
    ConstraintLayout downloadHistoryLayout;
    ConstraintLayout autoUploadHistoryToggleLayout;

    ImageView autoUploadHistoryToggleImage;
    Utilities.Alert info;

    public SettingsFragment(TransferInfo transferInfo, MuzifyViewModel muzifyViewModel) {
        this.transferInfo = transferInfo;
        this.muzifyViewModel = muzifyViewModel;
    }

    private void initialize(View view)
    {
        sharedMemory = new MuzifySharedMemory(this.getActivity());
        profilePicture = view.findViewById(R.id.settingsAccountProfilePicture);
        userName = view.findViewById(R.id.settingsAccountUserName);

        switchAccountLayout = view.findViewById(R.id.settingsSwitchAccountButtonLayout);
        uploadHistoryLayout = view.findViewById(R.id.settingsUploadHistoryLayout);
        downloadHistoryLayout = view.findViewById(R.id.settingsDownloadHistoryLayout);
        autoUploadHistoryToggleLayout = view.findViewById(R.id.settingsAutoUploadHistoryLayout);

        autoUploadHistoryToggleImage = view.findViewById(R.id.settingsAutoUploadHistoryImage);

        profilePicture.setImageResource(R.drawable.person_image);
        userName.setText("Welcome");
        info = new Utilities.Alert(getActivity(), Utilities.Alert.INFO);
    }

    private void loadUserData()
    {
        String emailID = sharedMemory.getDefaultAccount();
        LiveData<List<LocalStorage.UserData>> userData = muzifyViewModel.getUserDataForSpecificUser(emailID);
        userData.observe(getViewLifecycleOwner(), new Observer<List<LocalStorage.UserData>>() {
            @Override
            public void onChanged(List<LocalStorage.UserData> data) {
                userData.removeObserver(this);
                LocalStorage.UserData currentUserData = data.get(0);
                userName.setText(currentUserData.getUserName());
                Picasso.get().load(currentUserData.getUserProfilePictureURL()).placeholder(R.drawable.person_image).into(profilePicture);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        initialize(rootView);
        loadUserData();
        initializeButtons();
        return rootView;
    }

    private void initializeButtonDisplay(boolean autoUploadEnabled)
    {
        if(autoUploadEnabled){
            uploadHistoryLayout.setBackgroundResource(R.drawable.border_thin_red);
            autoUploadHistoryToggleImage.setImageResource(R.drawable.auto_upload_on);
        }else{
            uploadHistoryLayout.setBackgroundResource(R.drawable.border_thin_grey);
            autoUploadHistoryToggleImage.setImageResource(R.drawable.auto_upload_off);
        }
    }

    private void initializeButtons()
    {
        initializeButtonDisplay(sharedMemory.autoUploadHistorySelected(sharedMemory.getDefaultAccount()));
        switchAccountLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleSwitchAccount();
            }
        });

        uploadHistoryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleUploadHistory();
            }
        });

        autoUploadHistoryToggleLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleAutoUpload();
            }
        });

        downloadHistoryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleDownloadHistory();
            }
        });
    }

    private void handleDownloadHistory()
    {
        new CloudStorage().downloadHistoryForCurrentUser(getActivity(), muzifyViewModel, info, this, sharedMemory.getDefaultAccount());
    }

    private void handleAutoUpload()
    {
       initializeButtonDisplay(sharedMemory.toggleHistoryAutoUpload(sharedMemory.getDefaultAccount()));
    }

    private void handleUploadHistory()
    {
        if(sharedMemory.autoUploadHistorySelected(sharedMemory.getDefaultAccount())){
            Utilities.Loggers.showLongToast(getContext(), "Auto Upload History enabled. Disable it to manually upload History!");
            return;
        }
        new CloudStorage().uploadHistoryForCurrentUser(getActivity(), muzifyViewModel, info, this, sharedMemory.getDefaultAccount());
    }

    private void handleSwitchAccount()
    {
        Utilities.Loggers.showLongToast(getContext(), "Hold on, Restarting the App...");
        sharedMemory.enableSwitchAccount();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                transferInfo = new TransferInfo();
                startActivity(new Intent(getActivity(), WelcomeActivity.class));
                getActivity().finish();
            }
        }, 1500);
    }


    @Override
    public void infoAlertButtonClicked() {
       if(info.tempFlag){
           info.stopDialog();
       }else{
           Utilities.Loggers.showShortToast(getContext(), "Please wait..Some process are pending!");
       }
    }
}