package com.littlefoxstudios.muzify.datastorage;

import android.content.Context;
import android.os.AsyncTask;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.littlefoxstudios.muzify.Utilities;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

@Database(entities = {LocalStorage.UserData.class, LocalStorage.Card.class, LocalStorage.Album.class,
    LocalStorage.ShareInfo.class}, version = 1)
public abstract class MuzifyRoomDatabase extends RoomDatabase {
    private static MuzifyRoomDatabase instance;

    public abstract LocalStorage.UserDataDAO userDataDAO();
    public abstract LocalStorage.CardDAO cardDAO();
    public abstract LocalStorage.AlbumDAO albumDAO();
    public abstract LocalStorage.ShareInfoDAO shareInfoDAO();
    public abstract LocalStorage.CardWithAlbumsDAO CardWithAlbumsDAO();

    public static synchronized MuzifyRoomDatabase getInstance(Context context)
    {
        if(instance == null){
            instance = Room.databaseBuilder(context.getApplicationContext(), MuzifyRoomDatabase.class, "muzify_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }



}
