package com.example.music_clone.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.music_clone.R;
import com.example.music_clone.models.Artist;

import java.util.ArrayList;

public class CategoryRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final  ArrayList<Artist> mArtist;
    private final Context mContext;
    private final ICategorySelector mICategorySelector;

    public CategoryRecyclerAdapter(ArrayList<Artist> mArtist, Context mContext, ICategorySelector mICategorySelector) {
        this.mArtist = mArtist;
        this.mContext = mContext;
        this.mICategorySelector = mICategorySelector;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_artist_list_item,parent,false);
        return new ViewHolder(view,mICategorySelector);
    }

    @Override
    public void onBindViewHolder(@NonNull  RecyclerView.ViewHolder holder, int position) {
        ((ViewHolder)holder).title.setText(mArtist.get(position).getTitle());
        RequestOptions options = new RequestOptions()
                .error(R.drawable.ic_launcher_background);
        Glide.with(mContext)
                .setDefaultRequestOptions(options)
                .load(mArtist.get(position).getImage())
                .into(((ViewHolder)holder).image);
    }

    @Override
    public int getItemCount() {
        return mArtist.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView title;
        private ImageView image;
        private ICategorySelector iCategorySelector;
        public ViewHolder(@NonNull View itemView, ICategorySelector iCategorySelector) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            image = itemView.findViewById(R.id.image);
            this.iCategorySelector = iCategorySelector;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            iCategorySelector.onArtistSelected(getAdapterPosition());
        }
    }

    public interface ICategorySelector {
        void onArtistSelected(int position);
    }
}
