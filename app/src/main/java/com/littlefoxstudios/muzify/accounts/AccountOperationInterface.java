package com.littlefoxstudios.muzify.accounts;

import android.content.Intent;
import android.view.View;

public interface AccountOperationInterface {

    void signIn();

    void signOut();

    void listenStartActivityForResult(int requestCode, int resultCode, Intent data);
}
