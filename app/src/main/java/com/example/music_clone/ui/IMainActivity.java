package com.example.music_clone.ui;

import com.example.music_clone.models.Artist;

public interface IMainActivity {
    void hideProgressBar();
    void showProgressBar();
    void onCategorySelected(String category);
    void onArtistSelected(String category, Artist artist);
    void setActionBarTitle(String title);
    void playPause();
}
