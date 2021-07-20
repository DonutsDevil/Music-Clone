package com.example.music_clone.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.music_clone.R;

import java.util.ArrayList;

public class HomeRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final ArrayList<String> mCategories;
    private final Context mContext;
    private final IHomeSelector mIHomeSelector;

    public HomeRecyclerAdapter(ArrayList<String> mCategories, Context mContext, IHomeSelector mIHomeSelector) {
        this.mCategories = mCategories;
        this.mContext = mContext;
        this.mIHomeSelector = mIHomeSelector;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull  ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_category_list_item,parent,false);
        return new ViewHolder(view, mIHomeSelector);
    }

    @Override
    public void onBindViewHolder(@NonNull  RecyclerView.ViewHolder holder, int position) {
        ((ViewHolder)holder).category.setText(mCategories.get(position));
        RequestOptions requestOptions = new RequestOptions()
                .error(R.drawable.ic_launcher_background);

        Drawable iconResource = null;
        switch (mCategories.get(position)) {
            case "Music" : {
                iconResource = ContextCompat.getDrawable(mContext,R.drawable.ic_audiotrack_white_24dp);
                break;
            }
            case "Podcasts" : {
                iconResource = ContextCompat.getDrawable(mContext,R.drawable.ic_mic_white_24dp);
                break;
            }
        }

        Glide.with(mContext)
                .setDefaultRequestOptions(requestOptions)
                .load(iconResource)
                .into(((ViewHolder)holder).categoryIcon);
    }

    @Override
    public int getItemCount() {
        return mCategories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView category;
        private final ImageView categoryIcon;
        private final IHomeSelector iHomeSelector;
        public ViewHolder(@NonNull View itemView, IHomeSelector iHomeSelector) {
            super(itemView);
            category = itemView.findViewById(R.id.category_title);
            categoryIcon = itemView.findViewById(R.id.category_icon);
            itemView.setOnClickListener(this);
            this.iHomeSelector = iHomeSelector;
        }

        @Override
        public void onClick(View v) {
            iHomeSelector.onCategorySelected(getAdapterPosition());
        }
    }

    public interface IHomeSelector{
        void onCategorySelected(int position);
    }
}
