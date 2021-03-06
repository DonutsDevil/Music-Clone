package com.example.music_clone.ui;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_clone.R;
import com.example.music_clone.adapters.HomeRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;


public class HomeFragment extends Fragment implements HomeRecyclerAdapter.IHomeSelector {

    private static final String TAG = "HomeFragment";


    // UI Components
    private RecyclerView mRecyclerView;

    // Vars
    private HomeRecyclerAdapter mAdapter;
    private final ArrayList<String> mCategories = new ArrayList<>();
    private IMainActivity mIMainActivity;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if(!hidden)
        mIMainActivity.setActionBarTitle(getString(R.string.categories));
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
            setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull  View view, @Nullable  Bundle savedInstanceState) {
        initRecyclerView(view);
        mIMainActivity.setActionBarTitle(getString(R.string.categories));
    }
    // This will Retrieve All the Categories  Eg. Music, Podcasts
    private void retrieveCategories() {
        mIMainActivity.showProgressBar();
        FirebaseFirestore fireStore = FirebaseFirestore.getInstance();
        DocumentReference ref = fireStore
                .collection(getString(R.string.collection_audio))
                .document(getString(R.string.document_categories));
        ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull  Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    HashMap<String,String> categoriesMap = (HashMap)doc.getData().get("categories");
                    mCategories.addAll(categoriesMap.keySet());
                }
                updateDataSet();
            }
        });
    }

    // This method will Load all the Categories in The Recycler View
    private void updateDataSet() {
        mIMainActivity.hideProgressBar();
        mAdapter.notifyDataSetChanged();
    }
    private void initRecyclerView(View view) {
            mRecyclerView = view.findViewById(R.id.recycler_view);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            mAdapter = new HomeRecyclerAdapter(mCategories,getActivity(),this);
            mRecyclerView.setAdapter(mAdapter);
            if(mCategories.size() == 0) {
                retrieveCategories();
            }

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mIMainActivity = (IMainActivity) getActivity();
    }

    @Override
    public void onCategorySelected(int position) {
        mIMainActivity.onCategorySelected(mCategories.get(position));
    }
}















