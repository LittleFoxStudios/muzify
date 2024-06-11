package com.littlefoxstudios.muzify.datastorage;

import static com.littlefoxstudios.muzify.Utilities.encodeUserEmail;
import static com.littlefoxstudios.muzify.datastorage.CloudStorage.Keys.CARDS;
import static com.littlefoxstudios.muzify.datastorage.CloudStorage.Keys.PROFILE_PICTURE_URL;
import static com.littlefoxstudios.muzify.datastorage.CloudStorage.Keys.SHARED_USER_DETAILS;
import static com.littlefoxstudios.muzify.datastorage.CloudStorage.Keys.USER_NAME;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.primitives.Longs;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger;
import com.littlefoxstudios.muzify.AlertButtonInterface;
import com.littlefoxstudios.muzify.AppVersion;
import com.littlefoxstudios.muzify.MuzifyConfigs;
import com.littlefoxstudios.muzify.MuzifySharedMemory;
import com.littlefoxstudios.muzify.Utilities;
import com.littlefoxstudios.muzify.Validator;
import com.littlefoxstudios.muzify.accounts.MuzifyAccount;
import com.littlefoxstudios.muzify.homescreenfragments.history.innercard.DetailsBlockRecyclerViewAdapter;
import com.littlefoxstudios.muzify.homescreenfragments.history.innercard.InnerCardActivity;
import com.littlefoxstudios.muzify.homescreenfragments.history.innercard.InnerCardObj;
import com.littlefoxstudios.muzify.homescreenfragments.transfer.MusicServices;
import com.littlefoxstudios.muzify.homescreenfragments.transfer.MuziShareTransferAuthentication;
import com.littlefoxstudios.muzify.homescreenfragments.transfer.TransferAuthenticationInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class CloudStorage extends AppCompatActivity {


    public static class Keys
    {
        static final String ACCOUNTS = "accounts";
        static final String USER_NAME = "userName";
        static final String EMAIL_ID = "emailID";
        static final String PROFILE_PICTURE_URL = "profilePictureURL";
        static final String SHARES = "shares";
        static final String SHARED_CARD_DATA = "sharedCardData";
        static final String SHARED_TIME = "sharedTime";
        static final String APP_VERSION = "appVersion";
        static final String CARDS = "cardData";
        static final String SHARED_USER_DETAILS = "sharedUserDetails";
        static final String MUSIC_SERVICES = "musicServices";
        static final String SERVICE_ACCESS_REQUESTS = "serviceAccessRequests";
    }

    public static void addNewAccount(MuzifyAccount account, Context context)
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(Keys.ACCOUNTS);
        myRef.child(encodeUserEmail(account.getEmailID())).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.getResult().getValue() == null){
                 myRef.child(encodeUserEmail(account.getEmailID())).setValue(MuzifyAccount.getFreshAccount(account)).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Utilities.Loggers.showLongToast(context, "Welcome to Muzify, "+account.getUserName());
                        }
                    });
                }else{
                    Utilities.Loggers.showLongToast(context, "Welcome back, "+account.getUserName());
                }
            }
        });
    }

    public static void updateAccount(MuzifyAccount account)
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(Keys.ACCOUNTS).child(encodeUserEmail(account.getEmailID()));
        myRef.child(PROFILE_PICTURE_URL).setValue(account.getProfilePictureURL());
        myRef.child(USER_NAME).setValue(account.getUserName());
        myRef.child("accountLastUpdatedTime").setValue(System.currentTimeMillis()+"");
    }

    public static void checkAppVersion(Activity activity, MuzifyViewModel muzifyViewModel)
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(Keys.APP_VERSION);
        myRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
               String version = task.getResult().getValue(String.class);
               AppVersion appVersion = new AppVersion(version);
              try
              {
                  Validator.handleAppUpdate(activity, !appVersion.isVersionAllowed(), muzifyViewModel);
              }catch (Exception e)
              {
                  Utilities.Loggers.showLongToast(activity.getApplicationContext(), "Error occurred : "+e.getMessage());
                  activity.finishAndRemoveTask();
              }
            }
        });
    }

    public void uploadCard(MuzifyViewModel muzifyViewModel, LocalStorage.Card card, Activity activity){
        uploadCard(muzifyViewModel, card, activity, true, null, false, "");
    }

    public void uploadCard(MuzifyViewModel muzifyViewModel , LocalStorage.Card card, Activity activity, boolean includeSuccessToast, Utilities.Alert alert, boolean closeAlert, String generatedMessage){
        if(card == null){
            return;
        }
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        String emailID = encodeUserEmail(card.getEmailID());
        DatabaseReference myRef = database.getReference(Keys.ACCOUNTS).child(emailID).child(CARDS).child(card.getCardNumber()+"");
        myRef.setValue(card).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                //remove card numbers from cards to be uploaded
               LiveData<List<LocalStorage.UserData>> allUserData = muzifyViewModel.getAllUserData();
               allUserData.observe((LifecycleOwner) activity, new Observer<List<LocalStorage.UserData>>() {
                   @Override
                   public void onChanged(List<LocalStorage.UserData> userData) {
                       allUserData.removeObserver(this);
                       for(LocalStorage.UserData user : userData){
                           if(user.getEmailID().equals(card.getEmailID())){
                               ArrayList<Long> cardsToBeUploaded = user.getCardsToBeUploaded();
                               cardsToBeUploaded.remove(card.getCardNumber());
                               user.cardsToBeUploaded = Utilities.convertListToString(cardsToBeUploaded);
                               try {
                                   muzifyViewModel.update(user);
                               } catch (Exception e) {
                                   e.printStackTrace();
                               }
                               break;
                           }
                       }
                   }
               });
               if(includeSuccessToast){
                   Utilities.Loggers.showShortToast(activity.getApplicationContext(), card.getPlaylistTitle()+" has been uploaded");
               }
                if(closeAlert){
                    try {
                        Utilities.Loggers.showLongToast(activity.getApplicationContext(), generatedMessage);
                        alert.uploadFinished();
                    } catch (Exception e) {
                        activity.finishAndRemoveTask();
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Utilities.Loggers.showLongToast(activity.getApplicationContext(), Utilities.ErrorDetailer.UNABLE_TO_UPLOAD_CARD.getErrorMessage());
            }
        });
    }

    public static void getShare(String shareCode, MuziShareTransferAuthentication callingFragment) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(Keys.SHARES);
        myRef.child(shareCode).child(Keys.SHARED_CARD_DATA).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.getResult().getValue() == null){
                    callingFragment.handleGetShare(null, false, null);
                }else{
                    DataSnapshot snapshot =  task.getResult();
                    HashMap<String, HashMap> dataHash = new HashMap<>();
                    if(snapshot.getValue() != null){
                        dataHash = (HashMap<String, HashMap>) snapshot.getValue();
                    }
                    callingFragment.handleGetShare(LocalStorage.Card.convertShareHashDataToObject(dataHash), false, shareCode);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                callingFragment.handleGetShare(null, true, null);
            }
        });
    }

    public static void addShare(String emailID, MuzifyViewModel muzifyViewModel, long cardNumber, long shareTime, InnerCardObj.ShareModel shareModel, InnerCardActivity.ShareInfoDialog shareInfoDialog)
    {
        int size = Utilities.getRandomNumber(MuzifyConfigs.MUZI_SHARE_CODE_LENGTH_MIN, MuzifyConfigs.MUZI_SHARE_CODE_LENGTH_MAX);
        String code = Utilities.getAlphaNumericString(size);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(Keys.SHARES);

        myRef.child(code).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.getResult().getValue() == null){
                    try {
                        List<LocalStorage.Card> cards = muzifyViewModel.getCardsWithAlbums(emailID);
                        LocalStorage.Card card = null;
                        for(LocalStorage.Card c : cards){
                            if(c.getCardNumber() == cardNumber){
                                card = c;
                                break;
                            }
                        }
                        if(card != null){
                            card.shareCode = code;
                            shareModel.setShareCode(code);
                            String day = Utilities.convertTimeStampToDate(shareTime);
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference myRef = database.getReference(Keys.SHARES);
                            myRef.child(code).child(Keys.SHARED_TIME).setValue(day);
                            myRef.child(code).child(Keys.SHARED_CARD_DATA).setValue(card);
                            card.albums = null; //a card should not have album (in local storage)
                            muzifyViewModel.update(card);
                            shareInfoDialog.show(shareModel);
                            return;
                        }
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else{
                    addShare(emailID, muzifyViewModel, cardNumber, shareTime, shareModel, shareInfoDialog);
                }
            }
        });
    }


    public static void addShareV2(String emailID, MuzifyViewModel muzifyViewModel, long cardNumber, long shareTime, InnerCardActivity callingActivity, DetailsBlockRecyclerViewAdapter.MyViewHolder holder)
    {
        int size = Utilities.getRandomNumber(MuzifyConfigs.MUZI_SHARE_CODE_LENGTH_MIN, MuzifyConfigs.MUZI_SHARE_CODE_LENGTH_MAX);
        String code = Utilities.getAlphaNumericString(size);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(Keys.SHARES);

        myRef.child(code).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.getResult().getValue() == null){
                    try {
                        List<LocalStorage.Card> cards = muzifyViewModel.getCardsWithAlbums(emailID);
                        LocalStorage.Card card = null;
                        for(LocalStorage.Card c : cards){
                            if(c.getCardNumber() == cardNumber){
                                card = c;
                                break;
                            }
                        }
                        if(card != null){
                            card.shareCode = code;
                            String day = Utilities.convertTimeStampToDate(shareTime);
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference myRef = database.getReference(Keys.SHARES);
                            myRef.child(code).child(Keys.SHARED_TIME).setValue(day);
                            myRef.child(code).child(Keys.SHARED_CARD_DATA).setValue(card);

                            //if a card is uploaded before share code generated and share is generated later on, we have to
                            //update the uploaded card with the new sharecode
                            updateCardWithShareCodeOnCloud(card, emailID, callingActivity);

                            //updating card in local
                            card.albums = null; //a card should not have album (in local storage)
                            muzifyViewModel.update(card);

                            callingActivity.handleAddShareCallbackV2FromCloud(code, holder);
                            return;
                        }
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else{
                    addShareV2(emailID, muzifyViewModel, cardNumber, shareTime, callingActivity, holder);
                }
            }
        });
    }

    private static void updateCardWithShareCodeOnCloud(LocalStorage.Card card, String emailID, InnerCardActivity callingActivity) {
        try{
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            String encodedEmailID = encodeUserEmail(emailID);
            DatabaseReference myRef = database.getReference(Keys.ACCOUNTS).child(encodedEmailID);
            myRef.child(CARDS).child(card.getCardNumber()+"").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if(task.getResult().getValue() == null){
                        //card not yet uploaded. exit.
                        return;
                    }
                    database.getReference(Keys.ACCOUNTS).child(encodedEmailID).child(CARDS).child(card.getCardNumber()+"").child("shareCode").setValue(card.getShareCode()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Utilities.Loggers.showLongToast(callingActivity, "Share code updated in the uploaded history");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Utilities.Loggers.showLongToast(callingActivity, "Failed to update share code in the uploaded history");
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }catch (Exception e){

        }
    }

    public void downloadHistoryForCurrentUser(Activity activity, MuzifyViewModel muzifyViewModel, Utilities.Alert alert, AlertButtonInterface alertButtonInterface, String currentUserEmailID){
        alert.tempFlag = false;
        alert.showInfoResult(Utilities.Alert.INFO, "Downloading History", "Downloading your history...\nPlease wait.", alertButtonInterface);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        String encodedEmailID = encodeUserEmail(currentUserEmailID);
        DatabaseReference myRef = database.getReference(Keys.ACCOUNTS).child(encodedEmailID);
        myRef.child(CARDS).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                alert.tempFlag = true;
                if(task.getResult().getValue() == null){
                    alert.showInfoResult(Utilities.Alert.INFO, "No History found in Cloud!", "There are no history present in cloud!", alertButtonInterface);
                    return;
                }
                alert.tempFlag = false;
                HashMap<String, HashMap> dataHash = (HashMap<String, HashMap>) task.getResult().getValue();
                List<LocalStorage.Card> cardsInCloud = LocalStorage.Card.convertHashDataToObject(dataHash);
                LiveData<List<LocalStorage.Card>> cardsInLocal = muzifyViewModel.getCardDataForSpecificUser(currentUserEmailID);
                cardsInLocal.observe((LifecycleOwner) activity, new Observer<List<LocalStorage.Card>>() {
                    @Override
                    public void onChanged(List<LocalStorage.Card> cards) {
                        cardsInLocal.removeObserver(this);
                        ArrayList<Long> existingCardNumbers = new ArrayList<>();
                        boolean downloadHit = false;
                        for(LocalStorage.Card card : cards){
                            existingCardNumbers.add(card.getCardNumber());
                        }
                        for(LocalStorage.Card card : cardsInCloud) {
                            card.downloadFlag = true;
                            if (existingCardNumbers.contains(card.cardNumber)) {
                                continue;
                            }
                            downloadHit = true;
                            try {
                                muzifyViewModel.insert(card);
                                for(LocalStorage.Album album : card.albums){
                                    muzifyViewModel.insert(album);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        if(downloadHit){
                            alert.tempFlag = true;
                            alert.showInfoResult(Utilities.Alert.INFO, "History Downloaded!", "History downloaded successfully!", alertButtonInterface);
                            return;
                        }
                    }
                });
                alert.tempFlag = true;
                alert.showInfoResult(Utilities.Alert.INFO, "Up-to-Date!!", "No new history found on the cloud!", alertButtonInterface);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                alert.tempFlag = true;
                alert.showInfoResult(Utilities.Alert.INFO, "Download Failed", "Some error had occurred from firebase servers.\nDownload failed :(", alertButtonInterface);
            }
        });
    }

    public void uploadHistoryForCurrentUser(Activity activity, MuzifyViewModel muzifyViewModel, Utilities.Alert alert, AlertButtonInterface alertButtonInterface, String currentUserEmailID){
        LiveData<List<LocalStorage.UserData>> currentUser = muzifyViewModel.getUserDataForSpecificUser(currentUserEmailID);
        currentUser.observe((LifecycleOwner) activity, new Observer<List<LocalStorage.UserData>>() {
            @Override
            public void onChanged(List<LocalStorage.UserData> userData) {
                currentUser.removeObserver(this);
                alert.tempFlag = false;
                LocalStorage.UserData currentUserData = userData.get(0);
                ArrayList<Long> cardsToBeUploaded = currentUserData.getCardsToBeUploaded();
                if(cardsToBeUploaded == null || cardsToBeUploaded.size() == 0){
                    alert.tempFlag = true;
                    alert.showInfoResult(Utilities.Alert.INFO, "Already Uploaded to Cloud!", "Nothing new to upload here. Everything has already been uploaded :)", alertButtonInterface);
                    return;
                }
                alert.showInfoResult(Utilities.Alert.INFO, "Uploading History..", "Uploading History for connected account! This may take a minute. Please wait..", alertButtonInterface);
                long[] cardNumbers = Utilities.convertToPrimitiveLongArray(cardsToBeUploaded);
                String[] emailID = new String[1];
                emailID[0] = currentUserEmailID;
                LiveData<List<LocalStorage.CardWithAlbums>> cardsForUsers = muzifyViewModel.getSpecificCardDataForSpecificUsersWithAlbums(emailID, cardNumbers);
                cardsForUsers.observe((LifecycleOwner) activity, new Observer<List<LocalStorage.CardWithAlbums>>() {
                    @Override
                    public void onChanged(List<LocalStorage.CardWithAlbums> cardWithAlbums) {
                        cardsForUsers.removeObserver(this);
                        ArrayList<LocalStorage.Card> simplifiedCards = LocalStorage.CardWithAlbums.convertToCards(cardWithAlbums);
                        int count = 0;
                        for(LocalStorage.Card c : simplifiedCards){
                            //updating our local card - upload flag check update
                            try{
                                LocalStorage.Card copy = (LocalStorage.Card) c.clone();
                                copy.albums = null;
                                copy.uploadFlag = true;
                                muzifyViewModel.update(copy);
                            }catch (Exception ignored){}

                            //shareFlag should be ignored - it may be a shared card so no need to reset
                            c.uploadFlag = false;
                            c.downloadFlag = false;
                            uploadCard(muzifyViewModel, c, activity);
                            count++;
                        }
                        alert.tempFlag = true;
                        alert.showInfoResult(Utilities.Alert.INFO, "Upload Successful!", "Awesome, "+count+" item(s) has been uploaded!", alertButtonInterface);
                    }
                });
            }
        });
    }




    public static void uploadSharedUserDetails(String shareCode, MuzifyAccount account){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(Keys.SHARES);
        myRef.child(shareCode).child(SHARED_USER_DETAILS).child(account.st+"_"+encodeUserEmail(account.getEmailID())).setValue(account);
    }


    public static void refreshSharedUserDetails(String shareCode, MuzifyViewModel muzifyViewModel, Activity activity, List<LocalStorage.ShareInfo> shareInfos, InnerCardActivity callingActivity)
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(Keys.SHARES);
        myRef.child(shareCode).child(SHARED_USER_DETAILS).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.getResult() == null){
                    callingActivity.stopLoading();
                    return;
                }
                boolean dataUpdated = false;
                DataSnapshot snapshot = task.getResult();
                HashMap<String,HashMap> dataHash = (HashMap<String, HashMap>) snapshot.getValue();
                if(dataHash == null){
                    callingActivity.stopLoading();
                    return;
                }
                Set<String> keys = dataHash.keySet();
                for(String key : keys){
                    MuzifyAccount account = MuzifyAccount.get(dataHash.get(key));
                    try {
                        if(LocalStorage.ShareInfo.isSharedUserDataExists(account, shareInfos)){
                            continue;
                        }
                        muzifyViewModel.insert(new LocalStorage.ShareInfo(account.shareCode, account.getUserName(), account.getEmailID(), account.dsc, account.st, account.getProfilePictureURL(), account.oe));
                        dataUpdated = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if(dataUpdated){
                    Utilities.Loggers.showLongToast(activity.getApplicationContext(), "New shared user details downloaded! Re-open this history to view");
                }
                callingActivity.stopLoading();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Utilities.Loggers.showLongToast(activity.getApplicationContext(), "Unable to refresh shared user details for share code : "+shareCode);
            }
        });
    }


    public static void refreshSharedUserDetailsV2(String shareCode, MuzifyViewModel muzifyViewModel, List<LocalStorage.ShareInfo> shareInfos, InnerCardActivity callingActivity)
    {
        List<LocalStorage.ShareInfo> totalShareInfos = new ArrayList<>();
        List<LocalStorage.ShareInfo> newShareInfos = new ArrayList<>();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(Keys.SHARES);
        if(shareCode == null){
            return;
        }
        myRef.child(shareCode).child(SHARED_USER_DETAILS).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.getResult() == null){
                    callingActivity.handleRefreshShareCallbackV2(new ArrayList<>());
                    return;
                }
                DataSnapshot snapshot = task.getResult();
                HashMap<String,HashMap> dataHash = (HashMap<String, HashMap>) snapshot.getValue();
                if(dataHash == null){
                    callingActivity.handleRefreshShareCallbackV2(new ArrayList<>());
                    return;
                }
                Set<String> keys = dataHash.keySet();
                for(String key : keys){
                    MuzifyAccount account = MuzifyAccount.get(dataHash.get(key));
                    LocalStorage.ShareInfo shareInfo = new LocalStorage.ShareInfo(account.shareCode, account.getUserName(), account.getEmailID(), account.dsc, account.st, account.getProfilePictureURL(), account.oe);
                    totalShareInfos.add(shareInfo);
                    try {
                        if(LocalStorage.ShareInfo.isSharedUserDataExists(account, shareInfos)){
                            continue;
                        }
                        newShareInfos.add(shareInfo);
                        muzifyViewModel.insert(shareInfo);
                        Utilities.Loggers.postInfoLog("SHARE_INFO_INSERT", "Shareinfo inserted!");
                    } catch (Exception e) {
                        e.printStackTrace();
                        Utilities.Loggers.postInfoLog("SHARED_USER_DETAILS_INSERT", "Error occurred while adding share info");
                    }
                }
                callingActivity.handleRefreshShareCallbackV2(totalShareInfos);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Utilities.Loggers.showLongToast(callingActivity, "Unable to refresh shared user details for share code : "+shareCode);
            }
        });
    }



    public static void checkMusicServiceEnabled(MuzifySharedMemory muzifySharedMemory, Utilities.MusicService service, String defaultAccount, TransferAuthenticationInterface callingActivity) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(Keys.ACCOUNTS);
        myRef.child(encodeUserEmail(defaultAccount)).child(Keys.MUSIC_SERVICES).child(service.getFormattedServiceName()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.getResult().getValue() == null){
                    callingActivity.handleMusicServiceEnableCheck(true,false, defaultAccount, null);
                }else{
                    String connectedAccountEmail = task.getResult().getValue(String.class);
                    muzifySharedMemory.enableMusicService(service, defaultAccount, connectedAccountEmail);
                    callingActivity.handleMusicServiceEnableCheck(true,true, defaultAccount, connectedAccountEmail);
                }
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                callingActivity.handleMusicServiceEnableCheck(false,false, null, null);
            }
        });
    }

    private static String generateKeyForServiceAccessRequest(String accountEmail, Utilities.MusicService service){
        return service.getFormattedServiceName()+"___"+encodeUserEmail(accountEmail);
    }

    public static void checkIfServiceAccessRequestRaised(Utilities.MusicService service, String accountEmail, TransferAuthenticationInterface callingActivity) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(Keys.SERVICE_ACCESS_REQUESTS);
        myRef.child(generateKeyForServiceAccessRequest(accountEmail, service)).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.getResult().getValue() == null){
                    //new request
                    callingActivity.handleServiceAccessRequestRaiseCheck(true, false, null, null);
                }else{
                    //existing request
                    HashMap dataHash = (HashMap) task.getResult().getValue();
                    MusicServices.MusicServiceRequest serviceRequest = MusicServices.MusicServiceRequest.get(dataHash);
                    callingActivity.handleServiceAccessRequestRaiseCheck(true, true, serviceRequest.requestDate, serviceRequest.developerMessage);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                callingActivity.handleServiceAccessRequestRaiseCheck(false, false, null, null);
            }
        });
    }

    public static void addMusicServiceRequest(Utilities.MusicService service, String accountEmailID, String requestingEmailID, TransferAuthenticationInterface callingActivity){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(Keys.SERVICE_ACCESS_REQUESTS);
        String baseKey = generateKeyForServiceAccessRequest(accountEmailID, service);
        myRef.child(baseKey).child("requestDate").setValue(Utilities.convertTimeStampToDate(System.currentTimeMillis()));
        myRef.child(baseKey).child("requestedAccountEmail").setValue(requestingEmailID).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                callingActivity.handleAddServiceAccessRequest(true);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                callingActivity.handleAddServiceAccessRequest(false);
            }
        });
    }

}




