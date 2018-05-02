package com.kaist.ninjas.cs408ninjas;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity implements View.OnClickListener {

    private MediaControllerHub mediaControllerHub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mediaControllerHub = new MediaControllerHub(this);
        Button playButton = findViewById(R.id.play_button);
        Button pauseButton = findViewById(R.id.pause_button);
        Button nextButton = findViewById(R.id.next_button);
        Button prevButton = findViewById(R.id.prev_button);
        Button rewindButton = findViewById(R.id.rewind_button);
        Button volumeUpButton = findViewById(R.id.volume_up_button);
        Button volumeDownButton  = findViewById(R.id.volume_down_button);
        Button notifyButton = findViewById(R.id.notify_button);
        Button cameraPreviewButton = findViewById(R.id.preview_button);
        playButton.setOnClickListener(this);
        pauseButton.setOnClickListener(this);
        nextButton.setOnClickListener(this);
        prevButton.setOnClickListener(this);
        rewindButton.setOnClickListener(this);
        volumeUpButton.setOnClickListener(this);
        volumeDownButton.setOnClickListener(this);
        notifyButton.setOnClickListener(this);
        cameraPreviewButton.setOnClickListener(this);
    }

    private void updateNotification() {
        Intent myIntent = new Intent(getApplicationContext(), MainBackground.class);
        startService(myIntent);
    }

    @Override
    public void onClick(View v){
        switch (v.getId()) {
            case R.id.play_button: {
                mediaControllerHub.play();
                break;
            }

            case R.id.pause_button: {
                mediaControllerHub.pause();
                break;
            }

            case R.id.next_button: {
                mediaControllerHub.next();
                break;
            }

            case R.id.prev_button: {
                mediaControllerHub.previous();
                break;
            }

            case R.id.rewind_button: {
                mediaControllerHub.rewind();
                break;
            }

            case R.id.volume_down_button: {
                mediaControllerHub.volumeDown();
                break;
            }

            case R.id.volume_up_button: {
                mediaControllerHub.volumeUp();
                break;
            }

            case R.id.notify_button: {
                updateNotification();
                break;
            }

            case R.id.preview_button : {
                Intent mIntent = new Intent(this, CameraDetectionPreview.class);
                startActivity(mIntent);
                break;
            }
        }
    }
}
