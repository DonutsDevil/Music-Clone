package com.example.music_clone.client;

import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;

// This class will send state message from mediaBrowserHelper to UI
public interface MediaBrowserHelperCallback {
    void onMetaDataChanged(final MediaMetadataCompat metadata);

    void onPlaybackStateChanged(PlaybackStateCompat state);

    void onMediaControllerConnected(MediaControllerCompat mediaController);
}
