package com.example.music_clone.ui;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.music_clone.MediaSeekBar;
import com.example.music_clone.R;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


public class MediaControllerFragment extends Fragment implements
        View.OnClickListener {


    private static final String TAG = "MediaControllerFragment";


    // UI Components
    private TextView mSongTitle;
    private ImageView mPlayPause;
    private MediaSeekBar mSeekbarAudio;
    private boolean mIsPlaying;
    private MediaMetadataCompat mSelectedMedia;

    // Vars
    private IMainActivity mIMainActivity;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_media_controller, container, false);
    }

    @Override
    public void onViewCreated(@NonNull  View view, @Nullable  Bundle savedInstanceState) {
        mSongTitle = view.findViewById(R.id.media_song_title);
        mPlayPause = view.findViewById(R.id.play_pause);
        mSeekbarAudio = view.findViewById(R.id.seekbar_audio);
        mPlayPause.setOnClickListener(this);
        if (savedInstanceState != null) {
            mSelectedMedia = savedInstanceState.getParcelable("selected_media");
            if (mSelectedMedia != null) {
                setMediaTitle(mSelectedMedia);
                setIsPlaying(savedInstanceState.getBoolean("is_playing"));
            }
        }
    }

    public MediaSeekBar getMediaSeekBar(){
        return mSeekbarAudio;
    }
    @Override
    public void onClick(View v) {
        if (v.getId()==R.id.play_pause) {
            Log.d(TAG, "onClick: called");
            mIMainActivity.playPause();
        }
    }

    // Call when we select a song from PlaylistFragment
    public void setIsPlaying(boolean isPlaying) {
        mIsPlaying = isPlaying;
        if(isPlaying) {
            Glide.with(getActivity())
                    .load(R.drawable.ic_pause_circle_outline_white_24dp)
                    .into(mPlayPause);
        }else{
            Glide.with(getActivity())
                    .load(R.drawable.ic_play_circle_outline_white_24dp)
                    .into(mPlayPause);
        }
    }

    // Call when we select a song from PlaylistFragment
    public void setMediaTitle(MediaMetadataCompat mediaItem) {
        Log.d(TAG, "setMediaTitle: MediaBrowserHelper "+mediaItem.getDescription().getTitle());
        mSelectedMedia = mediaItem;
        mSongTitle.setText(mediaItem.getDescription().getTitle());
    }

    @Override
    public void onAttach(@NonNull  Context context) {
        super.onAttach(context);
        mIMainActivity = ((IMainActivity)getActivity());
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("selected_media",mSelectedMedia);
        outState.putBoolean("is_playing",mIsPlaying);
    }
}


















