package com.example.music_clone.ui;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_clone.R;
import com.example.music_clone.adapters.PlaylistRecyclerAdapter;
import com.example.music_clone.models.Artist;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class PlaylistFragment extends Fragment implements PlaylistRecyclerAdapter.IMediaSelector
{

    private static final String TAG = "PlaylistFragment";

    // UI Components
    private RecyclerView mRecyclerView;


    // Vars
    private PlaylistRecyclerAdapter mAdapter;
    private ArrayList<MediaMetadataCompat> mMediaList = new ArrayList<>();
    private IMainActivity mIMainActivity;
    private String mSelectedCategory;
    private Artist mSelectedArtist;
    private MediaMetadataCompat mSelectedMedia;

    public static PlaylistFragment newInstance(String category,Artist artist) {
        PlaylistFragment playlistFragment = new PlaylistFragment();
        Bundle args = new Bundle();
        args.putString("category",category);
        args.putParcelable("artist",artist);
        playlistFragment.setArguments(args);
        return playlistFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            mSelectedCategory = getArguments().getString("category");
            mSelectedArtist = getArguments().getParcelable("artist");
        }
        setRetainInstance(true);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if(!hidden)
            mIMainActivity.setActionBarTitle(mSelectedArtist.getTitle());
    }

    @Override
    public void onViewCreated(@NonNull  View view, @Nullable  Bundle savedInstanceState) {
        initRecyclerView(view);
        mIMainActivity.setActionBarTitle(mSelectedArtist.getTitle());
        if (savedInstanceState != null) {
            mAdapter.setSelectedIndex(savedInstanceState.getInt("selected_index"));
        }
    }

    private void getSelectedMediaItem(String mediaId) {
        for (MediaMetadataCompat mediaItem: mMediaList) {
            if (mediaItem.getDescription().getMediaId().equals(mediaId)) {
                mSelectedMedia = mediaItem;
                mAdapter.setSelectedIndex(mAdapter.getIndexOfItem(mSelectedMedia));
                break;
            }
        }
    }

    private void retrieveMedia(){
        mIMainActivity.showProgressBar();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        Query query = firestore
                .collection(getString(R.string.collection_audio))
                .document(getString(R.string.document_categories))
                .collection(mSelectedCategory)
                .document(mSelectedArtist.getArtist_id())
                .collection(getString(R.string.collection_content))
                .orderBy(getString(R.string.field_date_added), Query.Direction.ASCENDING);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    for(QueryDocumentSnapshot document : task.getResult()){
                        addToMediaList(document);
                    }
                }else {
                    Log.d(TAG, "onComplete: error getting documents: "+task.getException());
                }
                updateDataSet();
            }
        });
    }

    // Loop to the document from firebase and then add each filed in mMediaList
    private void addToMediaList(QueryDocumentSnapshot document) {
        MediaMetadataCompat media = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID,document.getString(getString(R.string.field_media_id)))
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST,document.getString(getString(R.string.field_artist)))
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE,document.getString(getString(R.string.field_title)))
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI,document.getString(getString(R.string.field_media_url)))
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION,document.getString(getString(R.string.field_description)))
                .putString(MediaMetadataCompat.METADATA_KEY_DATE,document.getDate(getString(R.string.field_date_added)).toString())
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI,mSelectedArtist.getImage())
                .build();
        mMediaList.add(media);
    }

    private void updateDataSet() {
        mIMainActivity.hideProgressBar();
        mAdapter.notifyDataSetChanged();

        if (mIMainActivity.getMyPrefManager().getLastPlayedArtist().equals(mSelectedArtist.getArtist_id())) {
            getSelectedMediaItem(mIMainActivity.getMyPrefManager().getLastPlayedMedia());
        }
    }
    private void initRecyclerView(View view) {
        mRecyclerView = view.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new PlaylistRecyclerAdapter(mMediaList ,getActivity(),this);
        mRecyclerView.setAdapter(mAdapter);

        if(mMediaList.size() == 0){
            retrieveMedia();
        }

    }

    @Override
    public void onAttach(@NonNull  Context context) {
        super.onAttach(context);
        mIMainActivity = ((IMainActivity)getActivity());
    }

    @Override
    public void onMediaSelected(int position) {
        mIMainActivity.getMyApplication().setMediaItems(mMediaList);
        mSelectedMedia = mMediaList.get(position);
        mAdapter.setSelectedIndex(position);
        mIMainActivity.onMediaSelected(mSelectedArtist.getArtist_id(),mMediaList.get(position),position);
        saveLastSongProperties();
    }

    private void saveLastSongProperties() {
        mIMainActivity.getMyPrefManager().savePlaylistId(mSelectedArtist.getArtist_id());
        mIMainActivity.getMyPrefManager().saveLastPlayedArtist(mSelectedArtist.getArtist_id());
        Log.d(TAG, "saveLastSongProperties: save lastplayed category : "+mSelectedCategory);
        mIMainActivity.getMyPrefManager().saveLastPlayedCategory(mSelectedCategory);
        mIMainActivity.getMyPrefManager().saveLastPlayedArtistImage(mSelectedArtist.getImage());
        mIMainActivity.getMyPrefManager().saveLastPlayedMedia(mSelectedMedia.getDescription().getMediaId());
    }

    public void updateUI(MediaMetadataCompat mediaItem) {
        mAdapter.setSelectedIndex(mAdapter.getIndexOfItem(mediaItem));
        mSelectedMedia = mediaItem;
        saveLastSongProperties();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("selected_index",mAdapter.getSelectedIndex());
    }
}















