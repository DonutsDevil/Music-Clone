package com.example.music_clone.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import static com.example.music_clone.util.Constants.PLAYLIST_ID;

public class MyPreferenceManager {
    private static final String TAG = "MyPreferenceManager";

    private SharedPreferences mPreferences;

    public MyPreferenceManager(Context context) {
        this.mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void setPlaylistId(String playlistId) {
        SharedPreferences.Editor editor =  mPreferences.edit();
        editor.putString(PLAYLIST_ID,playlistId);
        editor.apply();
    }

    public String getPlaylistId() {
        return mPreferences.getString(PLAYLIST_ID,"");
    }
}
