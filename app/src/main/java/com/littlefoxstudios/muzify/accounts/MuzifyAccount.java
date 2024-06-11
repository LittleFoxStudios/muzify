package com.littlefoxstudios.muzify.accounts;


import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.littlefoxstudios.muzify.HomeScreenActivity;
import com.littlefoxstudios.muzify.Utilities;
import com.littlefoxstudios.muzify.WelcomeActivity;
import com.littlefoxstudios.muzify.datastorage.LocalStorage;
import com.littlefoxstudios.muzify.datastorage.MuzifyViewModel;

import java.util.HashMap;
import java.util.List;

import okhttp3.internal.Util;

public class MuzifyAccount  {
    private  String emailID;
    private  String userName;
    private  String profilePic;

    //for shared user data
    public long st;
    public int dsc;
    public long cn;
    public String oe;
    public String shareCode;

    public static HashMap<String, String> getFreshAccount(MuzifyAccount account) {
        HashMap<String,String> hash = new HashMap<>();
        hash.put("emailID", account.getEmailID());
        hash.put("profilePictureURL", account.getProfilePictureURL());
        hash.put("userName", account.getUserName());
        long time = System.currentTimeMillis();
        hash.put("accountCreatedTime", time+"");
        hash.put("accountLastUpdatedTime", time+"");
        return hash;
    }

    public void setEmailID(String emailID){
        this.emailID = emailID;
    }

    public void setUserName(String userName){
        this.userName = userName;
    }

    public String getEmailID(){
        return emailID;
    }

    public String getUserName(){
        return userName;
    }

    public void setProfilePictureURL(String profilePic){
        this.profilePic = profilePic;
    }

    public String getProfilePictureURL() {
        return profilePic;
    }

    public MuzifyAccount(String userName, String email, String profilePic, int destinationServiceCode, long cardNumber, String ownerEmailID, String shareCode){
        this.st = System.currentTimeMillis();
        this.emailID = email;
        this.userName = userName;
        this.profilePic = profilePic;
        this.dsc = destinationServiceCode;
        this.cn = cardNumber;
        this.oe = ownerEmailID;
        this.shareCode = shareCode;
    }


    public MuzifyAccount(){

    }

    public static MuzifyAccount get(HashMap data){
        String userName = (String) data.get("userName");
        String emailID = (String) data.get("emailID");
        long st = (Long) data.get("st");
        int dsc = Integer.parseInt(String.valueOf((Long) data.get("dsc")));
        String profilePic = (String) data.get("profilePictureURL");
        String oe = (String) data.get("oe");
        long cn = (Long) data.get("cn");
        String shareCode = (String) data.get("shareCode");
        MuzifyAccount acc = new MuzifyAccount(userName, emailID, profilePic, dsc, cn, oe, shareCode);
        acc.st = st;
        return acc;
    }

}
