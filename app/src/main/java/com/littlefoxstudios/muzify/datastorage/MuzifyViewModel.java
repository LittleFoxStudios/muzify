package com.littlefoxstudios.muzify.datastorage;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class MuzifyViewModel extends AndroidViewModel {
    private  MuzifyRepository repository;


    public MuzifyViewModel(@NonNull Application application) {
        super(application);
        repository = new MuzifyRepository(application, true);
    }


    private MuzifyRepository.RoomOperationsInterface getRepository(Object obj) throws Exception
    {
        if(obj instanceof LocalStorage.UserData){
           return repository.new UserDataOperations();
        }else if(obj instanceof LocalStorage.Card){
            return repository.new CardOperations();
        }else if(obj instanceof LocalStorage.Album){
            return repository.new AlbumOperations();
        }else if(obj instanceof LocalStorage.ShareInfo){
            return repository.new ShareInfoOperations();
        }
        throw new Exception("Unable to get repository");
    }


    public void insert(Object obj) throws Exception {
       getRepository(obj).insert(obj);
    }

    public void update(Object obj) throws Exception {
      getRepository(obj).update(obj);
    }

    public void delete(Object obj) throws Exception {
        getRepository(obj).delete(obj);
    }

    public LiveData<List<LocalStorage.UserData>> getAllUserData(){
       return repository.new UserDataOperations().getAllUserData();
    }

    public LiveData<List<LocalStorage.UserData>> getUserDataForSpecificUser(String emailID){
        return repository.new UserDataOperations().getAppropriateSpecificData(emailID);
    }

    public LiveData<List<LocalStorage.Card>> getCardDataForSpecificUser(String emailID){
        return repository.new CardOperations().getAppropriateSpecificData(emailID);
    }

    public LiveData<List<LocalStorage.CardWithAlbums>> getSpecificCardDataForSpecificUsersWithAlbums(String[] emailIDs, long[] cardNumbers){
        return repository.new CardOperations().getSpecificCardsForSpecificUsersWithAlbums(emailIDs, cardNumbers);
    }

    public LocalStorage.Card getCard(long cardNumber){
        return repository.new CardOperations().getCard(cardNumber);
    }

    public LiveData<List<LocalStorage.Album>> getAlbumDataForSpecificCard(long cardNumber){
        return repository.new AlbumOperations().getAppropriateSpecificData(String.valueOf(cardNumber));
    }

    public LiveData<List<LocalStorage.ShareInfo>> getShareInfoForSpecificCard(String shareInfo){
        return repository.new ShareInfoOperations().getAppropriateSpecificData(shareInfo);
    }

    public List<LocalStorage.Card> getCardsWithAlbums(String emailID) throws ExecutionException, InterruptedException {
        return repository.new CardOperations().getCardWithAlbums(emailID);
    }

    //DELETE

    public void deleteSelectedCards(ArrayList<LocalStorage.Card> cards){
        MuzifyRepository.CardOperations co = repository.new CardOperations();
        for(LocalStorage.Card c : cards){
            co.delete(c);
        }
    }

}
