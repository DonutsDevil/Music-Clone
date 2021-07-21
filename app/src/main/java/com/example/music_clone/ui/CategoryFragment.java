package com.example.music_clone.ui;


import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.music_clone.R;
import com.example.music_clone.adapters.CategoryRecyclerAdapter;
import com.example.music_clone.adapters.HomeRecyclerAdapter;
import com.example.music_clone.models.Artist;
import com.google.android.exoplayer2.C;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;


public class CategoryFragment extends Fragment implements CategoryRecyclerAdapter.ICategorySelector {

    private static final String TAG = "CategoryFragment";


    // UI Components
    private RecyclerView mRecyclerView;

    // Vars
    private CategoryRecyclerAdapter mAdapter;
    private ArrayList<Artist> mArtist = new ArrayList<>();
    private IMainActivity mIMainActivity;
    private String mSelectedCategory;

    public static CategoryFragment newInstance(String category) {
        CategoryFragment categoryFragment = new CategoryFragment();
        Bundle args = new Bundle();
        args.putString("category",category);
        categoryFragment.setArguments(args);
        return categoryFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            mSelectedCategory = getArguments().getString("category");
        }
        setRetainInstance(true);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if(!hidden)
            mIMainActivity.setActionBarTitle(mSelectedCategory);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initRecyclerView(view);
        mIMainActivity.setActionBarTitle(mSelectedCategory);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mIMainActivity = (IMainActivity) getActivity();
    }

    private void retrieveArtist() {
        mIMainActivity.showProgressBar();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        Query query = firestore
                .collection(getString(R.string.collection_audio))
                .document(getString(R.string.document_categories))
                .collection(mSelectedCategory);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    Log.d(TAG, "onComplete: getQuery ==> "+task.getResult());
                    for(QueryDocumentSnapshot document : task.getResult()) {
                        mArtist.add(document.toObject(Artist.class));
                    }
                }else {
                    Log.d(TAG, "onComplete error getting documents: "+task.getException());
                }
                updateDataSet();
            }
        });
    }
    private void updateDataSet() {
        mIMainActivity.hideProgressBar();
        mAdapter.notifyDataSetChanged();
    }
    private void initRecyclerView(View view) {
        mRecyclerView = view.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new CategoryRecyclerAdapter(mArtist ,getActivity(),this);
        mRecyclerView.setAdapter(mAdapter);
        if(mArtist.size() == 0) {
            retrieveArtist();
        }

    }


    @Override
    public void onArtistSelected(int position) {
        mIMainActivity.onArtistSelected(mSelectedCategory,mArtist.get(position));
    }
}















