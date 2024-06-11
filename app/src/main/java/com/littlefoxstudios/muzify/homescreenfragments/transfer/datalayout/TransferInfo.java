package com.littlefoxstudios.muzify.homescreenfragments.transfer.datalayout;

import com.littlefoxstudios.muzify.Utilities;
import com.littlefoxstudios.muzify.datastorage.LocalStorage;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.internal.Util;

public class TransferInfo {

    public static final int TRANSFER_SIDE = 1;
    public static final int RECEIVER_SIDE = 2;

    public String deepLinkShareCode;

    public Integer muziShareSourceServiceCode;
    private Integer sourceServiceCode;
    private Integer sharedOriginalSourceCode;
    private Integer destinationServiceCode;
    private String sourceAccountEmailID;
    private String destinationAccountEmailID;
    private String selectedPlaylistID; //use this for share card also
    private ArrayList<PlaylistItem> transferredPlaylistItems;

    private String sourceAccountName;
    private String destinationAccountName;

    //V1.2 Update - Do not mix with selectedPlaylistID
    private String sourcePlaylistURL;
    private String destinationPlaylistURL;
    private String sourceAccountUserProfilePicture;
    private String destinationAccountUserProfilePicture;

    private ArrayList<TransferDataCache> transferDataCaches;

    private HashMap<String, LocalStorage.Card> sharedPlaylistData = new HashMap<>();

    public TransferInfo()
    {
        this.transferDataCaches = new ArrayList<>();
    }

    public int getSourceServiceCode() {
        return sourceServiceCode;
    }

    public void setSourceServiceCode(int sourceServiceCode) {
        this.sourceServiceCode = sourceServiceCode;
    }

    public void setSourcePlaylistURL(String sourcePlaylistURL){
        this.sourcePlaylistURL = sourcePlaylistURL;
    }

    public void setDestinationPlaylistURL(String destinationPlaylistURL){
        this.destinationPlaylistURL = destinationPlaylistURL;
    }

    public String getSourcePlaylistURL(){
        return Utilities.validateNullString(sourcePlaylistURL);
    }

    public String getDestinationPlaylistURL(){
       return Utilities.validateNullString(destinationPlaylistURL);
    }

    public String getSourceAccountEmailID() {
        return sourceAccountEmailID;
    }

    public void setSourceAccountEmailID(String sourceAccountEmailID) {
        this.sourceAccountEmailID = sourceAccountEmailID;
    }

    public String getSourceAccountName() {
        return sourceAccountName;
    }

    public void setSourceAccountName(String sourceAccountName) {
        this.sourceAccountName = sourceAccountName;
    }

    public String getDestinationAccountName() {
        return destinationAccountName;
    }

    public void setDestinationAccountName(String destinationAccountName) {
        this.destinationAccountName = destinationAccountName;
    }

    public String getSelectedPlaylistID() {
        return selectedPlaylistID;
    }

    public void setSelectedPlaylistID(String selectedPlaylistID) {
        this.selectedPlaylistID = selectedPlaylistID;
    }

    public String getDestinationAccountEmailID() {
        return destinationAccountEmailID;
    }

    public void setDestinationAccountEmailID(String destinationAccountEmailID) {
        this.destinationAccountEmailID = destinationAccountEmailID;
    }

    public int getDestinationServiceCode() {
        return destinationServiceCode;
    }

    public void setDestinationServiceCode(int destinationServiceCode) {
        this.destinationServiceCode = destinationServiceCode;
    }


    public ArrayList<TransferDataCache> getTransferDataCaches() {
        return transferDataCaches;
    }

    public void setTransferDataCaches(ArrayList<TransferDataCache> transferDataCaches) {
        this.transferDataCaches = transferDataCaches;
    }

    public boolean isSharedTransfer(){
        return sourceServiceCode == Utilities.ServiceCode.MUZI_SHARE;
    }

    public String getSourceAccountUserProfilePicture() {
        return sourceAccountUserProfilePicture;
    }

    public void setSourceAccountUserProfilePicture(String sourceAccountUserProfilePicture) {
        this.sourceAccountUserProfilePicture = sourceAccountUserProfilePicture;
    }

    public String getDestinationAccountUserProfilePicture() {
        return destinationAccountUserProfilePicture;
    }

    public void setDestinationAccountUserProfilePicture(String destinationAccountUserProfilePicture) {
        this.destinationAccountUserProfilePicture = destinationAccountUserProfilePicture;
    }

    public Integer getSharedOriginalSourceCode(){
        return sharedOriginalSourceCode;
    }

    public void clearTransfer()
    {
        sourceServiceCode = null;
        destinationServiceCode = null;
        sourceAccountEmailID = null;
        destinationAccountEmailID = null;
        selectedPlaylistID = null;
        transferredPlaylistItems = null;
        sourceAccountName = null;
        destinationAccountName = null;
    }

    public void backButtonClear(int code)
    {
        if(sourceServiceCode != null && sourceServiceCode == code){
           sourceServiceCode = null;
           sourceAccountEmailID = null;
           sourceAccountName = null;
        }
        if(destinationServiceCode != null && destinationServiceCode == code){
           destinationServiceCode = null;
           destinationAccountEmailID = null;
           destinationAccountName = null;
        }
        selectedPlaylistID = null;
        transferredPlaylistItems = null;
    }


    public ArrayList<Playlist> getPlaylists(){
        return TransferDataCache.getPlaylists(sourceServiceCode, sourceAccountEmailID, getTransferDataCaches(), false);
    }

    @NotNull
    public Playlist getPlaylist()
    {
        ArrayList<Playlist> playlists = getPlaylists();
        for(Playlist p : playlists){
            if(p.getPlaylistID().equals(selectedPlaylistID)){
                return p;
            }
        }
        return null;
    }

    public ArrayList<PlaylistItem> getTransferredPlaylistItems(){
        return transferredPlaylistItems == null ? new ArrayList<>() : transferredPlaylistItems;
    }

    public ArrayList<PlaylistItem> getSourcePlaylistItems(){
        ArrayList<Playlist> playlists = getPlaylists();
        for(Playlist p : playlists){
            if(p.getPlaylistID().equals(selectedPlaylistID)){
                return new ArrayList<>(p.getPlaylistItems());
            }
        }
        return null;
    }

    public ArrayList<PlaylistItem> combineSourcePlaylistItemsWithTransferredPlaylistItems(ArrayList<PlaylistItem> sourcePlaylistItems, ArrayList<PlaylistItem> transferredPlaylistItems){
        for(PlaylistItem p : transferredPlaylistItems){
            String x = "";
        }
        return transferredPlaylistItems;
    }

    public void addTransferredPlaylistItem(String songName, String sourceSongID, String artistName, String thumbnailURL, int processedBy, String songID, int index)
    {
        transferredPlaylistItems = getTransferredPlaylistItems();
        transferredPlaylistItems.add( new PlaylistItem(songName, sourceSongID, artistName, thumbnailURL, processedBy, songID, index));
    }


    public void initializeSharedPlaylist(String currentShareCode, LocalStorage.Card card){
        this.selectedPlaylistID = currentShareCode; //using sharecode as playlist id
        sharedPlaylistData.put(currentShareCode, card);
        updateWithShareCardDetails(card);
    }

    public void updateWithShareCardDetails(LocalStorage.Card card){
        this.sourceServiceCode = Utilities.ServiceCode.MUZI_SHARE;
        this.muziShareSourceServiceCode = card.getSourceServiceCode();
        this.setSourceAccountEmailID(card.getSourceAccountEmail());
        this.sourceAccountName = card.getSourceAccountName();
        this.sharedOriginalSourceCode = card.getDestinationServiceCode();

        Playlist playlist = new Playlist(card.getShareCode(), card.getPlaylistTitle(), card.playlistImageUrls, card.getTotalItemsInPlaylist());
        List<LocalStorage.Album> albums  = card.albums;
        ArrayList<PlaylistItem> playlistItems = new ArrayList<>();
        int index = 0;
        for(LocalStorage.Album album : albums){
            playlistItems.add(new PlaylistItem(album.getAlbumSongName(), album.getSourceSongID(), album.getAlbumArtist(), album.getAlbumCoverURL(), album.getProcessedBy(), album.getSongID(), index++));
        }
        playlist.updatePlaylistItems(playlistItems);
        ArrayList<Playlist> playlists = new ArrayList<>();
        playlists.add(playlist);

        ArrayList<TransferDataCache> cache = TransferDataCache.updateTransferDataCache(sourceServiceCode, sourceAccountEmailID, playlists, transferDataCaches);
        this.setTransferDataCaches(cache);
    }

    public boolean isShareCodeAvailable(String shareCode)
    {
        return sharedPlaylistData.containsKey(shareCode);
    }

    public boolean isInvalidShareCode(String shareCode)
    {
        return sharedPlaylistData.get(shareCode) == null;
    }

    public void addInvalidShareCode(String shareCode)
    {
        sharedPlaylistData.put(shareCode, null);
    }

    public LocalStorage.Card getSharedCardDetails(String shareCode)
    {
        return sharedPlaylistData.get(shareCode);
    }
}
