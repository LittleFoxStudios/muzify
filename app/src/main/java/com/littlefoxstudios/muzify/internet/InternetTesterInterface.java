package com.littlefoxstudios.muzify.internet;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.littlefoxstudios.muzify.Utilities;

public interface InternetTesterInterface {
    public InternetTestingViewModel initializeInternetTestingViewModel();

    public default void observeInternetConnection(InternetTestingViewModel vm, LifecycleOwner lifecycleOwner, Utilities.Alert alert){
        vm.getConnected().observe(lifecycleOwner, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isConnected) {
                if(isConnected){
                    alert.internetConnected();
                }else{
                    alert.showNoInternetDialog(vm);
                }
            }
        });
    }
}
