package com.littlefoxstudios.muzify.datastorage;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MuzifyRepository {
    private  LocalStorage.UserDataDAO userDataDAO;
    private  LocalStorage.CardDAO cardDAO;
    private  LocalStorage.AlbumDAO albumDAO;
    private  LocalStorage.ShareInfoDAO shareInfoDAO;
    private  LocalStorage.CardWithAlbumsDAO CardWithAlbumsDAO;

    private  LiveData<List<LocalStorage.UserData>> allUserData;


    static final String INSERT = "insert";
    static final String UPDATE = "update";
    static final String DELETE = "delete";

    public MuzifyRepository(Application application, boolean forceUserRefresh){
        MuzifyRoomDatabase roomDB = MuzifyRoomDatabase.getInstance(application);

        //initializing dao
        this.userDataDAO = userDataDAO != null ? userDataDAO : roomDB.userDataDAO();
        this.cardDAO = cardDAO != null ? cardDAO : roomDB.cardDAO();
        this.albumDAO = albumDAO != null ? albumDAO : roomDB.albumDAO();
        this.shareInfoDAO = shareInfoDAO != null ? shareInfoDAO : roomDB.shareInfoDAO();
        this.CardWithAlbumsDAO = CardWithAlbumsDAO != null ? CardWithAlbumsDAO : roomDB.CardWithAlbumsDAO();

        //initializing all users data
        if(forceUserRefresh){
            allUserData = userDataDAO.getAllUserData();
        }
        allUserData = allUserData != null ? allUserData : userDataDAO.getAllUserData();
    }

    public interface RoomOperationsInterface<T>{
        void insert(T value);
        void update(T value);
        void delete(T value);
        LiveData<List<T>> getAppropriateSpecificData(String searchKey);
    }


    public class UserDataOperations implements RoomOperationsInterface<LocalStorage.UserData> {
        public void insert(LocalStorage.UserData userData){
            new UserDataAsyncTask(userDataDAO, INSERT).execute(userData);
        }
        public void update(LocalStorage.UserData userData){
            new UserDataAsyncTask(userDataDAO, UPDATE).execute(userData);
        }
        public void delete(LocalStorage.UserData userData){
            new UserDataAsyncTask(userDataDAO, DELETE).execute(userData);
        }

        @Override
        public LiveData<List<LocalStorage.UserData>> getAppropriateSpecificData(String searchKey) {
            return userDataDAO.getUserDataForSpecificUser(searchKey);
        }

        public LiveData<List<LocalStorage.UserData>> getAllUserData(){
            return allUserData;
        }

        private class UserDataAsyncTask extends AsyncTask<LocalStorage.UserData, Void, Void>{
            private LocalStorage.UserDataDAO userDataDAO;
            String currentOperation;
            private UserDataAsyncTask(LocalStorage.UserDataDAO userDataDAO, String operation){
                this.userDataDAO = userDataDAO;
                this.currentOperation = operation;
            }

            @Override
            protected Void doInBackground(LocalStorage.UserData... userData) {
               switch (currentOperation)
               {
                   case INSERT: userDataDAO.insert(userData[0]); break;
                   case UPDATE: userDataDAO.update(userData[0]); break;
                   case DELETE: userDataDAO.delete(userData[0]); break;
                   default: break;
               }
                return null;
            }
        }
    }

    public class CardOperations implements RoomOperationsInterface<LocalStorage.Card>{
        public void insert(LocalStorage.Card card){
            new CardAsyncTask(cardDAO, INSERT).execute(card);
        }
        public void update(LocalStorage.Card card){
            new CardAsyncTask(cardDAO, UPDATE).execute(card);
        }
        public void delete(LocalStorage.Card card){
            new CardAsyncTask(cardDAO, DELETE).execute(card);
        }

        @Override
        public LiveData<List<LocalStorage.Card>> getAppropriateSpecificData(String searchKey) {
            return cardDAO.getCardsForSpecificUser(searchKey);
        }

        public LiveData<List<LocalStorage.CardWithAlbums>> getSpecificCardsForSpecificUsersWithAlbums(String[] emailIDs, long[] cardNumbers){
            return CardWithAlbumsDAO.getSpecificCardsForSpecificUsersWithAlbums(emailIDs, cardNumbers);
        }

        public List<LocalStorage.CardWithAlbums> getCardsWithAlbums(String emailID) throws ExecutionException, InterruptedException {
            String[] params = new String[]{emailID};
            return new GetCardWithAlbumsAsyncTask().execute(params).get();
        }

        public List<LocalStorage.Card> getCardWithAlbums(String emailID) throws ExecutionException, InterruptedException {
            List<LocalStorage.CardWithAlbums> cwa = getCardsWithAlbums(emailID);
            List<LocalStorage.Card> cards = new ArrayList<>();
            for(LocalStorage.CardWithAlbums c : cwa){
                cards.add(c.getData());
            }
            return cards;
        }


        private class GetCardWithAlbumsAsyncTask extends AsyncTask<String, Void , List<LocalStorage.CardWithAlbums> >
        {
            protected List<LocalStorage.CardWithAlbums> doInBackground(String... params) {
                return CardWithAlbumsDAO.getCardsWithAlbums(params[0]);
            }
        }

        public LocalStorage.Card getCard(long cardNumber){
            return cardDAO.getCard(cardNumber);
        }

        private class CardAsyncTask extends AsyncTask<LocalStorage.Card, Void, Void>{
            private LocalStorage.CardDAO cardDAO;
            String currentOperation;
            private CardAsyncTask(LocalStorage.CardDAO cardDAO, String operation){
                this.cardDAO = cardDAO;
                this.currentOperation = operation;
            }

            @Override
            protected Void doInBackground(LocalStorage.Card... card) {
                switch (currentOperation)
                {
                    case INSERT: cardDAO.insert(card[0]); break;
                    case UPDATE: cardDAO.update(card[0]); break;
                    case DELETE: cardDAO.delete(card[0]); break;
                    default: break;
                }
                return null;
            }
        }
    }

    public class AlbumOperations implements RoomOperationsInterface<LocalStorage.Album>{
        public void insert(LocalStorage.Album album){
            new AlbumAsyncTask(albumDAO, INSERT).execute(album);
        }
        public void update(LocalStorage.Album album){
            new AlbumAsyncTask(albumDAO, UPDATE).execute(album);
        }
        public void delete(LocalStorage.Album album){
            new AlbumAsyncTask(albumDAO, DELETE).execute(album);
        }

        @Override
        public LiveData<List<LocalStorage.Album>> getAppropriateSpecificData(String searchKey) {
            return albumDAO.getAlbumsForSpecificCard(Long.parseLong(searchKey));
        }

        private class AlbumAsyncTask extends AsyncTask<LocalStorage.Album, Void, Void>{
            private LocalStorage.AlbumDAO albumDAO;
            String currentOperation;
            private AlbumAsyncTask(LocalStorage.AlbumDAO albumDAO, String operation){
                this.albumDAO = albumDAO;
                this.currentOperation = operation;
            }

            @Override
            protected Void doInBackground(LocalStorage.Album... album) {
                switch (currentOperation)
                {
                    case INSERT: albumDAO.insert(album[0]); break;
                    case UPDATE: albumDAO.update(album[0]); break;
                    case DELETE: albumDAO.delete(album[0]); break;
                    default: break;
                }
                return null;
            }
        }
    }

    public class ShareInfoOperations implements RoomOperationsInterface<LocalStorage.ShareInfo>{
        public void insert(LocalStorage.ShareInfo shareInfo){
            new ShareInfoAsyncTask(shareInfoDAO, INSERT).execute(shareInfo);
        }
        public void update(LocalStorage.ShareInfo shareInfo){
            new ShareInfoAsyncTask(shareInfoDAO, UPDATE).execute(shareInfo);
        }
        public void delete(LocalStorage.ShareInfo shareInfo){
            new ShareInfoAsyncTask(shareInfoDAO, DELETE).execute(shareInfo);
        }

        @Override
        public LiveData<List<LocalStorage.ShareInfo>> getAppropriateSpecificData(String searchKey) {
            return shareInfoDAO.getShareInfoForSpecificCard(searchKey);
        }

        private class ShareInfoAsyncTask extends AsyncTask<LocalStorage.ShareInfo, Void, Void> {
            private LocalStorage.ShareInfoDAO shareInfoDAO;
            String currentOperation;

            private ShareInfoAsyncTask(LocalStorage.ShareInfoDAO shareInfoDAO, String operation) {
                this.shareInfoDAO = shareInfoDAO;
                this.currentOperation = operation;
            }

            @Override
            protected Void doInBackground(LocalStorage.ShareInfo... shareInfo) {
                switch (currentOperation) {
                    case INSERT:
                        shareInfoDAO.insert(shareInfo[0]);
                        break;
                    case UPDATE:
                        shareInfoDAO.update(shareInfo[0]);
                        break;
                    case DELETE:
                        shareInfoDAO.delete(shareInfo[0]);
                        break;
                    default:
                        break;
                }
                return null;
            }
        }
    }
}
