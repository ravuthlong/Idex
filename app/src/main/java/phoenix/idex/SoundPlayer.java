package phoenix.idex;

import android.content.Context;
import android.media.MediaPlayer;

/**
 * Created by Ravinder on 4/28/16.
 */
public class SoundPlayer {
    private MediaPlayer mp;
    private Context context;
    private int soundID;

    public SoundPlayer(Context context, int soundID) {
        this.context = context;
        this.soundID = soundID;
    }

    public void playSound() {
        mp = MediaPlayer.create(context, soundID);
        mp.start();
        onCompleteListener();
    }

    public void onCompleteListener() {
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.reset();
                mp.release();
            }
        });
    }
}
