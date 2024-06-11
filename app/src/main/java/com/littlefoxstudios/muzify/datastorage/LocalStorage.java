package com.littlefoxstudios.muzify.datastorage;

import static com.littlefoxstudios.muzify.Utilities.convertListToString;
import static com.littlefoxstudios.muzify.Utilities.convertStringToList;

import androidx.lifecycle.LiveData;
import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Insert;
import androidx.room.PrimaryKey;
import androidx.room.Query;
import androidx.room.Relation;
import androidx.room.Update;

import com.littlefoxstudios.muzify.Constants;
import com.littlefoxstudios.muzify.Utilities;
import com.littlefoxstudios.muzify.accounts.MuzifyAccount;
import com.littlefoxstudios.muzify.homescreenfragments.history.innercard.Album;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class LocalStorage {

public static final int MAXIMUM_ATTEMPTS = 3;
private static final long ONE_DAY_IN_MILLIS = 86400000l;

@Entity
public static class UserData
{
    public UserData(String emailID, String userName, String userProfilePictureURL) {
        this.emailID = emailID;
        this.userName = userName;
        this.nextUploadTime = System.currentTimeMillis() + ONE_DAY_IN_MILLIS; //1 day interval
        this.isSynced = true;
        this.attemptsLeft = MAXIMUM_ATTEMPTS;
        this.cardsToBeDeleted = "";
        this.cardsToBeUploaded = "";
        this.userProfilePictureURL = userProfilePictureURL;
    }

    @PrimaryKey @NotNull
    public String emailID;
    public String userName;
    int attemptsLeft;
    boolean isSynced; //set this to false whenever a new card is added
    public long nextUploadTime;
    public String cardsToBeDeleted;
    public String cardsToBeUploaded;
    public String userProfilePictureURL;


    public ArrayList<Long> getCardsToBeDeleted() {
        return convertStringToList(cardsToBeDeleted);
    }

    public ArrayList<Long> getCardsToBeUploaded() {
        return convertStringToList(cardsToBeUploaded);
    }

    public void addCardToBeDeleted(long cardNumber){
        if(cardsToBeDeleted == null){cardsToBeDeleted = "";}
        ArrayList<Long> list = convertStringToList(cardsToBeDeleted);
        list.add(cardNumber);
        if(convertStringToList(cardsToBeUploaded).size() != 0){
            ArrayList<Long> list2 = convertStringToList(cardsToBeUploaded);
            list2.remove(cardNumber);
            cardsToBeUploaded = convertListToString(list2);
        }
        cardsToBeDeleted = convertListToString(list);
    }

    public String getUserProfilePictureURL()
    {
        return userProfilePictureURL;
    }

    public void addCardToBeUploaded(long cardNumber){
        if(cardsToBeUploaded == null){
            cardsToBeUploaded = "";
        }
        if(!convertStringToList(cardsToBeUploaded).contains(cardNumber)){
           ArrayList<Long> list = convertStringToList(cardsToBeUploaded);
           list.add(cardNumber);
           cardsToBeUploaded = convertListToString(list);
        }
    }

    public void checkAndSync() throws Exception
    {
        if(System.currentTimeMillis() > getNextUploadTime()){
            resetAttempts();
            updateUploadTime();
        }

        if(isSynced){
            throw new Exception("Data already synced with the cloud!");
        }
        if(attemptsLeft <= 0){
            throw new Exception("Maximum attempts for a day already reached! Only "+MAXIMUM_ATTEMPTS+" attempts per day");
        }
        useAttempt();
        setAsSynced();
    }

    private void resetAttempts(){
        attemptsLeft = MAXIMUM_ATTEMPTS;
    }

    private void updateUploadTime(){
        nextUploadTime = System.currentTimeMillis() + ONE_DAY_IN_MILLIS;
    }

    public void useAttempt(){
        attemptsLeft--;
    }

    public void setSyncNeeded(){
        isSynced = false;
    }

    public void setAsSynced(){
        isSynced = true;
    }

    public String getEmailID() {
        return emailID;
    }

    public String getUserName()
    {
        return userName;
    }

    public long getNextUploadTime() {
        return nextUploadTime;
    }
}


@Dao
public interface UserDataDAO
{
    @Insert
    void insert(UserData userData);

    @Update
    void update(UserData userData);

    @Delete
    void delete(UserData userData);

    @Query("SELECT * FROM UserData")
    LiveData<List<UserData>> getAllUserData();

    @Query("SELECT * FROM UserData WHERE emailID = :emailID")
    LiveData<List<UserData>> getUserDataForSpecificUser(String emailID);
}

@Entity(foreignKeys = {@ForeignKey(entity = UserData.class,
        parentColumns = "emailID",
        childColumns = "emailID",
        onDelete = ForeignKey.CASCADE)
})
public static class Card implements Cloneable
{


    public Card(String emailID, long cardNumber, String playlistTitle, int totalItemsInPlaylist,
                int failedItemsInPlaylist, String sourceAccountEmail,
                String destinationAccountEmail, String sourceAccountName,
                int sourceServiceCode, int destinationServiceCode,
                String playlistImageUrls, String currentTime, String destinationAccountName,
                String shareCode, String sourcePlaylistURL, String destinationPlaylistURL,
                int muziShareSourceServiceCode, String sourceAccountProfilePictureURL, String destinationAccountProfilePictureURL) {
        this.emailID = emailID;
        this.cardNumber = cardNumber;
        this.playlistTitle = playlistTitle;
        this.totalItemsInPlaylist = totalItemsInPlaylist;
        this.failedItemsInPlaylist = failedItemsInPlaylist;
        this.sourceAccountEmail = sourceAccountEmail;
        this.destinationAccountEmail = destinationAccountEmail;
        this.sourceAccountName = sourceAccountName;
        this.sourceServiceCode = sourceServiceCode;
        this.destinationServiceCode = destinationServiceCode;
        this.playlistImageUrls = playlistImageUrls;
        this.currentTime = currentTime;
        this.destinationAccountName = destinationAccountName;
        this.shareCode = shareCode;
        this.nextSync = 0L;
        this.uploadFlag = false;
        this.downloadFlag = false;
        this.shareFlag = false;
        this.sourcePlaylistURL = sourcePlaylistURL;
        this.destinationPlaylistURL = destinationPlaylistURL;
        this.muziShareSourceServiceCode = muziShareSourceServiceCode;
        this.sourceAccountProfilePictureURL = sourceAccountProfilePictureURL;
        this.destinationAccountProfilePictureURL = destinationAccountProfilePictureURL;
    }

    public String emailID;
    @PrimaryKey @NotNull
    public long cardNumber;

    public static Card convertShareHashDataToObject(HashMap<String, HashMap> dataHash){
        HashMap<String,HashMap> newHash = new HashMap<>();
        newHash.put(dataHash.get("cardNumber")+"", new HashMap(dataHash));
        List<Card> card = convertHashDataToObject(newHash);
        return card != null ? card.get(0) : null;
    }

    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }


    private static Card getCardFromHash(HashMap hash)
    {
        Card c = new Card((String) hash.get(Constants.Keys.HistoryKeys.EMAIL_ID),
                (long) hash.get("cardNumber"),
                (String) hash.get(Constants.Keys.HistoryKeys.PLAY_LIST_TITLE),
                (Integer.parseInt(hash.get(Constants.Keys.HistoryKeys.TOTAL_ITEMS_IN_PLAYLIST)+"")),
                (Integer.parseInt(hash.get(Constants.Keys.HistoryKeys.FAILED_ITEMS_IN_PLAYLIST)+"")),
                (String) hash.get(Constants.Keys.HistoryKeys.SOURCE_ACCOUNT_EMAIL),
                (String) hash.get(Constants.Keys.HistoryKeys.DESTINATION_ACCOUNT_EMAIL),
                (String) hash.get(Constants.Keys.HistoryKeys.SOURCE_ACCOUNT_NAME),
                (Integer.parseInt(hash.get(Constants.Keys.HistoryKeys.SOURCE_SERVICE_CODE)+"")),
                (Integer.parseInt (hash.get(Constants.Keys.HistoryKeys.DESTINATION_SERVICE_CODE)+"")),
                (String) hash.get(Constants.Keys.HistoryKeys.PLAYLIST_IMAGE_URLS),
                (String) hash.get("currentTime"),
                (String) hash.get(Constants.Keys.HistoryKeys.DESTINATION_ACCOUNT_NAME),
                (String) hash.get("shareCode"),
                (String) hash.get(Constants.Keys.HistoryKeys.SOURCE_PLAYLIST_URL),
                (String) hash.get(Constants.Keys.HistoryKeys.DESTINATION_PLAYLIST_URL),
                (Integer.parseInt(hash.get(Constants.Keys.HistoryKeys.MUZI_SHARE_SOURCE_SERVICE_CODE)+"")),
                (String) hash.get("sourceAccountProfilePictureURL"),
                (String) hash.get("destinationAccountProfilePictureURL")
        );
        if(hash.get("albums") != null){
            c.albums = Album.convertHashDataToObject((List<HashMap>)hash.get("albums"));
        }
        if(c.playlistTitle == null || c.playlistTitle.equals("null")){
            //playlist title and image urls have different key
            c.playlistTitle = (String) hash.get("playlistTitle");
            c.playlistImageUrls = convertListToString((ArrayList<String>)  hash.get("playlistImageUrls"));
        }
        //share flag update
        try{
            c.shareFlag = (Boolean) hash.get("shareFlag");
        }catch (Exception ignored){}
        return c;
    }

    @Nullable
    public static List<Card> convertHashDataToObject(HashMap<String, HashMap> dataHash) {
        List<Card> cards = new ArrayList<>();
        if(dataHash == null || dataHash.size() == 0){
            return cards;
        }
        Set<String> cardNumbers = dataHash.keySet();
        for(String cardNumber : cardNumbers){
            HashMap hash = (HashMap) dataHash.get(cardNumber);
            cards.add(getCardFromHash(hash));
        }
        return cards;
    }

    public String getEmailID() {
        return emailID;
    }

    public long getCardNumber() {
        return cardNumber;
    }

    public String getPlaylistTitle() {
        return playlistTitle;
    }

    public int getTotalItemsInPlaylist() {
        return totalItemsInPlaylist;
    }

    public int getFailedItemsInPlaylist() {
        return failedItemsInPlaylist;
    }

    public String getSourceAccountEmail() {
        return sourceAccountEmail;
    }

    public String getDestinationAccountEmail() {
        return destinationAccountEmail;
    }

    public String getSourceAccountName() {
        return sourceAccountName;
    }

    public int getSourceServiceCode() {
        return sourceServiceCode;
    }

    public int getDestinationServiceCode() {
        return destinationServiceCode;
    }

    public ArrayList<String> getPlaylistImageUrls() {
        return convertStringToList(playlistImageUrls);
    }

    public void setDestinationAccountName(String destinationAccountName)
    {
        this.destinationAccountName = destinationAccountName;
    }

    public String getDestinationAccountName(){
        return this.destinationAccountName;
    }

    public void setShareCode(String shareCode){
        this.shareCode = shareCode;
    }

    public void setCardNumber(long cardNumber){
        this.cardNumber = cardNumber;
    }

    public String getShareCode(){
        return (shareCode == null) ? "" : shareCode;
    }

    public String getCreatedTime(){return currentTime;}

    @ColumnInfo (name = Constants.Keys.HistoryKeys.PLAY_LIST_TITLE)
    public String playlistTitle;
    @ColumnInfo (name = Constants.Keys.HistoryKeys.TOTAL_ITEMS_IN_PLAYLIST)
    public int totalItemsInPlaylist;
    @ColumnInfo (name = Constants.Keys.HistoryKeys.FAILED_ITEMS_IN_PLAYLIST)
    public int failedItemsInPlaylist;
    @ColumnInfo (name = Constants.Keys.HistoryKeys.SOURCE_ACCOUNT_EMAIL)
    public String sourceAccountEmail;
    @ColumnInfo (name = Constants.Keys.HistoryKeys.DESTINATION_ACCOUNT_EMAIL)
    public String destinationAccountEmail;
    @ColumnInfo (name = Constants.Keys.HistoryKeys.SOURCE_ACCOUNT_NAME)
    public String sourceAccountName;
    @ColumnInfo (name = Constants.Keys.HistoryKeys.DESTINATION_ACCOUNT_NAME)
    public String destinationAccountName;
    @ColumnInfo (name = Constants.Keys.HistoryKeys.SOURCE_SERVICE_CODE)
    public int sourceServiceCode;
    @ColumnInfo (name = Constants.Keys.HistoryKeys.DESTINATION_SERVICE_CODE)
    public int destinationServiceCode;
    @ColumnInfo (name = Constants.Keys.HistoryKeys.PLAYLIST_IMAGE_URLS)
    public String playlistImageUrls;
    public String currentTime;
    public String shareCode;
    @ColumnInfo (name = Constants.Keys.HistoryKeys.NEXT_SHARED_USERS_DATA_SYNC)
    public long nextSync;
    public boolean uploadFlag;
    public boolean downloadFlag;
    public boolean shareFlag;

    public String sourcePlaylistURL;
    public String destinationPlaylistURL;
    @ColumnInfo (name = Constants.Keys.HistoryKeys.MUZI_SHARE_SOURCE_SERVICE_CODE)
    public int muziShareSourceServiceCode;
    public String sourceAccountProfilePictureURL;
    public String destinationAccountProfilePictureURL;

    public String getSourceAccountProfilePictureURL() {
        return sourceAccountProfilePictureURL;
    }

    public String getDestinationAccountProfilePictureURL() {
        return destinationAccountProfilePictureURL;
    }

    @Ignore
    public List<Album> albums; //Use it only for cloud export. Do not use locally

    public boolean isSharedUserDataSyncAvailable(){
        //return this.nextSync >= System.currentTimeMillis();
        return true;
    }

    public void updateSharedUserDataSync(){
        this.nextSync = System.currentTimeMillis() + ONE_DAY_IN_MILLIS;
    }

    public String getSourcePlaylistURL(){
        return this.sourcePlaylistURL;
    }

    public String getDestinationPlaylistURL(){
        return this.destinationPlaylistURL;
    }

}



@Dao
public interface CardDAO
{
    @Insert
    void insert(Card card);

    @Update
    void update(Card card);

    @Delete
    void
    delete(Card card);

    @Query("SELECT * FROM Card WHERE emailID = :emailID ORDER BY cardNumber DESC")
    LiveData<List<Card>> getCardsForSpecificUser(String emailID);

    @Query("SELECT * FROM Card WHERE cardNumber = :cardNumber")
    Card getCard(long cardNumber);
}


@Entity (foreignKeys = {@ForeignKey(entity = Card.class,
        parentColumns = "cardNumber",
        childColumns = "cardNumber",
        onDelete = ForeignKey.CASCADE)
})


public static class Album
{


    public Album(long cardNumber, String albumName, String albumCoverURL, String albumArtist, String albumSongName, int processedBy, String songID, String sourceSongID) {
        this.cardNumber = cardNumber;
        this.albumSongName = albumSongName;
        this.albumCoverURL = albumCoverURL;
        this.albumArtist = albumArtist;
        this.albumName = albumName;
        this.processedBy = processedBy;
        this.songID = songID;
        this.sourceSongID = sourceSongID;
    }

    public static List<Album> convertHashDataToObject(List<HashMap> albums) {
        List<Album> list = new ArrayList<>();
        if(albums == null || albums.size() == 0){
            return list;
        }
        for(HashMap map : albums){
            Album a = new Album(
                    (long) map.get("cardNumber"),
                    (String) map.get(Constants.Keys.HistoryKeys.ALBUM_NAME),
                    (String) map.get(Constants.Keys.HistoryKeys.ALBUM_COVER_URL),
                    (String) map.get(Constants.Keys.HistoryKeys.ALBUM_ARTIST),
                    (String) map.get(Constants.Keys.HistoryKeys.ALBUM_SONG_NAME),
                    Integer.parseInt(map.get(Constants.Keys.HistoryKeys.PROCESSED_BY)+""),
                    (String) map.get("songID"),
                    (String) map.get("sourceSongID")
            );
          a.isSongFailed = a.getIsSongFailed();
          a.albumID = Integer.parseInt(map.get("albumID")+"");
          list.add(a);
        }
        return list;
    }

    public String getSongID(){
        return songID == null ? "" : songID;
    }

    public long getCardNumber() {
        return cardNumber;
    }

    private String appendHyphenIfEmpty(String text){
        return (text == null || text.length() == 0) ? "-" : text;
    }

    public String getAlbumSongName() {
        return appendHyphenIfEmpty(albumSongName);
    }

    public String getAlbumCoverURL() {
        return appendHyphenIfEmpty(albumCoverURL);
    }

    public String getAlbumArtist() {
        return appendHyphenIfEmpty(albumArtist);
    }

    public boolean getIsSongFailed()
    {
        return com.littlefoxstudios.muzify.homescreenfragments.history.innercard.Album.ProcessedBy.isFailedSong(processedBy);
    }

    public String getAlbumName() {
        return appendHyphenIfEmpty(albumName);
    }

    public int getProcessedBy() {
        return processedBy;
    }

    public String getProcessedByText() {
     try{
         return com.littlefoxstudios.muzify.homescreenfragments.history.innercard.Album.ProcessedBy.getProcessedByObj(getProcessedBy()).getProcessedByText();
     }catch (Exception e){
         return "";
     }
    }

    public String getSourceSongID(){
        return Utilities.validateNullString(sourceSongID);
    }

    public String getDestinationSongID(){
        return Utilities.validateNullString(songID);
    }

    public boolean isSourceSelected(){
        return isSourceSelected;
    }


    @PrimaryKey(autoGenerate = true)
    public int albumID;
    public long cardNumber;
    @ColumnInfo (name = Constants.Keys.HistoryKeys.ALBUM_SONG_NAME)
    public String albumSongName;
    @ColumnInfo (name = Constants.Keys.HistoryKeys.ALBUM_COVER_URL)
    public String albumCoverURL;
    @ColumnInfo (name = Constants.Keys.HistoryKeys.ALBUM_ARTIST)
    public String albumArtist;
    @ColumnInfo (name = Constants.Keys.HistoryKeys.ALBUM_NAME)
    public String albumName;
    @ColumnInfo (name = Constants.Keys.HistoryKeys.PROCESSED_BY)
    public int processedBy;
    public String songID; //destinationSongID
    //V1.2
    public String sourceSongID;

    @Ignore
    public boolean isSongFailed;
    @Ignore
    public boolean isSourceSelected = true;
}

@Dao
    public interface AlbumDAO
    {
        @Insert
        void insert(Album album);

        @Update
        void update(Album album);

        @Delete
        void delete(Album album);

        @Query("SELECT * FROM Album WHERE cardNumber = :cardNumber")
        LiveData<List<Album>> getAlbumsForSpecificCard(long cardNumber);


    }


@Entity
public static class ShareInfo
{
    public ShareInfo(String shareCode, String userName, String emailID, int sharedDestinationCode, long sharedTime, String profilePictureURL, String ownerEmailID) {
        this.shareCode = shareCode;
        this.userName = userName;
        this.emailID = emailID;
        this.sharedDestinationCode = sharedDestinationCode;
        this.sharedTime = sharedTime;
        this.profilePictureURL = profilePictureURL;
        this.ownerEmailID = ownerEmailID;
    }

    @PrimaryKey(autoGenerate = true)
    public int shareInfoID;
    public String shareCode;
    @ColumnInfo (name = Constants.Keys.HistoryKeys.USER_NAME)
    public String userName;
    @ColumnInfo (name = Constants.Keys.HistoryKeys.EMAIL_ID)
    public String emailID;
    @ColumnInfo (name = Constants.Keys.HistoryKeys.DESTINATION_SERVICE_CODE)
    public int sharedDestinationCode;
    @ColumnInfo (name = Constants.Keys.HistoryKeys.SHARED_TIME)
    public long sharedTime;
    public String profilePictureURL;
    public String ownerEmailID;

    public String getShareCode() {
        return shareCode;
    }

    public String getUserName(){
        return userName;
    }

    public String getEmailID(){
        return emailID;
    }

    public int getSharedDestinationCode() {
        return sharedDestinationCode;
    }

    public long getSharedTime() {
        return sharedTime;
    }

    public String getProfilePictureURL()
    {
        return profilePictureURL;
    }

    public String getOwnerEmailID(){ return ownerEmailID; }

    private static String constructChainData(String userName, String profilePictureURL, int sharedDestinationCode, String emailID, String ownerEmailID, long sharedTime, String shareCode){
        return userName+"_"+profilePictureURL+"_"+sharedDestinationCode+"_"+emailID+"_"+ownerEmailID+"_"+sharedTime+"_"+shareCode;
    }

    public static boolean isSharedUserDataExists(MuzifyAccount account, List<ShareInfo> shareInfos){
        ArrayList<String> chainedData = new ArrayList<>();
        for(ShareInfo shareInfo : shareInfos){
           chainedData.add(constructChainData(shareInfo.getUserName(), shareInfo.getProfilePictureURL(), shareInfo.getSharedDestinationCode(), shareInfo.getEmailID(), shareInfo.getOwnerEmailID(), shareInfo.getSharedTime(), shareInfo.getShareCode()));
        }
        String chainedAccountInfo = constructChainData(account.getUserName(), account.getProfilePictureURL(), account.dsc, account.getEmailID(), account.oe, account.st, account.shareCode);
        return chainedData.contains(chainedAccountInfo);
    }
}

@Dao
    public interface ShareInfoDAO
    {
        @Insert
        void insert(ShareInfo shareInfo);

        @Update
        void update(ShareInfo shareInfo);

        @Delete
        void delete(ShareInfo shareInfo);

        @Query("SELECT * FROM ShareInfo WHERE shareCode = :shareCode")
        LiveData<List<ShareInfo>> getShareInfoForSpecificCard(String shareCode);
    }


    public static class CardWithAlbums
    {
        @Embedded
        public Card card;

        @Relation(parentColumn = "cardNumber", entityColumn = "cardNumber", entity = Album.class)
        public List<Album> albums;

        public Card getData()
        {
            card.albums = albums;
            return card;
        }

        public static ArrayList<Card> convertToCards(List<CardWithAlbums> cwa){
            ArrayList<Card> cardsList = new ArrayList<>();
            for(CardWithAlbums cards : cwa){
                Card c = cards.card;
                c.albums = cards.albums;
                cardsList.add(c);
            }
            return cardsList;
        }
    }

    @Dao
    public interface CardWithAlbumsDAO
    {
        @Query("SELECT * FROM Card WHERE emailID = :emailID")
        List<CardWithAlbums> getCardsWithAlbums(String emailID);

        @Query("SELECT * FROM Card WHERE emailID IN (:emailIDs) AND CardNumber IN (:cardNumbers) ORDER BY cardNumber DESC")
        LiveData<List<CardWithAlbums>> getSpecificCardsForSpecificUsersWithAlbums(String[] emailIDs, long[] cardNumbers);
    }


}
