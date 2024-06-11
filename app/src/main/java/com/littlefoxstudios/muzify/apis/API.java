package com.littlefoxstudios.muzify.apis;

import com.littlefoxstudios.muzify.Utilities;
import com.littlefoxstudios.muzify.homescreenfragments.transfer.datalayout.PlaylistFragment;
import com.littlefoxstudios.muzify.homescreenfragments.transfer.datalayout.PlaylistItemsFragment;
import com.littlefoxstudios.muzify.homescreenfragments.transfer.datalayout.ReceivePlaylistFragment;


import java.util.HashMap;

public interface API {
    void uploadPlaylist(ReceivePlaylistFragment callingFragment);
    void getAllPlaylistItems(Utilities.Alert alert, PlaylistItemsFragment fragment, String playlistID, HashMap<String,String> params, String extraString) throws Exception;
    HashMap<String,String> getAllPlaylistItemsInitialParams();
    void getAllPlaylists(Utilities.Alert alert, PlaylistFragment fragment, HashMap<String,String> params) throws Exception;
    HashMap<String,String> getAllPlaylistInitialParams();
}
