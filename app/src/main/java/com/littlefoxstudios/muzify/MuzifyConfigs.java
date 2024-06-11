package com.littlefoxstudios.muzify;

public class MuzifyConfigs {
    //System
    public static final boolean INCLUDE_STARTUP_MUSIC = true;

    public static final int MAXIMUM_SONGS_SUPPORTED_FOR_TRANSFER = 1000;

    public static final boolean SAVE_HISTORY_ENABLED = true;

    public static final long PERIODIC_UPDATE_CHECKING_TIME = 10800000L;

    //MuziShare
    public static final int MUZI_SHARE_MAX_ATTEMPTS = 7;
    public static final long MUZI_SHARE_REFRESH_TIME = 18000000L; //5hours

    public static final int MUZI_SHARE_CODE_LENGTH_MIN = 6;
    public static final int MUZI_SHARE_CODE_LENGTH_MAX = 9;

    //Youtube
    public static final int YOUTUBE_INSERT_SONG_INTERVAL = 1000;

    public static boolean autoUploadHistoryEnabled(MuzifySharedMemory muzifySharedMemory)
    {
        return muzifySharedMemory.autoUploadHistorySelected(muzifySharedMemory.getDefaultAccount());
    }

}
