package com.littlefoxstudios.muzify.homescreenfragments.history.innercard;

import androidx.lifecycle.LiveData;

import com.littlefoxstudios.muzify.R;
import com.littlefoxstudios.muzify.Utilities;
import com.littlefoxstudios.muzify.datastorage.LocalStorage;

import org.parceler.Parcel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import okhttp3.internal.Util;

@Parcel
public class InnerCardObj  {

    public static final int TOTAL_DISPLAY_BLOCKS_SUPPORTED = 5;

    public int albumFilterType = Album.FilterType.SHOW_ALL;
    long cardNumber;
    Utilities.MusicService source, destination, originalSource;
    Integer imageOne, imageTwo, imageThree, imageFour;
    String playListTitle;
    String sourceAccountName, destinationAccountName, sourceAccountEmail, destinationAccountEmail;
    int currentSlide = Slider.SLIDER_NO_1;

    private String sourcePlaylistURL;
    private String destinationPlaylistURL;
    private String sourceAccountProfilePicture;
    private String destinationAccountProfilePicture;

    public Utilities.MusicService getOriginalSourceCode()
    {
        return source.getCode() == Utilities.ServiceCode.MUZI_SHARE ? originalSource : source;
    }

    public ArrayList<String> playListImageURLs;

    public long getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(long cardNumber) {
        this.cardNumber = cardNumber;
    }

    public Utilities.MusicService getSource() {
        return source;
    }

    public void setSource(Utilities.MusicService source) {
        this.source = source;
    }

    public Utilities.MusicService getDestination() {
        return destination;
    }

    public void setDestination(Utilities.MusicService destination) {
        this.destination = destination;
    }

    public void setSourcePlaylistURL(String url){
        sourcePlaylistURL = url;
    }

    public void setDestinationPlaylistURL(String url){
        destinationPlaylistURL = url;
    }

    public String getSourcePlaylistURL(){
        return this.sourcePlaylistURL;
    }

    public String getDestinationPlaylistURL(){
        return this.destinationPlaylistURL;
    }

    public void setPlayListImageURLs(ArrayList<String> urls){
        this.playListImageURLs = urls;
    }

    public ArrayList<String> getPlayListImageURLs(){
        return playListImageURLs;
    }

    public Integer getImageOne() {
        return imageOne;
    }

    public void setImageOne(Integer imageOne) {
        this.imageOne = imageOne;
    }

    public Integer getImageTwo() {
        return imageTwo;
    }

    public void setImageTwo(Integer imageTwo) {
        this.imageTwo = imageTwo;
    }

    public Integer getImageThree() {
        return imageThree;
    }

    public void setImageThree(Integer imageThree) {
        this.imageThree = imageThree;
    }

    public Integer getImageFour() {
        return imageFour;
    }

    public void setImageFour(Integer imageFour) {
        this.imageFour = imageFour;
    }

    public String getPlayListTitle() {
        return playListTitle;
    }

    public void setPlayListTitle(String playListTitle) {
        this.playListTitle = playListTitle;
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

    public Slider getSliderDetails()
    {
        return Slider.getSliderDetails(this);
    }

    public String getSourceAccountEmail() {
        return sourceAccountEmail;
    }

    public void setSourceAccountEmail(String sourceAccountEmail) {
        this.sourceAccountEmail = sourceAccountEmail;
    }

    public String getDestinationAccountEmail() {
        return destinationAccountEmail;
    }

    public void setDestinationAccountEmail(String destinationAccountEmail) {
        this.destinationAccountEmail = destinationAccountEmail;
    }

    public static int getSlideNo() {
        return slideNo;
    }

    public static void setSlideNo(int slideNo) {
        InnerCardObj.slideNo = slideNo;
    }

    public int getTotalItemsInPlaylist() {
        return totalItemsInPlaylist;
    }

    public void setTotalItemsInPlaylist(int totalItemsInPlaylist) {
        this.totalItemsInPlaylist = totalItemsInPlaylist;
    }

    public int getTotalItemsFailed() {
        return totalItemsFailed;
    }

    public void setTotalItemsFailed(int totalItemsFailed) {
        this.totalItemsFailed = totalItemsFailed;
    }

    public String getSourceAccountDetails()
    {
        return getSourceAccountName()+" | "+getSourceAccountEmail();
    }

    public String getDestinationAccountDetails()
    {
        return getDestinationAccountName()+" | "+getDestinationAccountEmail();
    }

    public int getShareCount() {
        return (shareCount == -1) ? 0 : shareCount;
    }

    public void setShareCount(int shareCount) {
        this.shareCount = shareCount;
    }


    static int slideNo;

    int totalItemsInPlaylist;
    int totalItemsFailed;
    int shareCount = -1;

    ShareModel shareModel = null;

    public ShareModel getShareModel(){
        return shareModel;
    }

    public void setShareModel(ShareModel sm){
        this.shareModel = sm;
    }

    @Parcel
    public static class ShareModel
    {
        String shareCode;
        ArrayList<SharedDetails> sharedDetailsList = null;

        ShareModel(){

        }

        public void setShareCode(String shareCode){
            this.shareCode = shareCode;
        }

        public ShareModel(String shareCode){
            this.shareCode = shareCode;
        }

        ShareModel(String shareCode, List<LocalStorage.ShareInfo> shareInfos){
            this.shareCode = shareCode;
            ArrayList<SharedDetails> sharedDetails = new ArrayList<>();
            shareInfos = (shareInfos == null) ? new ArrayList<>() : shareInfos;
            for(LocalStorage.ShareInfo sh : shareInfos){
                sharedDetails.add(new SharedDetails(sh.getUserName(), sh.getEmailID(), sh.getSharedDestinationCode(), sh.getSharedTime(), sh.getProfilePictureURL(), sh.getOwnerEmailID()));
            }
            this.sharedDetailsList = sharedDetails;
        }

        public List<LocalStorage.ShareInfo> getShareInfos(String shareCode)
        {
            if(sharedDetailsList == null){
                return new ArrayList<>();
            }
            List<LocalStorage.ShareInfo> shareInfos = new ArrayList<>();
            for(SharedDetails sd : sharedDetailsList){
                shareInfos.add(new LocalStorage.ShareInfo(shareCode, sd.userName, sd.emailID, sd.destinationCode, sd.sharedTime, sd.profilePicURL, sd.ownerEmailID));
            }
            return shareInfos;
        }
    }

    @Parcel
    static class SharedDetails
    {
        String userName;
        String emailID;
        int destinationCode;
        long sharedTime;
        String convertedDate;
        Utilities.MusicService musicService;
        String profilePicURL;
        String ownerEmailID;

        SharedDetails()
        {

        }

        SharedDetails(String userName, String emailID, int destinationCode, long sharedTime, String profilePicURL, String ownerEmailID){
            this.sharedTime = sharedTime;
            this.destinationCode = destinationCode;
            this.userName = userName;
            this.emailID = emailID;
            this.convertedDate = Utilities.convertTimeStampToDate(sharedTime);
            this.musicService = Utilities.MusicService.getMusicServiceFromCode(destinationCode);
            this.profilePicURL = profilePicURL;
            this.ownerEmailID = ownerEmailID;
        }

        public String getUserName()
        {
            return userName;
        }

        public String getEmailID()
        {
            return emailID;
        }

        public String getSharedDate()
        {
            return convertedDate;
        }

        public Utilities.MusicService getDestinationService()
        {
            return musicService;
        }

        public String getProfilePicURL()
        {
            return profilePicURL;
        }

        public String getOwnerEmailID() { return ownerEmailID; }

    }




    public ArrayList<String> getAlbumCoverForThumbnail(List<LocalStorage.Album> albums)
    {
        ArrayList<String> list = new ArrayList<>();
        for(LocalStorage.Album album : albums){
            if(album.getAlbumCoverURL() != null){
                list.add(album.getAlbumCoverURL());
            }
            if(list.size() == 4){
                return list;
            }
        }
        int size = 4 - list.size();
        for(int i=0;i<size;i++){
            list.add("some string to trigger default thumbnail img");
        }
        return list;
    }

    public int getAlbumSize(List<LocalStorage.Album> albums)
    {
        return Album.getAlbumSize(albumFilterType, albums);
    }

    public InnerCardObj()
    {

    }

   static class Slider {
        String title;
        String value;
        public static final int TOTAL_SLIDES_SUPPORTED = 3;
        public static final int SLIDER_NO_1 = 1;
        public static final int SLIDER_NO_2 = 2;
        public static final int SLIDER_NO_3 = 3;


        Slider(String title, String value){
            this.title = title;
            this.value = value;
        }

        public static void readyNextSlide(InnerCardObj innerCard)
        {
            if(innerCard.currentSlide == TOTAL_SLIDES_SUPPORTED){
                innerCard.currentSlide = SLIDER_NO_1;
                return;
            }
            innerCard.currentSlide++;
        }

        public static Slider getSliderDetails(InnerCardObj innerCard)
        {
            switch (innerCard.currentSlide)
            {
                case SLIDER_NO_2: {
                    return new Slider("Failed items", innerCard.totalItemsFailed+"");
                }
                case SLIDER_NO_3: {
                    return new Slider("Total Shares", (innerCard.shareCount == -1) ? "-" : innerCard.shareCount+"");
                }
                default:{
                    return new Slider("Total Items", innerCard.totalItemsInPlaylist+"");
                }
            }
        }
    }

    public String getSourceAccountProfilePicture() {
        return sourceAccountProfilePicture;
    }

    public void setSourceAccountProfilePicture(String sourceAccountProfilePicture) {
        this.sourceAccountProfilePicture = sourceAccountProfilePicture;
    }

    public String getDestinationAccountProfilePicture() {
        return destinationAccountProfilePicture;
    }

    public void setDestinationAccountProfilePicture(String destinationAccountProfilePicture) {
        this.destinationAccountProfilePicture = destinationAccountProfilePicture;
    }

    public static InnerCardObj getInnerCard(LocalStorage.Card card){
        InnerCardObj ic = new InnerCardObj();
        ic.setCardNumber(card.getCardNumber());
        ic.setSource(Utilities.MusicService.getMusicServiceFromCode(card.getSourceServiceCode()));
        ic.setDestination(Utilities.MusicService.getMusicServiceFromCode(card.getDestinationServiceCode()));
        ic.setPlayListTitle(card.getPlaylistTitle());
        ic.setSourceAccountName(card.getSourceAccountName());
        ic.setDestinationAccountName(card.getDestinationAccountName());
        ic.setSourceAccountEmail(card.getSourceAccountEmail());
        ic.setDestinationAccountEmail(card.getDestinationAccountEmail());
        ic.setPlayListImageURLs(card.getPlaylistImageUrls());

        ic.setTotalItemsInPlaylist(card.getTotalItemsInPlaylist());
        ic.setTotalItemsFailed(card.getFailedItemsInPlaylist());
        //V1.2
        ic.setSourcePlaylistURL(card.getSourcePlaylistURL());
        ic.setDestinationPlaylistURL(card.getDestinationPlaylistURL());
        ic.originalSource = Utilities.MusicService.getMusicServiceFromCode(card.muziShareSourceServiceCode);
        ic.setSourceAccountProfilePicture(card.getSourceAccountProfilePictureURL());
        ic.setDestinationAccountProfilePicture(card.getDestinationAccountProfilePictureURL());

        return ic;
    }

    public InnerCardObj updateInnerCard(LocalStorage.Card card, InnerCardObj ic, List<LocalStorage.ShareInfo> shareInfo){
        if(card.getShareCode() != null && card.getShareCode().length() > 0){
            if(shareInfo != null ){
                ic.setShareCount(shareInfo.size());
                ic.setShareModel(new ShareModel(card.getShareCode(), shareInfo));
            }else{
                ic.setShareCount(0);
            }
        }
        return ic;
    }
}
