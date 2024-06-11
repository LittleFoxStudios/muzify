package com.littlefoxstudios.muzify;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.littlefoxstudios.muzify.databinding.ActivityHomeScreenBinding;
import com.littlefoxstudios.muzify.datastorage.MuzifyViewModel;
import com.littlefoxstudios.muzify.homescreenfragments.SettingsFragment;
import com.littlefoxstudios.muzify.homescreenfragments.HistoryFragment;
import com.littlefoxstudios.muzify.homescreenfragments.TransferFragment;
import com.littlefoxstudios.muzify.homescreenfragments.transfer.MuziShareTransferAuthentication;
import com.littlefoxstudios.muzify.homescreenfragments.transfer.datalayout.TransferInfo;
import com.littlefoxstudios.muzify.internet.InternetTestingViewModel;

public class HomeScreenActivity extends AppCompatActivity {

    ActivityHomeScreenBinding binding;
    BottomNavigationView bottomNavigationView;
    MuzifyViewModel muzifyViewModel;
    Utilities.Alert alert;

    TransferInfo transferInfo;
    int selectedutton = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getSupportActionBar().hide(); //hiding the action bar
        muzifyViewModel = new MuzifyViewModel(getApplication());
        binding = ActivityHomeScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        bottomNavigationView = (BottomNavigationView)findViewById(R.id.BottomNavigationView);

        //on initial load, transfer fragment is set
        transferInfo = transferInfo == null ? new TransferInfo() : transferInfo;
        initialLoad();

        alert = new Utilities.Alert(HomeScreenActivity.this, Utilities.Alert.NO_INTERNET);
        if(!InternetTestingViewModel.internetConnectionAvailable(20000)){
            alert.showNoInternetDialog(null);
        }else{
            continueProcess();
        }
    }

    private void handleDeeplinkMuziShare(String shareCode)
    {
        if(shareCode == null){
            return;
        }
        transferInfo.deepLinkShareCode = shareCode;
        transferInfo.setSourceServiceCode(Utilities.ServiceCode.MUZI_SHARE);
        Fragment fragment = new MuziShareTransferAuthentication(transferInfo);
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    private void continueProcess()
    {
        String emailID = getIntent().getStringExtra("emailID");
        String shareCode = getIntent().getStringExtra("shareCode");
        Validator.validateAppVersion(HomeScreenActivity.this, muzifyViewModel);
        Validator.validateUser(muzifyViewModel, emailID);
        binding.BottomNavigationView.setOnItemSelectedListener(item ->
        {
            switch(item.getItemId()){
                case R.id.history:
                    if(selectedutton == 1){
                        break;
                    }
                    selectedutton = 1;
                    replaceFragment(new HistoryFragment(muzifyViewModel));
                    break;
                case R.id.transfer:
                    if(selectedutton == 2){
                        break;
                    }
                    selectedutton = 2;
                    replaceFragment(new TransferFragment(transferInfo, TransferInfo.TRANSFER_SIDE));
                    break;
                case R.id.settings:
                    if(selectedutton == 3){
                        break;
                    }
                    selectedutton = 3;
                    replaceFragment(new SettingsFragment(transferInfo, muzifyViewModel));
                    break;
            }
            return true;
        });
        handleDeeplinkMuziShare(shareCode);
    }

    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count == 0) {
            super.onBackPressed();
            //additional code
        } else {
            getSupportFragmentManager().popBackStack();
        }
    }


    private void initialLoad()
    {
        replaceFragment(new TransferFragment(transferInfo, TransferInfo.TRANSFER_SIDE)); //without this, only the transfer button will be highlighted
        //transfer fragment is set, however, the nav element highlighted will be the first one.
        //manually setting it to transfer
        bottomNavigationView.getMenu().findItem(R.id.transfer).setChecked(true);
    }

    private void replaceFragment(Fragment fragment)
    {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
        //background colour to black
        bottomNavigationView.setBackgroundColor(getColor(R.color.black));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent result) {
        super.onActivityResult(requestCode, resultCode, result);
    }
}