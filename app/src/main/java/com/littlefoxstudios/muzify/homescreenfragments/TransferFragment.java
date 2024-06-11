package com.littlefoxstudios.muzify.homescreenfragments;

import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.littlefoxstudios.muzify.OnBackPressedInterface;
import com.littlefoxstudios.muzify.R;
import com.littlefoxstudios.muzify.Utilities;
import com.littlefoxstudios.muzify.homescreenfragments.transfer.MusicServices;
import com.littlefoxstudios.muzify.homescreenfragments.transfer.TransferMusicServiceRecyclerView;
import com.littlefoxstudios.muzify.homescreenfragments.transfer.datalayout.TransferInfo;
import com.littlefoxstudios.muzify.internet.InternetTesterInterface;
import com.littlefoxstudios.muzify.internet.InternetTestingViewModel;


public class TransferFragment extends Fragment implements InternetTesterInterface, TransferMusicServiceRecyclerView.ItemClickListener {

    InternetTestingViewModel internetTester;
    Utilities.Alert alert;
    TransferMusicServiceRecyclerView musicRVAdapter;
    TransferInfo transferInfo;
    private int transferCode;
    TextView fragmentTitle;


    public TransferFragment(TransferInfo transferInfo, int code) {
       this.transferInfo = transferInfo;
       this.transferCode = code;
    }



    @Override
    public InternetTestingViewModel initializeInternetTestingViewModel(){
        return new ViewModelProvider(this).get(InternetTestingViewModel.class);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_transfer, container, false);
        internetTester = initializeInternetTestingViewModel();
        alert = new Utilities.Alert(getActivity(), Utilities.Alert.NO_INTERNET);
        observeInternetConnection(internetTester, this, alert);
        RecyclerView recyclerView = view.findViewById(R.id.transferFragmentMusicServiceRecyclerView);
        fragmentTitle = view.findViewById(R.id.transferFragmentTitle);
        recyclerView.setLayoutManager(new GridLayoutManager(view.getContext(), 2));
        musicRVAdapter = new TransferMusicServiceRecyclerView(view.getContext(), (TransferInfo.TRANSFER_SIDE == transferCode) ? MusicServices.getAllMusicService() : MusicServices.getMusicServiceExcluding(Utilities.ServiceCode.MUZI_SHARE));
        musicRVAdapter.setClickListener(this);
        recyclerView.setAdapter(musicRVAdapter);
        if(transferCode == TransferInfo.TRANSFER_SIDE){
            fragmentTitle.setText("Transfer From");
        }else{
            fragmentTitle.setText("Transfer To");
        }
       return view;
    }

    @Override
    public void onItemClick(View view, int position) {
        //switch to next fragment
        try
        {
            if(TransferInfo.TRANSFER_SIDE == transferCode) {
                transferInfo.setSourceServiceCode(musicRVAdapter.getItem(position).getServiceCode());
            }else{
                transferInfo.setDestinationServiceCode(musicRVAdapter.getItem(position).getServiceCode());
            }
            Fragment nextFragment = MusicServices.getAppropriateFragment(transferInfo, musicRVAdapter.getItem(position).getServiceCode(), transferCode);
            if(!validateAppInstallation(musicRVAdapter.getItem(position).getServiceCode())){
                Utilities.Loggers.showLongToast(getContext(), "Please install latest version of "+musicRVAdapter.getItem(position).getServiceName()+" on your device!");
                return;
            }
            replaceFragment(nextFragment);
        }
        catch (Exception e)
        {
            Utilities.Loggers.showLongToast(view.getContext(), "Sorry, Some error have occurred!");
        }
    }

    private boolean validateAppInstallation(int serviceCode)
    {
        switch (serviceCode)
        {
            case Utilities.ServiceCode.SPOTIFY : return validateSpotifyAppInstallation();
            default: return true;
        }
    }

    private boolean validateSpotifyAppInstallation()
    {
        PackageManager pm = getContext().getPackageManager();
        try {
            pm.getPackageInfo("com.spotify.music", 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void replaceFragment(Fragment fragment)
    {
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }
}