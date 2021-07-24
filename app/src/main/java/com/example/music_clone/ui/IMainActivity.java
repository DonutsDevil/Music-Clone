package com.example.music_clone.ui;

import android.support.v4.media.MediaMetadataCompat;

import com.example.music_clone.MyApplication;
import com.example.music_clone.models.Artist;

public interface IMainActivity {
    void hideProgressBar();
    void showProgressBar();
    void onCategorySelected(String category);
    void onArtistSelected(String category, Artist artist);
    void setActionBarTitle(String title);
    void playPause();
    MyApplication getMyApplication();
    void onMediaSelected(String playlistId, MediaMetadataCompat mediaItem);
}
