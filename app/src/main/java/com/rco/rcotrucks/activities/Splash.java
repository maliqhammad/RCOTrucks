package com.rco.rcotrucks.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.utils.BaseActivity;

public class Splash extends BaseActivity {
    private static final String TAG = Splash.class.getSimpleName();
    Thread splashTread;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        extractToFullScreen();
        Log.d(TAG, "requestPermissions: onCreate: ");
        setContentView(R.layout.activity_splash);

        runThreadForFewSeconds();
    }

    void runThreadForFewSeconds() {

        Log.d(TAG, "requestPermissions: runThreadForFewSeconds: ");
        splashTread = new Thread() {
            @Override
            public void run() {
                try {
                    int waited = 0;
                    // Splash screen pause time
                    while (waited < 3000) {
                        sleep(100);
                        waited += 100;
                    }
                    Log.d(TAG, "run: ");

                } catch (InterruptedException e) {
                    // do nothing

                    Log.d(TAG, "run: When some exception comes then start Sign In Screen ");
                    Log.d(TAG, "requestPermissions: run: Exception is " + e.getMessage());
                    e.printStackTrace();

                } finally {
                    Log.d(TAG, "requestPermissions: run: finally block");
                    Intent intent = new Intent(Splash.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        };
        splashTread.start();
    }

}