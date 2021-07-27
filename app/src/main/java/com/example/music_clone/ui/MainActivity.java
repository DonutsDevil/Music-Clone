package com.example.music_clone.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.music_clone.MyApplication;
import com.example.music_clone.R;
import com.example.music_clone.client.MediaBrowserHelper;
import com.example.music_clone.client.MediaBrowserHelperCallback;
import com.example.music_clone.models.Artist;
import com.example.music_clone.services.MediaService;
import com.example.music_clone.util.MainActivityFragmentManager;
import com.example.music_clone.util.MyPreferenceManager;

import java.util.ArrayList;

import static com.example.music_clone.util.Constants.MEDIA_QUEUE_POSITION;
import static com.example.music_clone.util.Constants.QUEUE_NEW_PLAYLIST;
import static com.example.music_clone.util.Constants.SEEK_BAR_MAX;
import static com.example.music_clone.util.Constants.SEEK_BAR_PROGRESS;

public class MainActivity extends AppCompatActivity implements
        IMainActivity, MediaBrowserHelperCallback
{

    private static final String TAG = "MainActivity";

    // UI
    private ProgressBar mProgressBar;

    // Vars
    private MediaBrowserHelper mMediaBrowserHelper;
    private MyApplication mMyApplication;
    private MyPreferenceManager mMyPrefManager;
    private boolean mIsPlaying;
    private SeekBarBroadcastReceiver mSeekBarBroadcastReceiver;
    private UpdateUIBroadcastReceiver mUpdateUIBroadcastReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProgressBar = findViewById(R.id.progress_bar);

        mMyPrefManager = new MyPreferenceManager(this);
        mMediaBrowserHelper = new MediaBrowserHelper(this, MediaService.class);

        mMediaBrowserHelper.setMediaBrowserHelperCallback(this);
        mMyApplication = MyApplication.getInstance();

        if (savedInstanceState == null) {
            loadFragment(HomeFragment.newInstance(), true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initSeekBarBroadcastReceiver();
        initUpdateUIBroadcastReceiver();
    }

    private void initSeekBarBroadcastReceiver(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(getString(R.string.broadcast_seekbar_update));
        mSeekBarBroadcastReceiver  = new SeekBarBroadcastReceiver();
        registerReceiver(mSeekBarBroadcastReceiver,intentFilter);
    }

    private void initUpdateUIBroadcastReceiver(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(getString(R.string.broadcast_update_ui));
        mUpdateUIBroadcastReceiver  = new UpdateUIBroadcastReceiver();
        registerReceiver(mUpdateUIBroadcastReceiver,intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mSeekBarBroadcastReceiver != null) {
            unregisterReceiver(mSeekBarBroadcastReceiver);
        }
        if (mUpdateUIBroadcastReceiver != null) {
            unregisterReceiver(mUpdateUIBroadcastReceiver);
        }
    }

    private class UpdateUIBroadcastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String mediaId = intent.getStringExtra(getString(R.string.broadcast_new_media_id));
            Log.d(TAG, "onReceive: new Media id: "+mediaId);
            if (getPlaylistFragment() != null) {
                getPlaylistFragment().updateUI(mMyApplication.getMediaItem(mediaId));
            }
        }
    }

    private class SeekBarBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            long seekProgress = intent.getLongExtra(SEEK_BAR_PROGRESS,0);
            long maxProgress = intent.getLongExtra(SEEK_BAR_MAX,0);

            if (!getMediaControllerFragment().getMediaSeekBar().isTracking()) {
                getMediaControllerFragment().getMediaSeekBar().setProgress((int)seekProgress);
                getMediaControllerFragment().getMediaSeekBar().setMax((int)maxProgress);
            }
        }
    }
    @Override
    public void playPause() {
        Log.d(TAG, "playPause: mIsPlaying : "+mIsPlaying);
        if(mIsPlaying) {
            mMediaBrowserHelper.getTransportControls().pause();
        }else {
            mMediaBrowserHelper.getTransportControls().play();
        }
    }

    // This is used to set Playlist in the whole app.
    @Override
    public MyApplication getMyApplication() {
        return mMyApplication;
    }

    @Override
    public void onMediaSelected(String playlistId, MediaMetadataCompat mediaItem, int queuePosition) {
        if (mediaItem != null) {
            Log.d(TAG, "onMediaSelected: called = " +mediaItem.getDescription().getMediaId());
            String currentPlaylistId =getMyPrefManager().getPlaylistId();
            Bundle bundle = new Bundle();
            bundle.putInt(MEDIA_QUEUE_POSITION, queuePosition);
            if (playlistId.equals(currentPlaylistId)) {
                mMediaBrowserHelper.getTransportControls().playFromMediaId(mediaItem.getDescription().getMediaId(),bundle);
            }
            else {
                bundle.putBoolean(QUEUE_NEW_PLAYLIST,true);
                mMediaBrowserHelper.subscribeToNewPlaylist(playlistId);
                mMediaBrowserHelper.getTransportControls().playFromMediaId(mediaItem.getDescription().getMediaId(),bundle);
            }


        }
        else {
            Toast.makeText(this, "Select Something to play", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public MyPreferenceManager getMyPrefManager() {
        return mMyPrefManager;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMediaBrowserHelper.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        getMediaControllerFragment().getMediaSeekBar().disconnectController();
        mMediaBrowserHelper.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("active_fragments",MainActivityFragmentManager.getInstance().getFragments().size());
    }

    private void loadFragment(Fragment fragment, boolean lateralMovement) {
        FragmentTransaction  transaction = getSupportFragmentManager().beginTransaction();
        if(lateralMovement) {
            transaction.setCustomAnimations(R.anim.enter_from_right,R.anim.exit_to_left);
        }
        String tag ="";
        if(fragment instanceof HomeFragment) {
            tag = getString(R.string.fragment_home);
        }else if(fragment instanceof  CategoryFragment) {
            tag = getString(R.string.fragment_category);
            transaction.addToBackStack(tag);
        }else if(fragment instanceof PlaylistFragment) {
            tag = getString(R.string.fragment_playlist);
            transaction.addToBackStack(tag);
        }
        transaction.add(R.id.main_container,fragment,tag);
        transaction.commit();

        MainActivityFragmentManager.getInstance().addFragment(fragment);
        // False cause we don't want to have back navigation animation
        // we want to show just the current fragment and hide other
        showFragment(fragment,false);
    }

    private void showFragment(Fragment fragment, boolean backwardsMovement) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if(backwardsMovement) {
            transaction.setCustomAnimations(R.anim.enter_from_left,R.anim.exit_to_right);
        }
        transaction.show(fragment);
        transaction.commit();

        for(Fragment f: MainActivityFragmentManager.getInstance().getFragments()) {
            if(f != null) {
                if(!f.getTag().equals(fragment.getTag())) {
                    FragmentTransaction t = getSupportFragmentManager().beginTransaction();
                    t.hide(f);
                    t.commit();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        ArrayList<Fragment> fragments = new ArrayList<>(MainActivityFragmentManager.getInstance().getFragments());
        if(fragments.size() > 1) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.remove(fragments.get(fragments.size()-1));

            MainActivityFragmentManager.getInstance().removeFragment(fragments.size()-1);
            showFragment(fragments.get(fragments.size()-2),true);
        }
        super.onBackPressed();
    }

    @Override
    public void hideProgressBar() {
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void showProgressBar() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCategorySelected(String category) {
        loadFragment(CategoryFragment.newInstance(category),true);
    }

    @Override
    public void onArtistSelected(String category, Artist artist) {
        loadFragment(PlaylistFragment.newInstance(category,artist),true);
    }

    @Override
    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }


    @Override
    public void onMetaDataChanged(MediaMetadataCompat metadata) {
        if (metadata == null) {
            return;
        }
        if (getMediaControllerFragment() != null)
        getMediaControllerFragment().setMediaTitle(metadata);
    }

    @Override
    public void onPlaybackStateChanged(final PlaybackStateCompat state) {

        mIsPlaying = state != null &&
                state.getState() == PlaybackStateCompat.STATE_PLAYING;
        Log.d(TAG, "onPlaybackStateChanged: called mIsPlaying = "+mIsPlaying    );
        // Update UI
        if (getMediaControllerFragment() != null) {
            getMediaControllerFragment().setIsPlaying(mIsPlaying);
        }
    }

    @Override
    public void onMediaControllerConnected(MediaControllerCompat mediaController) {
        getMediaControllerFragment().getMediaSeekBar().setMediaController(mediaController);
    }

    private  MediaControllerFragment getMediaControllerFragment() {
        MediaControllerFragment mediaControllerFragment = (MediaControllerFragment) getSupportFragmentManager()
                .findFragmentById(R.id.bottom_media_controller);
        if (mediaControllerFragment != null) {
            return mediaControllerFragment;
        }
        return null;
    }

    private PlaylistFragment getPlaylistFragment() {
        PlaylistFragment playlistFragment = (PlaylistFragment) getSupportFragmentManager()
                .findFragmentByTag(getString(R.string.fragment_playlist));
        if (playlistFragment != null) {
            return playlistFragment;
        }
        return null;
    }
}
