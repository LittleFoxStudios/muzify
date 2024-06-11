package com.littlefoxstudios.muzify.internet;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.littlefoxstudios.muzify.Utilities;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class InternetTestingViewModel extends AndroidViewModel {
    private final MutableLiveData<Boolean> mConnected = new MutableLiveData<>();

    public static boolean internetConnectionAvailable(int timeOut) {
        InetAddress inetAddress = null;
        try {
            Future<InetAddress> future = Executors.newSingleThreadExecutor().submit(new Callable<InetAddress>() {
                @Override
                public InetAddress call() {
                    try {
                        return InetAddress.getByName("google.com");
                    } catch (UnknownHostException e) {
                        return null;
                    }
                }
            });
            inetAddress = future.get(timeOut, TimeUnit.MILLISECONDS);
            future.cancel(true);
        } catch (InterruptedException | ExecutionException | TimeoutException ignored) {}
        return inetAddress!=null && !inetAddress.equals("");
    }


    public InternetTestingViewModel(Application app) {
        super(app);

        ConnectivityManager manager = (ConnectivityManager)app.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (manager == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            mConnected.setValue(true);
            return;
        }

        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build();

        manager.registerNetworkCallback(networkRequest, new ConnectivityManager.NetworkCallback() {
            Set<Network> availableNetworks = new HashSet<>();

            public void onAvailable(@NonNull Network network) {
                if(internetConnectionAvailable(2000)){
                    availableNetworks.add(network);
                }else{
                    availableNetworks.remove(network);
                }
                mConnected.postValue(!availableNetworks.isEmpty());
            }

            public void onLost(@NonNull Network network) {
                availableNetworks.remove(network);
                mConnected.postValue(!availableNetworks.isEmpty());
            }

            public void onUnavailable() {
                availableNetworks.clear();
                mConnected.postValue(!availableNetworks.isEmpty());
            }
        });
    }

    @NonNull
    public MutableLiveData<Boolean> getConnected() {
        return mConnected;
    }
}