package com.example.music_clone.services;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media.MediaBrowserServiceCompat;

import java.util.List;

public class MediaService extends MediaBrowserServiceCompat {
    private static final String TAG = "MediaService";
    private MediaSessionCompat mSession;

    public MediaService(){
        mSession = new MediaSessionCompat(this,TAG);
        mSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS
                );
        mSession.setCallback(new MediaSessionCallbacks());
        setSessionToken(mSession.getSessionToken());
    }

    /*
    * This class is called when we remove the app from recent task
    * */
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        // we need to stop the service we started.
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // We should release if we don't have any other media to play or activity is destroyed
        mSession.release();
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable  Bundle rootHints) {

        if(clientPackageName.equals(getApplicationContext().getPackageName())) {
            // allowed to browser media
        }
        return new BrowserRoot("empty_media",null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull  MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>> result) {
        if(TextUtils.equals("empty_media",parentId)) {
            result.sendResult(null);
            return;
        }
        result.sendResult(null);
    }

    public class MediaSessionCallbacks extends MediaSessionCompat.Callback {
        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            super.onPlayFromMediaId(mediaId, extras);
        }

        @Override
        public void onPrepare() {
            super.onPrepare();
        }

        @Override
        public void onPlay() {
            super.onPlay();
        }

        @Override
        public void onPause() {
            super.onPause();
        }

        @Override
        public void onSkipToNext() {
            super.onSkipToNext();
        }

        @Override
        public void onSkipToPrevious() {
            super.onSkipToPrevious();
        }

        @Override
        public void onStop() {
            super.onStop();
        }

        @Override
        public void onSeekTo(long pos) {
            super.onSeekTo(pos);
        }

        @Override
        public void onAddQueueItem(MediaDescriptionCompat description) {
            super.onAddQueueItem(description);
        }

        @Override
        public void onRemoveQueueItem(MediaDescriptionCompat description) {
            super.onRemoveQueueItem(description);
        }
    }
}
