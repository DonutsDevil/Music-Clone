package com.example.music_clone.adapters;

import android.content.Context;
import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_clone.R;

import java.util.ArrayList;

public class PlaylistRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "PlaylistRecyclerAdapter";

    private ArrayList<MediaMetadataCompat> mMediaList = new ArrayList<>();
    private Context mContext;
    private IMediaSelector mIMediaSelector;
    private int mSelectedIndex;

    public PlaylistRecyclerAdapter(ArrayList<MediaMetadataCompat> mMediaList, Context mContext, IMediaSelector mIMediaSelector) {
        Log.d(TAG, "PlaylistRecyclerAdapter: called");
        this.mMediaList = mMediaList;
        this.mContext = mContext;
        this.mIMediaSelector = mIMediaSelector;
        mSelectedIndex = -1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_playlist_list_item,parent,false);
        return new ViewHolder(view,mIMediaSelector);
    }

    @Override
    public void onBindViewHolder(@NonNull  RecyclerView.ViewHolder holder, int position) {
        ((ViewHolder)holder).title.setText(mMediaList.get(position).getDescription().getTitle());
        ((ViewHolder)holder).artist.setText(mMediaList.get(position).getDescription().getSubtitle());

        if(position == mSelectedIndex) {
            ((ViewHolder)holder).title.setTextColor(ContextCompat.getColor(mContext,R.color.green));
        }else {
            ((ViewHolder)holder).title.setTextColor(ContextCompat.getColor(mContext,R.color.white));
        }
    }

    @Override
    public int getItemCount() {
        return mMediaList.size();
    }

    public void setSelectedIndex(int index) {
        mSelectedIndex = index;
        notifyDataSetChanged();
    }

    public int getIndexOfItem(MediaMetadataCompat mediaItem ) {
        for(int i = 0; i < mMediaList.size();i++){
            if(mMediaList.get(i).getDescription().getMediaId()
                    .equals(mediaItem.getDescription().getMediaId())) {
                return i;
            }
        }
        return -1;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView title,artist;
        private IMediaSelector iMediaSelector;
        public ViewHolder(@NonNull View itemView,IMediaSelector iMediaSelector) {
            super(itemView);
            this.iMediaSelector = iMediaSelector;
            title = itemView.findViewById(R.id.media_title);
            artist = itemView.findViewById(R.id.media_artist);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            iMediaSelector.onMediaSelected(getAdapterPosition());
        }
    }
    public interface IMediaSelector{
        void onMediaSelected(int position);
    }
}
