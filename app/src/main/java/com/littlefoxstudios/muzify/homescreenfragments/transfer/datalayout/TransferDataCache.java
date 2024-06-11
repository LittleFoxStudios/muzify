package com.littlefoxstudios.muzify.homescreenfragments.transfer.datalayout;

import java.util.ArrayList;

import javax.annotation.Nullable;

public class TransferDataCache {
    private String accountEmailID;
    private ArrayList<Playlist> playlists;
    private int serviceCode;
    private long refreshTime;


    public String getAccountEmailID()
    {
        return accountEmailID;
    }

    public ArrayList<Playlist> getPlaylists()
    {
        return playlists;
    }

    public int getServiceCode()
    {
        return serviceCode;
    }

    public long getRefreshTime()
    {
        return refreshTime;
    }


    public static ArrayList<Playlist> getPlaylists(int serviceCode, String accountEmailID, ArrayList<TransferDataCache> caches, boolean allowRefresh)
    {
        TransferDataCache obj = null;
        for(TransferDataCache cache : caches){
            if(cache.getServiceCode() == serviceCode && accountEmailID.equals(cache.getAccountEmailID())){
                obj = cache;
                break;
            }
        }
        if(obj == null || (System.currentTimeMillis() >= obj.getRefreshTime() && allowRefresh)){
            return null;
        }
        return obj.getPlaylists();
    }

    @Nullable
    public static ArrayList<PlaylistItem> getPlaylistItems(int serviceCode, String accountEmailID, ArrayList<TransferDataCache> caches, String playlistID)
    {
        ArrayList<Playlist> playlists = getPlaylists(serviceCode, accountEmailID, caches, false);
        if(playlists == null){
            return null;
        }
        for(Playlist playlist : playlists){
            if(playlist.getPlaylistID().equals(playlistID)){
                return playlist.getPlaylistItems();
            }
        }
        return null;
    }

    public static Playlist getSelectedPlaylist(String playlistID, String sourceAccEmail, int serviceCode, ArrayList<TransferDataCache> caches){
        ArrayList<Playlist> playlists = getPlaylists(serviceCode, sourceAccEmail, caches, false);
        for(Playlist playlist : playlists){
            if(playlist.getPlaylistID().equals(playlistID)){
                return playlist;
            }
        }
        return null;
    }


    public static ArrayList<TransferDataCache> updateTransferDataCache(int serviceCode, String accountEmailID, ArrayList<Playlist> playlists, ArrayList<TransferDataCache> transferDataCaches){
        TransferDataCache obj = null;
        int index = 0;
        for(TransferDataCache cache : transferDataCaches){
            if(cache.getServiceCode() == serviceCode && accountEmailID.equals(cache.getAccountEmailID())){
                obj = cache;
                break;
            }
            index++;
        }
        TransferDataCache cache = new TransferDataCache();
        cache.serviceCode = serviceCode;
        cache.accountEmailID = accountEmailID;
        cache.playlists = playlists;
        cache.refreshTime = System.currentTimeMillis() + 300000; // 5 minutes
        if(obj == null){
            transferDataCaches  = new ArrayList<>();
            transferDataCaches.add(cache);
            return transferDataCaches;
        }
        transferDataCaches.remove(index);
        transferDataCaches.add(cache);
        return transferDataCaches;
    }

    public static ArrayList<TransferDataCache> updateTransferDataCache(int serviceCode, String accountEmailID, ArrayList<PlaylistItem> playlistItems, ArrayList<TransferDataCache> caches, String playlistID){
        TransferDataCache obj = null;
        int index = 0;
        for(TransferDataCache cache : caches){
            if(cache.getServiceCode() == serviceCode && accountEmailID.equals(cache.getAccountEmailID())){
                obj = cache;
                break;
            }
            index++;
        }
        TransferDataCache cache = new TransferDataCache();
        cache.serviceCode = serviceCode;
        cache.accountEmailID = accountEmailID;
        cache.refreshTime = System.currentTimeMillis() + 300000; // 5 minutes
        ArrayList<Playlist> playlists = (obj == null || obj.getPlaylists() == null) ? new ArrayList<>() : obj.getPlaylists();
        for(Playlist p : playlists){
            if(p.getPlaylistID().equals(playlistID)){
                p.updatePlaylistItems(playlistItems);
                break;
            }
        }
        cache.playlists = playlists;
        caches.remove(index);
        caches.add(cache);
        return caches;
    }


}
