package com.example.music_clone.client;

import android.content.ComponentName;
import android.content.Context;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.media.MediaBrowserServiceCompat;

import java.util.List;

public class MediaBrowserHelper {

    private static final String TAG = "MediaBrowserHelper";

    private final Context mContext;
    private final Class<? extends MediaBrowserServiceCompat> mMediaBrowserServiceClass;

    private MediaBrowserCompat mMediaBrowser;
    private MediaControllerCompat mMediaController;

    private MediaBrowserConnectionCallback mMediaBrowserConnectionCallback;
    private MediaBrowserSubscriptionCallback mMediaBrowserSubscriptionCallback;
    private MediaControllerCallback mMediaControllerCallback;
    private MediaBrowserHelperCallback mMediaBrowserCallback;

    public MediaBrowserHelper(Context context, Class<? extends MediaBrowserServiceCompat> mediaBrowserServiceClass) {
        this.mContext = context;
        this.mMediaBrowserServiceClass = mediaBrowserServiceClass;

        mMediaBrowserSubscriptionCallback = new MediaBrowserSubscriptionCallback();
        mMediaBrowserConnectionCallback = new MediaBrowserConnectionCallback();
        mMediaControllerCallback = new MediaControllerCallback();
    }
    public void setMediaBrowserHelperCallback(MediaBrowserHelperCallback browserHelperCallback) {
        mMediaBrowserCallback =  browserHelperCallback;
    }

    // Receives callbacks from the MediaController and updates the UI state,
    // i.e.: Which is the current item, whether it's playing or paused, etc.
    private class MediaControllerCallback extends MediaControllerCompat.Callback {

        @Override
        public void onPlaybackStateChanged(final PlaybackStateCompat state) {
            Log.d(TAG, "onPlaybackStateChanged: called");
            if (mMediaBrowserCallback != null) {
                Log.d(TAG, "onPlaybackStateChanged: called in if");
                mMediaBrowserCallback.onPlaybackStateChanged(state);
            }
        }

        @Override
        public void onMetadataChanged(final MediaMetadataCompat metadata) {
            Log.d(TAG, "onMetadataChanged: called");
            if (mMediaBrowserCallback != null) {
                mMediaBrowserCallback.onMetaDataChanged(metadata);
            }
        }
    }
    public void subscribeToNewPlaylist(String playlistId) {
        mMediaBrowser.subscribe(playlistId,mMediaBrowserSubscriptionCallback );
    }
    public void onStart() {
        if(mMediaBrowser == null) {
            mMediaBrowser = new MediaBrowserCompat(
                    mContext,
                    new ComponentName(mContext,mMediaBrowserServiceClass),
                    mMediaBrowserConnectionCallback,
                    null);
            mMediaBrowser.connect();
            Log.d(TAG, "onStart: connecting to the service");
        }
    }

    public void onStop() {
        if(mMediaController != null) {
            mMediaController.unregisterCallback(mMediaControllerCallback);
            mMediaController = null;
        }
        if(mMediaBrowser != null && mMediaBrowser.isConnected()) {
            mMediaBrowser.disconnect();
            mMediaBrowser = null;
        }

        Log.d(TAG, "onStop: disconnecting from the service");
    }

    // Receives callbacks from the MediaBrowser when it has successfully connected to the
    // MediaBrowserService (MusicService).
    private class MediaBrowserConnectionCallback extends MediaBrowserCompat.ConnectionCallback {
        // Happens as a result of onStart().
        @Override
        public void onConnected() {
            Log.d(TAG, "onConnected: Called");

            try{
                // Get a MediaController for the MediaSession.
                mMediaController = new MediaControllerCompat(mContext,mMediaBrowser.getSessionToken());
                mMediaController.registerCallback(mMediaControllerCallback);
                mMediaBrowserCallback.onMediaControllerConnected(mMediaController);
            }
            catch (Exception e) {
                Log.d(TAG, "onConnected: connection problem "+e.toString());
            }

            mMediaBrowser.subscribe(mMediaBrowser.getRoot(), mMediaBrowserSubscriptionCallback);
        }
    }

    // Receives callbacks from the MediaBrowser when the MediaBrowserService has loaded new media
    // that is ready for playback.
    public class MediaBrowserSubscriptionCallback extends MediaBrowserCompat.SubscriptionCallback {
        @Override
        public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children) {
            Log.d(TAG, "onChildrenLoaded: called "+parentId+" , "+children.toString());

            for(final MediaBrowserCompat.MediaItem mediaItem : children) {
                Log.d(TAG, "onChildrenLoaded: CALLED: queue item: " + mediaItem.getMediaId());
                mMediaController.addQueueItem(mediaItem.getDescription());
            }
        }
    }

    public MediaControllerCompat.TransportControls getTransportControls() {
        if (mMediaController == null) {
            Log.d(TAG, "getTransportControls: MediaController is null!");
            throw new IllegalStateException("Media Controller is null");
        }
        return mMediaController.getTransportControls();
    }
}
