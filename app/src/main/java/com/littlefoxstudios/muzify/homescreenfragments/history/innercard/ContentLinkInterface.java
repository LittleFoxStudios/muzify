package com.littlefoxstudios.muzify.homescreenfragments.history.innercard;

import android.content.Context;

public interface ContentLinkInterface {
    public void openPlaylistInApp(String playlistID);
    public void openPlaylistItemInApp(String playlistItemID);
    public boolean isAppInstalled(Context context);
}
