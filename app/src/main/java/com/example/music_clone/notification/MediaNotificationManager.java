package com.example.music_clone.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.session.PlaybackState;
import android.os.Build;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.media.session.MediaButtonReceiver;

import com.example.music_clone.R;
import com.example.music_clone.services.MediaService;
import com.example.music_clone.ui.MainActivity;

public class MediaNotificationManager {
    private static final String TAG = "MediaNotificationManage";

    private final MediaService mMediaService;
    private final NotificationManager mNotificationManager;
    private static final String CHANNEL_ID = "com.swapnil.music-clone.musicPlayer.channel01";

    private final NotificationCompat.Action mPlayAction;
    private final NotificationCompat.Action mPauseAction;
    private final NotificationCompat.Action mNextAction;
    private final NotificationCompat.Action mPrevAction;

    public MediaNotificationManager(MediaService mediaService) {
        this.mMediaService = mediaService;
        mNotificationManager = (NotificationManager)mMediaService.getSystemService(Context.NOTIFICATION_SERVICE);

        mPlayAction = new NotificationCompat.Action(R.drawable.ic_play_arrow_white_24dp,
                mMediaService.getString(R.string.label_play),
                MediaButtonReceiver.buildMediaButtonPendingIntent(mMediaService,
                        PlaybackStateCompat.ACTION_PLAY
                )
        );
        mPauseAction = new NotificationCompat.Action(R.drawable.ic_pause_circle_outline_white_24dp,
                mMediaService.getString(R.string.label_pause),
                MediaButtonReceiver.buildMediaButtonPendingIntent(mMediaService,
                        PlaybackStateCompat.ACTION_PAUSE
                )
        );
        mNextAction = new NotificationCompat.Action(R.drawable.ic_skip_next_white_24dp,
                mMediaService.getString(R.string.label_next),
                MediaButtonReceiver.buildMediaButtonPendingIntent(mMediaService,
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                )
        );
        mPrevAction = new NotificationCompat.Action(R.drawable.ic_skip_previous_white_24dp,
                mMediaService.getString(R.string.label_previous),
                MediaButtonReceiver.buildMediaButtonPendingIntent(mMediaService,
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                )
        );
        mNotificationManager.cancelAll();
    }

    public NotificationManager getNotificationManager() {
        return mNotificationManager;
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private void createChannel(){
        if (mNotificationManager.getNotificationChannel(CHANNEL_ID) == null) {
            CharSequence name = "MediaSession";
            String description = "MediaSession for media player";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID,name,importance);
            mChannel.setDescription(description);
            mChannel.enableLights(true);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100,200,300,400,500,400,300,200,100});
            mNotificationManager.createNotificationChannel(mChannel);
            Log.d(TAG, "createChannel: new Notification channel created");
        }
        else {
            Log.d(TAG, "createChannel: notification channel reused");
        }
    }

    private boolean isAndroidOOrHigher(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    private Notification buildNotification(@NonNull PlaybackStateCompat state,
                                           MediaSessionCompat.Token token,
                                           final MediaDescriptionCompat description,
                                           Bitmap bitmap)
    {
        boolean isPlaying = state.getState() == PlaybackStateCompat.STATE_PLAYING;
        if (isAndroidOOrHigher()) {
            createChannel();
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mMediaService,CHANNEL_ID);
        builder.setStyle(
                new androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(token)
                .setShowActionsInCompactView(0,1,2)
               ).setColor(ContextCompat.getColor(mMediaService, R.color.notification_bg))
                .setSmallIcon(R.drawable.ic_audiotrack_white_24dp)
                .setContentIntent(createContentIntent())
                .setContentTitle(description.getTitle())
                .setContentText(description.getSubtitle())
                .setLargeIcon(bitmap)
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(
                        mMediaService, PlaybackStateCompat.ACTION_STOP)
                )
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        if ((state.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS) != 0) {
            builder.addAction(mPrevAction);
        }
        builder.addAction(isPlaying ? mPauseAction : mPlayAction);
        if ((state.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_NEXT) != 0) {
            builder.addAction(mNextAction);
        }

        return builder.build();
    }

    private PendingIntent createContentIntent(){
        Intent openUi = new Intent(mMediaService, MainActivity.class);
        openUi.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(mMediaService,501,openUi,PendingIntent.FLAG_CANCEL_CURRENT);
    }
}
