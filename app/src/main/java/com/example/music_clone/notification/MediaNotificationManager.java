package com.example.music_clone.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
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

public class MediaNotificationManager extends BroadcastReceiver {
    private static final String TAG = "MediaNotificationManage";

    private final MediaService mMediaService;
    private final NotificationManager mNotificationManager;
    private static final String CHANNEL_ID = "com.swapnil.music-clone.musicPlayer.channel01";


    private static final String ACTION_PAUSE = "com.example.music_clone.notification.pause";
    private static final String ACTION_PLAY = "com.example.music_clone.notification.play";
    private static final String ACTION_NEXT = "com.example.music_clone.notification.next";
    private static final String ACTION_PREV = "com.example.music_clone.notification.prev";

    private final NotificationCompat.Action mPlayAction;
    private final NotificationCompat.Action mPauseAction;
    private final NotificationCompat.Action mNextAction;
    private final NotificationCompat.Action mPrevAction;

    public static final int NOTIFICATION_ID = 201;
    private static final int REQUEST_CODE = 100;

    public MediaNotificationManager(MediaService mediaService) {
        this.mMediaService = mediaService;
        mNotificationManager = (NotificationManager)mMediaService.getSystemService(Context.NOTIFICATION_SERVICE);

        String pkg = mediaService.getPackageName();
        PendingIntent playIntent = PendingIntent.getBroadcast(
                mMediaService,
                REQUEST_CODE,
                new Intent(ACTION_PLAY).setPackage(pkg),
                PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent pauseIntent =
                PendingIntent.getBroadcast(
                        mMediaService,
                        REQUEST_CODE,
                        new Intent(ACTION_PAUSE).setPackage(pkg),
                        PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent nextIntent =
                PendingIntent.getBroadcast(
                        mMediaService,
                        REQUEST_CODE,
                        new Intent(ACTION_NEXT).setPackage(pkg),
                        PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent prevIntent =
                PendingIntent.getBroadcast(
                        mMediaService,
                        REQUEST_CODE,
                        new Intent(ACTION_PREV).setPackage(pkg),
                        PendingIntent.FLAG_CANCEL_CURRENT);


        mPlayAction = new NotificationCompat.Action(
                R.drawable.ic_play_arrow_white_24dp,
                mMediaService.getString(R.string.label_play),
                playIntent);

        mPauseAction = new NotificationCompat.Action(
                R.drawable.ic_pause_circle_outline_white_24dp,
                mMediaService.getString(R.string.label_pause),
                pauseIntent);

        mNextAction = new NotificationCompat.Action(
                R.drawable.ic_skip_next_white_24dp,
                mMediaService.getString(R.string.label_next),
                nextIntent);

        mPrevAction = new NotificationCompat.Action(
                R.drawable.ic_skip_previous_white_24dp,
                mMediaService.getString(R.string.label_previous),
                prevIntent);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_NEXT);
        filter.addAction(ACTION_PAUSE);
        filter.addAction(ACTION_PLAY);
        filter.addAction(ACTION_PREV);

        mMediaService.registerReceiver(this,filter);
        // cancel all previously shown notifications
        mNotificationManager.cancelAll();
    }

    public NotificationManager getNotificationManager() {
        return mNotificationManager;
    }

    // Does nothing on versions of Android earlier than O.
    @RequiresApi(Build.VERSION_CODES.O)
    private void createChannel() {
        if (mNotificationManager.getNotificationChannel(CHANNEL_ID) == null) {
            // The user-visible name of the channel.
            CharSequence name = "MediaSession";
            // The user-visible description of the channel.
            String description = "MediaSession for media player";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID,name,importance);
            // Configure the notification channel.
            mChannel.setDescription(description);
            mChannel.enableLights(true);
            // Sets the notification light color for notifications posted to this
            // channel, if the device supports this feature.
            mChannel.setLightColor(Color.RED);
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

    public Notification buildNotification(@NonNull PlaybackStateCompat state,
                                           MediaSessionCompat.Token token,
                                           final MediaDescriptionCompat description,
                                           Bitmap bitmap)
    {
        boolean isPlaying = state.getState() == PlaybackStateCompat.STATE_PLAYING;
        Log.d(TAG, "buildNotification: playing? "+isPlaying);
        // Create the (mandatory) notification channel when running on Android Oreo.
        if (isAndroidOOrHigher()) {
            createChannel();
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mMediaService,CHANNEL_ID);
        builder.setStyle(
                new androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(token)
                .setShowActionsInCompactView(0,1,2)
               )
                .setColor(ContextCompat.getColor(mMediaService, R.color.notification_bg))
                .setSmallIcon(R.drawable.ic_audiotrack_white_24dp)
                // Pending intent that is fired when user clicks on notification.
                .setContentIntent(createContentIntent())
                // Title - Usually Song name.
                .setContentTitle(description.getTitle())
                // Subtitle - Usually Artist name.
                .setContentText(description.getSubtitle())
                .setLargeIcon(bitmap)
                // When notification is deleted (when playback is paused and notification can be
                // deleted) fire MediaButtonPendingIntent with ACTION_STOP.
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(
                        mMediaService, PlaybackStateCompat.ACTION_STOP)
                )
                // Show controls on lock screen even when user hides sensitive content.
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        // If skip to previous action is enabled.
        if ((state.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS) != 0) {
            builder.addAction(mPrevAction);
            Log.d(TAG, "buildNotification: prev");
        }
        builder.addAction(isPlaying ? mPauseAction : mPlayAction);
        // If skip to next action is enabled.
        if ((state.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_NEXT) != 0) {
            builder.addAction(mNextAction);
            Log.d(TAG, "buildNotification: skip");
        }

        return builder.build();
    }

    private PendingIntent createContentIntent(){
        Intent openUi = new Intent(mMediaService, MainActivity.class);
        openUi.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(mMediaService,501,openUi,PendingIntent.FLAG_CANCEL_CURRENT);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        switch (action) {
            case ACTION_PAUSE:
                mMediaService.getCallback().onPause();
                break;
            case ACTION_PLAY:
                mMediaService.getCallback().onPlay();
                break;
            case ACTION_NEXT:
                mMediaService.getCallback().onSkipToNext();
                break;
            case ACTION_PREV:
                mMediaService.getCallback().onSkipToPrevious();
                break;
        }
    }

    public void unregisterNotificationBroadcast() {
        mMediaService.unregisterReceiver(this);
    }
}
