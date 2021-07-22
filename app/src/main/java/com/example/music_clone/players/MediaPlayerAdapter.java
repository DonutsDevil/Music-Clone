package com.example.music_clone.players;

import  android.content.Context;
import android.net.Uri;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

public class MediaPlayerAdapter extends PlayerAdapter {
    private static final String TAG = "MediaPlayerAdapter";
    private Context mContext;
    private MediaMetadataCompat mCurrentMedia;
    private boolean mCurrentMediaPlayedToCompletion;
    private int mState;
    // track buffering time in ExoPlayer Buffer state
    private long mStartTime;

    // ExoPlayer Objects
    private SimpleExoPlayer mExoPlayer;
    private TrackSelector mTrackSelector;
    private DefaultRenderersFactory mRenderer;
    private DataSource.Factory mDataSourceFactory;

    public MediaPlayerAdapter(@NonNull Context context) {
        super(context);
        mContext = context;
    }

    private void initializeExoPlayer(){
        if(mExoPlayer == null) {
            mTrackSelector = new DefaultTrackSelector();
            mRenderer = new DefaultRenderersFactory(mContext);
            mDataSourceFactory = new DefaultDataSourceFactory(mContext, Util.getUserAgent(mContext,"Music-Clone"));
            mExoPlayer = new SimpleExoPlayer.Builder(mContext,mRenderer)
                    .setTrackSelector(mTrackSelector)
                    .setLoadControl(new DefaultLoadControl())
                    .build();
        }
    }

    private void release() {
        if(mExoPlayer != null) {
            mExoPlayer.release();
            mExoPlayer = null;
        }
    }
    @Override
    protected void onPlay() {
        if(mExoPlayer != null && !mExoPlayer.getPlayWhenReady()) {
            mExoPlayer.setPlayWhenReady(true);
            setNewState(PlaybackStateCompat.STATE_PLAYING);
        }
    }

    @Override
    protected void onPause() {
        if(mExoPlayer != null && !mExoPlayer.getPlayWhenReady()) {
            mExoPlayer.setPlayWhenReady(false);
            setNewState(PlaybackStateCompat.STATE_PAUSED);
        }
    }

    @Override
    public void playFromMedia(MediaMetadataCompat metadata) {
        startTrackingPlayback();
        playFile(metadata);
    }

    @Override
    public MediaMetadataCompat getCurrentMedia() {
        return mCurrentMedia;
    }

    @Override
    public boolean isPlaying() {
        if (mExoPlayer != null) {
            return mExoPlayer.getPlayWhenReady();
        }
        return false;
    }

    @Override
    protected void onStop() {
        setNewState(PlaybackStateCompat.STATE_STOPPED);
        release();
    }

    @Override
    public void seekTo(long position) {
        if (mExoPlayer != null) {
            mExoPlayer.seekTo((int) position);
            setNewState(mState);
        }
    }

    @Override
    public void setVolume(float volume) {
        if (mExoPlayer != null) {
            mExoPlayer.setVolume(volume);
        }
    }

    private void playFile(MediaMetadataCompat metadata) {
        // Check if we played first media or current media is changed.
        String mediaId = metadata.getDescription().getMediaId();
        boolean mediaChanged = (mCurrentMedia == null) || !mediaId.equals(mCurrentMedia.getDescription().getMediaId());
        // for looping song
        if(mCurrentMediaPlayedToCompletion) {
            mediaChanged = true;
            mCurrentMediaPlayedToCompletion = false;
        }
        // check if we clicked the same song playing
        if(!mediaChanged) {
            if(!isPlaying()) {
                play();
            }
            return;
        }
        else {
            release();
        }
        mCurrentMedia = metadata;
        initializeExoPlayer();
        try {
            MediaSource audioSource = new ProgressiveMediaSource.Factory(mDataSourceFactory)
                    .createMediaSource(Uri.parse(mCurrentMedia.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI)));
            mExoPlayer.prepare(audioSource);
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to play media url "
                +mCurrentMedia.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI), e);
        }
        play();
    }

    private void startTrackingPlayback() {

    }

    private void setNewState(@PlaybackStateCompat.State int newPlayerState) {
        mState = newPlayerState;
        if(mState == PlaybackStateCompat.STATE_STOPPED) {
            mCurrentMediaPlayedToCompletion = true;
        }
        final long reportPosition = mExoPlayer == null ? 0 : mExoPlayer.getCurrentPosition();
    }

    /**
     * Set the current capabilities available on this session. Note: If a capability is not
     * listed in the bitmask of capabilities then the MediaSession will not handle it. For
     * example, if you don't want ACTION_STOP to be handled by the MediaSession, then don't
     * included it in the bitmask that's returned.
     */
    @PlaybackStateCompat.Actions
    private long getAvailableActions() {
        long actions = PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
                | PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH
                | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS;
        switch (mState) {
            case PlaybackStateCompat.STATE_STOPPED:
                actions |= PlaybackStateCompat.ACTION_PLAY
                        | PlaybackStateCompat.ACTION_PAUSE;
                break;
            case PlaybackStateCompat.STATE_PLAYING:
                actions |= PlaybackStateCompat.ACTION_STOP
                        | PlaybackStateCompat.ACTION_PAUSE
                        | PlaybackStateCompat.ACTION_SEEK_TO;
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                actions |= PlaybackStateCompat.ACTION_PLAY
                        | PlaybackStateCompat.ACTION_STOP;
                break;
            default:
                actions |= PlaybackStateCompat.ACTION_PLAY
                        | PlaybackStateCompat.ACTION_PLAY_PAUSE
                        | PlaybackStateCompat.ACTION_STOP
                        | PlaybackStateCompat.ACTION_PAUSE;
        }
        return actions;
    }

    private class ExoPlayerEventListener implements Player.EventListener {

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            switch (playbackState) {
                case Player.STATE_ENDED: {
                    setNewState(PlaybackStateCompat.STATE_PAUSED);
                    break;
                }
                case Player.STATE_BUFFERING: {
                    mStartTime = System.currentTimeMillis();
                    break;
                }
                case Player.STATE_IDLE: {

                    break;
                }
                case Player.STATE_READY: {
                    Log.d(TAG, "onPlayerStateChanged: Time ELAPSED "+(System.currentTimeMillis() - mStartTime));
                    break;
                }
            }
        }
    }
}
