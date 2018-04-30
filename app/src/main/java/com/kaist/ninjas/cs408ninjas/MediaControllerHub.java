package com.kaist.ninjas.cs408ninjas;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.view.KeyEvent;
import android.widget.Toast;

public class MediaControllerHub {
    private Intent downIntent;
    private Intent upIntent;
    private KeyEvent mKeyEvent;
    private Context context;
    public MediaControllerHub(Context context) {
        this.context = context;
    }
    public void pause() {
        actionPassing(KeyEvent.KEYCODE_MEDIA_PAUSE);
    }

    public void play() {
        actionPassing(KeyEvent.KEYCODE_MEDIA_PLAY);
    }

    public void next() {
        actionPassing(KeyEvent.KEYCODE_MEDIA_NEXT);
    }

    public void previous() {
        actionPassing(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
    }

    public void fastForward() {
        actionPassing(KeyEvent.KEYCODE_MEDIA_FAST_FORWARD);
    }

    public void rewind() {
        actionPassing(KeyEvent.KEYCODE_MEDIA_REWIND);
    }

    public void volumeUp() {
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
    }

    public void volumeDown() {
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
    }

    private void actionPassing(int keyCode) {
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (mAudioManager != null) {
            KeyEvent mKey = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
            mAudioManager.dispatchMediaKeyEvent(mKey);
            mKey = new KeyEvent(KeyEvent.ACTION_UP, keyCode);
            mAudioManager.dispatchMediaKeyEvent(mKey);
        }
    }
}
