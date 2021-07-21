package com.example.music_clone.players;

import android.content.Context;
import android.net.Uri;
import android.support.v4.media.MediaMetadataCompat;

import androidx.annotation.NonNull;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

public class MediaPlayerAdapter extends PlayerAdapter {

    private Context mContext;
    private MediaMetadataCompat mCurrentMedia;
    private boolean mCurrentMediaPlayedToCompletion;

    // ExoPlayer Objects
    private SimpleExoPlayer mExoPlayer;
    private TrackSelector mTrackSelector;
    private DefaultRenderersFactory mRenderer;
    private DataSource.Factory mDataSourceFactory;

    public MediaPlayerAdapter(@NonNull Context context) {
        super(context);
    }

    private void initializeExoPlayer(){
        if(mExoPlayer == null) {
            mTrackSelector = new DefaultTrackSelector();
            mRenderer = new DefaultRenderersFactory(mContext);
            mDataSourceFactory = new DefaultDataSourceFactory(mContext, Util.getUserAgent(mContext,"music_clone"));
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

    }

    @Override
    protected void onPause() {

    }

    @Override
    public void playFromMedia(MediaMetadataCompat metadata) {
        startTrackingPlayback();
        playFile(metadata);
    }

    @Override
    public MediaMetadataCompat getCurrentMedia() {
        return null;
    }

    @Override
    public boolean isPlaying() {
        return false;
    }

    @Override
    protected void onStop() {

    }

    @Override
    public void seekTo(long position) {

    }

    @Override
    public void setVolume(float volume) {

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
}
