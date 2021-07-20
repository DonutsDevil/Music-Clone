package com.example.music_clone.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.music_clone.R;
import com.example.music_clone.models.Artist;
import com.example.music_clone.util.MainActivityFragmentManager;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements IMainActivity {

    private static final String TAG = "MainActivity";

    // UI
    private ProgressBar mProgressBar;

    // Vars

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProgressBar = findViewById(R.id.progress_bar);
        loadFragment(HomeFragment.newInstance(),true);
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
}
