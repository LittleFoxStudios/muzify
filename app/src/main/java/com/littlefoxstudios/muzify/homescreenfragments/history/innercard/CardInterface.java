package com.littlefoxstudios.muzify.homescreenfragments.history.innercard;

import com.littlefoxstudios.muzify.Utilities;

public interface CardInterface {
    void handleExternalLinkClick(int position);
    void handleMusicServiceToggleClick(int position, boolean isSource); //isSource - true > source | false > destination
    int getServiceThemeColor(boolean isSource);
    int getSecondaryColor();
}
