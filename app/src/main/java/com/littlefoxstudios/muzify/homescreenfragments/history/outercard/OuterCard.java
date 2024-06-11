package com.littlefoxstudios.muzify.homescreenfragments.history.outercard;

import android.util.Log;


import com.littlefoxstudios.muzify.R;
import com.littlefoxstudios.muzify.Utilities;
import com.littlefoxstudios.muzify.datastorage.LocalStorage;

import java.util.ArrayList;
import java.util.List;

public class OuterCard {
    private static final int MAX_THUMBNAIL_IMAGES = 4;
    private long cardNumber;
    private String playlistTitle;
    private boolean isPlaylistTransferSuccessful = false;
    private ArrayList<String> playlistImageUrls;
    private int failedItemsInPlaylist=0;
    private int totalItemsInPlaylist;
    private String createdDate;
    private boolean isCardSelected = false;

    private boolean uploadFlag = false;
    private boolean downloadFlag = false;
    private boolean shareFlag = false;


    public int getDestinationServiceCode() {
        return destinationServiceCode;
    }

    public int getSourceServiceCode() {
        return sourceServiceCode;
    }


    private int destinationServiceCode;
    private int sourceServiceCode;


    Integer imageOne;
    Integer imageTwo;
    Integer imageThree;
    Integer imageFour;

    public OuterCard()
    {

    }

    public void selectCard()
    {
        isCardSelected = true;
    }

    public void removeSelection()
    {
        isCardSelected = false;
    }

    public boolean isCardSelected()
    {
        return isCardSelected;
    }

    public int getTotalItemsInPlaylist(){
        return totalItemsInPlaylist;
    }


    private void initializeDefaultThumbnailImages()
    {
        if(playlistImageUrls == null || playlistImageUrls.size() == 0){
            initializeDefaultThumbnailImages(new ArrayList<String>());
        }else{
            initializeDefaultThumbnailImages(playlistImageUrls);
        }
    }

    private void initializeDefaultThumbnailImages(ArrayList<String> drawableUrls)
    {
        int count = MAX_THUMBNAIL_IMAGES - drawableUrls.size();

        if(drawableUrls.size() > MAX_THUMBNAIL_IMAGES){
            Log.e("HISTORY", "Error while initializing default thumbnail images - Provided drawable urls are more than max thumbnail count! Using Default images");
            count = MAX_THUMBNAIL_IMAGES;
        }

        if(count == 0){
            return;
        }

        imageFour = getRandomDrawableIDForThumbnail();

        if(count > 1){
            imageThree = getRandomDrawableIDForThumbnail();
        }
        if(count > 2){
            imageTwo = getRandomDrawableIDForThumbnail();
        }
        if(count > 3){
            imageOne = getRandomDrawableIDForThumbnail();
        }

    }

    public static int getRandomDrawableIDForThumbnail()
    {
        return Utilities.getRandomDrawableIDForThumbnail();
    }

    public ArrayList<String> getPlaylistImageUrls()
    {
        return playlistImageUrls;
    }

    public String getPlaylistTitle()
    {
        return playlistTitle;
    }

    public String getCreatedDate()
    {
        return createdDate;
    }

    public int getFailedItemsCount()
    {
        return failedItemsInPlaylist;
    }

    public int getTransferMediumImage()
    {
        return Utilities.MusicService.getMusicServiceFromCode(destinationServiceCode).getLogoDrawableID();
    }

    public boolean isPlaylistTransferSuccessful()
    {
        return isPlaylistTransferSuccessful;
    }

    public int getResponseImage()
    {
        return (failedItemsInPlaylist == totalItemsInPlaylist) ? R.drawable.error_icon : (failedItemsInPlaylist == 0) ? R.drawable.check_icon : 0;
    }

    public Integer getFirstImageID()
    {
        return imageOne;
    }

    public Integer getSecondImageID()
    {
        return imageTwo;
    }

    public Integer getThirdImageID()
    {
        return imageThree;
    }

    public Integer getFourthImageID()
    {
        return imageFour;
    }

    public long getCardNumber() { return cardNumber; }


    public OuterCard getOuterCard(LocalStorage.Card card){
        this.cardNumber = card.getCardNumber();
        this.playlistTitle = card.getPlaylistTitle();
        this.isPlaylistTransferSuccessful = (card.failedItemsInPlaylist == 0);
        this.playlistImageUrls = card.getPlaylistImageUrls();
        initializeDefaultThumbnailImages();
        this.failedItemsInPlaylist = card.getFailedItemsInPlaylist();
        this.createdDate = card.getCreatedTime();
        this.sourceServiceCode = card.getSourceServiceCode();
        this.destinationServiceCode = card.getDestinationServiceCode();
        this.totalItemsInPlaylist = card.getTotalItemsInPlaylist();
        this.uploadFlag = card.uploadFlag;
        this.downloadFlag = card.downloadFlag;
        this.shareFlag = card.shareFlag;
        return this;
    }

    public ArrayList<OuterCard> getOuterCards(List<LocalStorage.Card> allCards){
        ArrayList<OuterCard> outerCards = new ArrayList<>();
        for(LocalStorage.Card card : allCards){
            outerCards.add(new OuterCard().getOuterCard(card));
        }
        return outerCards;
    }

    public boolean isUploadFlagEnabled(){
        return this.uploadFlag;
    }

    public boolean isDownloadFlagEnabled() {
        return this.downloadFlag;
    }

    public boolean isShareFlagEnabled(){
        return this.shareFlag;
    }

}
