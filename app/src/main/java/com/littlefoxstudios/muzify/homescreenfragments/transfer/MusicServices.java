package com.littlefoxstudios.muzify.homescreenfragments.transfer;

import android.app.Activity;
import android.content.Context;

import androidx.fragment.app.Fragment;

import com.android.volley.RequestQueue;
import com.littlefoxstudios.muzify.Utilities;
import com.littlefoxstudios.muzify.accounts.Spotify;
import com.littlefoxstudios.muzify.accounts.YoutubeMusic;
import com.littlefoxstudios.muzify.apis.API;
import com.littlefoxstudios.muzify.apis.SpotifyAPI;
import com.littlefoxstudios.muzify.apis.YoutubeMusicAPI;
import com.littlefoxstudios.muzify.homescreenfragments.history.innercard.ContentLinkInterface;
import com.littlefoxstudios.muzify.homescreenfragments.transfer.datalayout.TransferInfo;

import java.util.ArrayList;
import java.util.HashMap;

public class MusicServices {
    private String serviceName;
    private int serviceCode;
    private int serviceImageCode;


    private MusicServices(String serviceName, int serviceCode, int serviceImageCode) {
        this.serviceName = serviceName;
        this.serviceCode = serviceCode;
        this.serviceImageCode = serviceImageCode;
    }


    public static Fragment getAppropriateFragment(TransferInfo transferInfo, int serviceCode, int transferCode) throws Exception {
        if (serviceCode == Utilities.MusicService.MUZI_SHARE.getCode()) {
            return new MuziShareTransferAuthentication(transferInfo);
        } else if (serviceCode == Utilities.MusicService.YOUTUBE_MUSIC.getCode()) {
            return new YoutubeMusicTransferAuthentication(transferInfo, transferCode);
        } else if (serviceCode == Utilities.MusicService.SPOTIFY.getCode()) {
            return new SpotifyTransferAuthentication(transferInfo, transferCode);
        }
        throw new Exception("Invalid Service Code provided");
    }

    public static ContentLinkInterface getAppropriateServiceForContentLinking(Context context, int serviceCode) {
        if (serviceCode == Utilities.MusicService.MUZI_SHARE.getCode()) {
            return null;
        } else if (serviceCode == Utilities.MusicService.YOUTUBE_MUSIC.getCode()) {
            return new YoutubeMusic(context);
        } else if (serviceCode == Utilities.MusicService.SPOTIFY.getCode()) {
            return new Spotify(context);
        }
        return null;
    }


    public static class MusicServiceRequest{
        public String requestDate;
        public String developerMessage;
        public String requestedAccountEmail;

        MusicServiceRequest(String requestDate, String developerMessage){
            this.requestDate = requestDate == null ? "" : requestDate;
            this.developerMessage = developerMessage == null ? "" : developerMessage;
        }

        public static MusicServiceRequest get(HashMap<String,Object> dataHash){
            String requestDate = (String) dataHash.get("requestDate");
            String developerMessage = (String) dataHash.get("developerMessage");
            return new MusicServiceRequest(requestDate, developerMessage);
        }
    }

   public static API apiFactory(int destinationServiceCode, RequestQueue requestQueue, Activity activity, TransferInfo transferInfo)
   {
       switch (destinationServiceCode)
       {
           case Utilities.ServiceCode.YOUTUBE_MUSIC: return new YoutubeMusicAPI(requestQueue, activity, transferInfo);
           case Utilities.ServiceCode.SPOTIFY: return new SpotifyAPI(requestQueue, activity, transferInfo);
           default:
               return null;
       }
   }



    public static ArrayList<MusicServices> getMusicServiceExcluding(int excludingServiceCode){
       return getAllMusicService(excludingServiceCode);
    }

    public static ArrayList<MusicServices> getAllMusicService(){
        return getAllMusicService(null);
    }

    private static ArrayList<MusicServices> getAllMusicService(Integer excludingServiceCode)
    {
        ArrayList<MusicServices> list = new ArrayList<>();
        ArrayList<Utilities.MusicService> services = Utilities.MusicService.getAllService();
        for(Utilities.MusicService service : services){
            if(excludingServiceCode != null && service.getCode() == excludingServiceCode){
                continue;
            }
           list.add(new MusicServices(service.getFormattedServiceName(), service.getCode(), service.getLogoDrawableID()));
        }
        return list;
    }

    public String getServiceName(){
        return this.serviceName;
    }

    public int getServiceCode(){
        return this.serviceCode;
    }

    public int getServiceImageCode(){
        return this.serviceImageCode;
    }
}
