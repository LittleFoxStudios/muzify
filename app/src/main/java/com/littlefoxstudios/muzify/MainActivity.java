package com.littlefoxstudios.muzify;

import static com.littlefoxstudios.muzify.Constants.APP_NAME;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        //animate logo
        animateLogo();
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //switch activity after animating logo
                /*
                startActivity(new Intent(MainActivity.this, HomeScreenActivity.class));
                 */
                startActivity(new Intent(MainActivity.this, WelcomeActivity.class));
                finish();
            }
        }, 1500);
    }


    //animation logic
    private void animateLogo()
    {
        TextView logoText = findViewById(R.id.logo_text_id);
        logoText.setText(APP_NAME);
        //TODO COMPLETE ANIMATION
    }


}
