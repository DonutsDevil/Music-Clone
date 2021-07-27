package com.example.music_clone.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaDescription;
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

import com.example.music_clone.R;
import com.example.music_clone.services.MediaService;

public class MediaNotificationManager {
    private static final String TAG = "MediaNotificationManage";

    private final MediaService mMediaService;
    private final NotificationManager mNotificationManager;
    private static final String CHANNEL_ID = "com.swapnil.music-clone.musicPlayer.channel01";

    public MediaNotificationManager(MediaService mediaService) {
        this.mMediaService = mediaService;
        mNotificationManager = (NotificationManager)mMediaService.getSystemService(Context.NOTIFICATION_SERVICE);
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
                .setContentIntent(null)
                .setContentTitle(description.getTitle())
                .setContentText(description.getSubtitle())
                .setLargeIcon(bitmap)
                .setDeleteIntent(null)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        return builder.build();
    }


}
