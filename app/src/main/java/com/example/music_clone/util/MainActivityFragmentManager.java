package com.example.music_clone.util;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;

// This class is responsible to manage all the fragments in this app: Backstack navigation.
public class  MainActivityFragmentManager {

    private ArrayList<Fragment> mFragments = new ArrayList<>();
    private static MainActivityFragmentManager instance;
    public static MainActivityFragmentManager getInstance() {
        if(instance == null) {
            instance = new MainActivityFragmentManager();
        }
        return instance;
    }

    public void addFragment(Fragment fragment) {
        mFragments.add(fragment);
    }

    public void removeFragment(Fragment fragment) {
        mFragments.remove(fragment);
    }

    public void removeFragment(int position) {
        mFragments.remove(position);
    }

    public ArrayList<Fragment> getFragments() {
        return mFragments;
    }

    public void removeAllFragments() {
        mFragments.clear();
    }
}
