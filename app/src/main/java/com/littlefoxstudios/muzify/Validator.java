package com.littlefoxstudios.muzify;

import static com.littlefoxstudios.muzify.MuzifySharedMemory.IS_UPDATE_NEEDED;
import static com.littlefoxstudios.muzify.MuzifySharedMemory.IS_UPLOAD_NEEDED;
import static com.littlefoxstudios.muzify.MuzifySharedMemory.NEXT_DOWNLOAD_TIME;
import static com.littlefoxstudios.muzify.MuzifySharedMemory.SHARED_PREFERENCE;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.littlefoxstudios.muzify.datastorage.CloudStorage;
import com.littlefoxstudios.muzify.datastorage.MuzifyViewModel;

import java.util.logging.Logger;

public class Validator {


    public static void validateUser(MuzifyViewModel muzifyViewModel, String userEmailID)
    {
        //TODO Implement user validation in next update
    }

    public static void validateAppVersion(Activity activity, MuzifyViewModel muzifyViewModel) {
      try
      {
          MuzifySharedMemory muzifySharedMemory = new MuzifySharedMemory(activity);
          long nextDownloadTime = muzifySharedMemory.getNextDownloadTime();
          boolean isUpdateNeeded = muzifySharedMemory.getUpdateNeeded();

          if(System.currentTimeMillis() >= nextDownloadTime)
          {
              CloudStorage.checkAppVersion(activity, muzifyViewModel);
          }else if(isUpdateNeeded){
              handleAppUpdate(activity, true, muzifyViewModel);
          }
      }
      catch (Exception e)
      {
          Utilities.Loggers.showLongToast(activity.getApplicationContext(), "Internal Error : "+e.getMessage());
          activity.finishAndRemoveTask();
      }
    }

    public static void handleAppUpdate(Activity activity, boolean isUpdateNeeded, MuzifyViewModel muzifyViewModel) throws Exception
    {
       MuzifySharedMemory muzifySharedMemory = new MuzifySharedMemory(activity);
       if(isUpdateNeeded){
           try {
               muzifySharedMemory.setUpdateNeeded(true);
               Utilities.Alert alert = new Utilities.Alert(activity, Utilities.Alert.UPDATE_AVAILABLE);
               alert.startUpdateDialog(false);
           } catch (Exception e) {
               Utilities.Loggers.showLongToast(activity.getApplicationContext(), "Error occurred while checking app version : "+e.getMessage());
               activity.finishAndRemoveTask();
           }
       }else{
           muzifySharedMemory.setUpdateNeeded(false);
           muzifySharedMemory.setNextDownloadTime(System.currentTimeMillis()+MuzifyConfigs.PERIODIC_UPDATE_CHECKING_TIME);
       }
    }

}
