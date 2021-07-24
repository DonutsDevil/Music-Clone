package com.example.music_clone.services;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media.MediaBrowserServiceCompat;

import com.example.music_clone.MyApplication;
import com.example.music_clone.players.MediaPlayerAdapter;
import com.example.music_clone.players.PlayerAdapter;
import com.example.music_clone.util.MediaLibrary;

import java.util.ArrayList;
import java.util.List;

public class MediaService extends MediaBrowserServiceCompat {
    private static final String TAG = "MediaService";
    private MediaSessionCompat mSession;
    private PlayerAdapter mPlayback;
    private MyApplication mMyApplication
;    @Override
    public void onCreate()  {
        super.onCreate();
        mMyApplication = MyApplication.getInstance();
        mSession = new MediaSessionCompat(this,TAG);
        mSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        // https://developer.android.com/guide/topics/media-apps/mediabuttons#mediabuttons-and-active-mediasessions
                        // Media buttons on the device
                        // (handles the PendingIntents for MediaButtonReceiver.buildMediaButtonPendingIntent)
                MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS );// Control the items in the queue (aka playlist)
        // See https://developer.android.com/guide/topics/media-apps/mediabuttons for more info on flags
        mSession.setCallback(new MediaSessionCallbacks());
        // A token that can be used to create a MediaController for this session
        setSessionToken(mSession.getSessionToken());

        mPlayback = new MediaPlayerAdapter(this);
    }

    /*
    * This class is called when we remove the app from recent task
    * */
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(TAG, "onTaskRemoved: Stopped");
        super.onTaskRemoved(rootIntent);
        // we need to stop the service we started.
        mPlayback.stop();
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: MediaPlayerAdapter stopped, and MediaSession released");
        // We should release if we don't have any other media to play or activity is destroyed
        mSession.release();
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable  Bundle rootHints) {

        if(clientPackageName.equals(getApplicationContext().getPackageName())) {
            // allowed to browser media
            return new BrowserRoot("some_real_playlist",null);
        }
        return new BrowserRoot("empty_media",null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull  MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>> result) {
        Log.d(TAG, "onLoadChildren: called: " + parentId + ", " + result);
        //  Browsing not allowed
        if(TextUtils.equals("empty_media",parentId)) {
            result.sendResult(null);
            return;
        }
        result.sendResult(mMyApplication.getMediaItems()); // return all available media
    }

    public class MediaSessionCallbacks extends MediaSessionCompat.Callback {

        private final List<MediaSessionCompat.QueueItem> mPlayList = new ArrayList<>();
        private int mQueueIndex = -1;
        private MediaMetadataCompat mPreparedMedia;

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            Log.d(TAG, "onPlayFromMediaId: Called");
            mPreparedMedia = mMyApplication.getMediaItem(mediaId);
            mSession.setMetadata(mPreparedMedia);
            if (!mSession.isActive()) {
                mSession.setActive(true);
            }
            mPlayback.playFromMedia(mPreparedMedia);
        }

        @Override
        public void onPrepare() {
            if (mQueueIndex < 0 && mPlayList.isEmpty()) {
                return;
            }
            String mediaId = mPlayList.get(mQueueIndex).getDescription().getMediaId();
            mPreparedMedia = mMyApplication.getMediaItem(mediaId);

            if(!mSession.isActive()) {
                mSession.setActive(true);
            }
        }

        @Override
        public void onPlay() {
            if (!isReadyToPlay()) {
                return;
            }

            if(mPreparedMedia == null) {
                onPrepare();
            }

            mPlayback.playFromMedia(mPreparedMedia);
        }

        @Override
        public void onPause() {
            mPlayback.pause();
        }

        @Override
        public void onSkipToNext() {
            Log.d(TAG, "onSkipToNext: SKIP TO NEXT");
            mQueueIndex = (++mQueueIndex % mPlayList.size());
            mPreparedMedia = null;
            onPlay();
        }

        @Override
        public void onSkipToPrevious() {
            Log.d(TAG, "onSkipToPrevious: SKIP TO Previous");
            mQueueIndex = mQueueIndex > 0 ? mQueueIndex - 1 : mPlayList.size() - 1;
            mPreparedMedia = null;
            onPlay();
        }

        @Override
        public void onStop() {
            mPlayback.stop();
            mSession.setActive(false);
        }

        @Override
        public void onSeekTo(long pos) {
            mPlayback.seekTo(pos);
        }

        @Override
        public void onAddQueueItem(MediaDescriptionCompat description) {
            Log.d(TAG, "onAddQueueItem: Called : Position in list "+mPlayList.size());
            mPlayList.add(new MediaSessionCompat.QueueItem(description,description.hashCode()));
            mQueueIndex = mQueueIndex == -1 ? 0 : mQueueIndex;
            mSession.setQueue(mPlayList);
        }

        @Override
        public void onRemoveQueueItem(MediaDescriptionCompat description) {
            Log.d(TAG, "onRemoveQueueItem: Called : Position in list "+mPlayList.size());
            mPlayList.remove(new MediaSessionCompat.QueueItem(description,description.hashCode()));
            mQueueIndex = mPlayList.isEmpty() ? -1 : mQueueIndex;
            mSession.setQueue(mPlayList);
        }

        private boolean isReadyToPlay() {
            return !mPlayList.isEmpty();
        }
    }
}
