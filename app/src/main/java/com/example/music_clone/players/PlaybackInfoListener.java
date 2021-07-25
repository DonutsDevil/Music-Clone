package com.example.music_clone.players;

import android.support.v4.media.session.PlaybackStateCompat;

public interface PlaybackInfoListener {
    // Detects the changes in the playback change from the main UI
    void onPlaybackStateChange(PlaybackStateCompat state);
}
