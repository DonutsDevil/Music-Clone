package com.example.music_clone.ui;

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
    public void playPause() {
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
    public void onPlaybackStateChanged(PlaybackStateCompat state) {
        mIsPlaying = state!=null &&
                state.getState() == PlaybackStateCompat.STATE_PLAYING;
        // Update UI
        if (getMediaControllerFragment() != null)
                getMediaControllerFragment().setIsPlaying(mIsPlaying);
    }

    private  MediaControllerFragment getMediaControllerFragment() {
        MediaControllerFragment mediaControllerFragment = (MediaControllerFragment) getSupportFragmentManager()
                .findFragmentById(R.id.bottom_media_controller);
        if (mediaControllerFragment != null) {
            return mediaControllerFragment;
        }
        return null;
    }

}
