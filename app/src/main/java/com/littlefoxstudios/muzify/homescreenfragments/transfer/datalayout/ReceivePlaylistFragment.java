package com.littlefoxstudios.muzify.homescreenfragments.transfer.datalayout;

import static com.littlefoxstudios.muzify.homescreenfragments.transfer.datalayout.ReceiveInfo.generateInitialPlaylistInfo;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.littlefoxstudios.muzify.AlertButtonInterface;
import com.littlefoxstudios.muzify.MuzifyConfigs;
import com.littlefoxstudios.muzify.MuzifySharedMemory;
import com.littlefoxstudios.muzify.R;
import com.littlefoxstudios.muzify.Utilities;
import com.littlefoxstudios.muzify.accounts.MuzifyAccount;
import com.littlefoxstudios.muzify.apis.API;
import com.littlefoxstudios.muzify.datastorage.CloudStorage;
import com.littlefoxstudios.muzify.datastorage.LocalStorage;
import com.littlefoxstudios.muzify.datastorage.MuzifyViewModel;
import com.littlefoxstudios.muzify.homescreenfragments.TransferFragment;
import com.littlefoxstudios.muzify.homescreenfragments.transfer.MusicServices;

import java.util.ArrayList;
import java.util.List;


public class ReceivePlaylistFragment extends Fragment implements AlertButtonInterface {


    public ReceivePlaylistFragment() {
        // Required empty public constructor
    }

    private TransferInfo transferInfo;
    private Utilities.Alert infoAlert;
    private Utilities.Alert internetAlert;
    private RecyclerView recyclerView;
    private ReceiveInfoRecyclerView receiveInfoRVAdapter;
    private ArrayList<ReceiveInfo> receiveInfoList;
    private RequestQueue queue;
    private TextView fragmentTitle;
    private boolean historySaveComplete = false;
    private boolean informToast = false;
    private MuzifySharedMemory sharedMemory;

    public ReceivePlaylistFragment(TransferInfo transferInfo)
    {
        this.transferInfo = transferInfo;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_receive_playlist, container, false);
        initializeView(view);
        startUploading();
        return view;
    }

    private void initializeView(View view)
    {
        recyclerView = view.findViewById(R.id.receiveFragmentInfoRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        receiveInfoList = generateInitialPlaylistInfo(transferInfo.getPlaylist());
        receiveInfoRVAdapter = new ReceiveInfoRecyclerView(getContext(), receiveInfoList);
        recyclerView.setAdapter(receiveInfoRVAdapter);
        infoAlert = new Utilities.Alert(getActivity(), Utilities.Alert.INFO);
        internetAlert = new Utilities.Alert(getActivity(), Utilities.Alert.NO_INTERNET);
        queue = Volley.newRequestQueue(view.getContext());
        fragmentTitle = view.findViewById(R.id.receiveFragmentTitle);
        sharedMemory = new MuzifySharedMemory(getActivity());
    }

    public void onComplete(){
        //save history
        long cardNumber = System.currentTimeMillis();
        historySaveComplete = false;
        saveHistory(cardNumber);
    }

    private void uploadSharedUserDetails(String email, String profilePicUrl, String userName, boolean isSharedTransfer, String shareCode, int destinationServiceCode, long cardNumber, String ownerEmailID)
    {
        if(!isSharedTransfer){
            return;
        }
        MuzifyAccount account = new MuzifyAccount(userName, email, profilePicUrl, destinationServiceCode, cardNumber, ownerEmailID, shareCode);
        CloudStorage.uploadSharedUserDetails(shareCode, account);
    }

    private void uploadHistory(MuzifyViewModel muzifyViewModel, LocalStorage.Card card){
        if(!MuzifyConfigs.autoUploadHistoryEnabled(sharedMemory)){
            historySaveComplete = true;
            return;
        }
        try{
            new CloudStorage().uploadCard(muzifyViewModel, card, getActivity());
        }catch (Exception e){
            Utilities.Loggers.showLongToast(getContext(), "Unable to upload playlist");
        }
        historySaveComplete = true;
    }

    private void saveHistory(long cardNumber)
    {
        fragmentTitle.setText("Transferred!");
        infoAlert.showInfoResult(Utilities.Alert.CHECK_TICK, "Transfer Complete!", "Your playlist has been transferred!", this);
        if(!MuzifyConfigs.SAVE_HISTORY_ENABLED){
            historySaveComplete = true;
            return;
        }

        MuzifyViewModel muzifyViewModel = new MuzifyViewModel(getActivity().getApplication());
        MuzifySharedMemory sharedPreference = new MuzifySharedMemory(getActivity());
        int sourceServiceCode = transferInfo.getSourceServiceCode();
        int destinationServiceCode = transferInfo.getDestinationServiceCode();
        String sourceAccountEmailID = transferInfo.getSourceAccountEmailID();
        String destinationAccountEmailID = transferInfo.getDestinationAccountEmailID();
        String sourceAccountName = transferInfo.getSourceAccountName();
        String destinationAccountName = transferInfo.getDestinationAccountName();
        String sourceAccountProfilePictureURL = transferInfo.getSourceAccountUserProfilePicture();
        String destinationAccountProfilePictureURL = transferInfo.getDestinationAccountUserProfilePicture();
        String playlistTitle = transferInfo.getPlaylist().getPlaylistTitle();
        ArrayList<PlaylistItem> playlistItems = transferInfo.getTransferredPlaylistItems();
        int failedItemsCount = PlaylistItem.getFailedItemsCount(playlistItems);
        String playlistImageUrls = PlaylistItem.getPlaylistThumbnailURLS(playlistItems);

        LiveData<List<LocalStorage.UserData>> allUserData = muzifyViewModel.getUserDataForSpecificUser(sharedPreference.getDefaultAccount());
        allUserData.observe(getViewLifecycleOwner(), new Observer<List<LocalStorage.UserData>>() {
            @Override
            public void onChanged(List<LocalStorage.UserData> userData) {
                allUserData.removeObserver(this);
                LocalStorage.UserData currentUser = userData.get(0);
                String currentUserEmail = currentUser.getEmailID();
                currentUser.addCardToBeUploaded(cardNumber);
                try {
                    ArrayList<LocalStorage.Album> albumsList = new ArrayList<>();
                    muzifyViewModel.update(currentUser);
                    LocalStorage.Card card = new LocalStorage.Card(currentUserEmail, cardNumber,
                            playlistTitle, playlistItems.size(), failedItemsCount, sourceAccountEmailID,
                            destinationAccountEmailID, sourceAccountName, sourceServiceCode,
                            destinationServiceCode, playlistImageUrls,
                            Utilities.convertTimeStampToDate(cardNumber), destinationAccountName,
                            transferInfo.isSharedTransfer() ? transferInfo.getSelectedPlaylistID() : "",
                            transferInfo.getSourcePlaylistURL(), transferInfo.getDestinationPlaylistURL(),
                            (sourceServiceCode != Utilities.ServiceCode.MUZI_SHARE ? sourceServiceCode : transferInfo.muziShareSourceServiceCode),
                            sourceAccountProfilePictureURL, destinationAccountProfilePictureURL); //selected playlist id will be the share code
                    card.shareFlag = transferInfo.isSharedTransfer();
                    card.uploadFlag = MuzifyConfigs.autoUploadHistoryEnabled(sharedMemory);
                    muzifyViewModel.insert(card);
                    for(PlaylistItem item : playlistItems){
                        LocalStorage.Album album = new LocalStorage.Album(cardNumber, "",
                                item.getThumbnailImageURL(), item.getArtistName(), item.getSongName(),
                                item.getProcessedBy(), item.getSongID(), item.getSourceSongID());
                        muzifyViewModel.insert(album);
                        albumsList.add(album);
                    }
                    fragmentTitle.setText("History Saved");
                    card.albums = albumsList; // adding albums to the card - to be uploaded to cloud
                    uploadHistory(muzifyViewModel, card);
                    uploadSharedUserDetails(currentUser.getEmailID(), currentUser.getUserProfilePictureURL(),
                            currentUser.getUserName(), transferInfo.isSharedTransfer(),
                            transferInfo.getSelectedPlaylistID(), destinationServiceCode,
                            cardNumber, transferInfo.getSourceAccountEmailID());
                    if(informToast){
                        Utilities.Loggers.showLongToast(getContext(), "Operation Completed!");
                    }
                } catch (Exception e) {
                   onSaveError();
                }
            }
        });
    }

    public void onSaveError()
    {
        fragmentTitle.setText("History save failed!");
        transferInfo.clearTransfer();
        historySaveComplete = true;
        infoAlert.showInfoResult(Utilities.Alert.ERROR, "Unable to save History!", "Sorry, An error occurred while saving your playlist history", this);
    }

    public void onErrorComplete(){
        fragmentTitle.setText("Transfer failed");
        transferInfo.clearTransfer();
        historySaveComplete = true;
        infoAlert.showInfoResult(Utilities.Alert.ERROR, "Transfer Failed!", "Either All songs have been failed or there was an internal error while transfer", this);
    }


    private void refreshScreen(int index)
    {
        receiveInfoRVAdapter.update(receiveInfoList);
        receiveInfoRVAdapter.notifyItemChanged(index);
    }

    public Utilities.Alert getInfoAlert() {
        return infoAlert;
    }

    public TransferInfo getTransferInfo() {
        return transferInfo;
    }

    private void startUploading()
    {
        API serviceAPI = MusicServices.apiFactory(transferInfo.getDestinationServiceCode(), queue, getActivity(), transferInfo);
        assert serviceAPI != null;
        serviceAPI.uploadPlaylist(this);
    }

    public void updateItem(int newStatusCode, int index)
    {
        receiveInfoList = ReceiveInfo.updateItem(receiveInfoList, newStatusCode, index);
        refreshScreen(index);
    }

    public void infoAlertButtonClicked()
    {
        if(historySaveComplete){
            infoAlert.stopDialog();
            transferInfo.clearTransfer();
            switchToTransferFragment();
        }else{
            Utilities.Loggers.showLongToast(getContext(), "Please wait.. Some process are still running");
            informToast = true;
        }
    }

    private void switchToTransferFragment(){
        Fragment nextFragment = new TransferFragment(transferInfo, TransferInfo.TRANSFER_SIDE);
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, nextFragment);
        fragmentTransaction.commit();
    }

    public void playlistCreationFailed(){
        receiveInfoList = ReceiveInfo.playlistCreationFailed(receiveInfoList);
        receiveInfoRVAdapter.update(receiveInfoList);
        receiveInfoRVAdapter.notifyDataSetChanged();
    }
}