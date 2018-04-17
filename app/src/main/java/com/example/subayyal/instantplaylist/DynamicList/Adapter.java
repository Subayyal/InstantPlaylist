package com.example.subayyal.instantplaylist.DynamicList;

/**
 * Created by subayyal on 4/12/2018.
 */

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.subayyal.instantplaylist.R;
import com.google.api.services.youtube.model.SearchResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Adapter extends ArrayAdapter<SearchResult> {

    int currentPLaying;

    List<SearchResult> objects;

    PlaylistListener listener;

    public Adapter(Context context, List<SearchResult> objects, PlaylistListener listener) {
        super(context, 0, objects);
        this.objects = objects;
        this.listener = listener;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View view, @NonNull ViewGroup parent) {
        Context context = getContext();
        if (null == view) {
            view = LayoutInflater.from(context).inflate(R.layout.playlist_item_layout
                    , null);
        }

        TextView playlist_item_title = view.findViewById(R.id.playlist_item_title);
        ImageView playlist_item_play = view.findViewById(R.id.playlist_item_play);
        ImageView playlist_item_remove = view.findViewById(R.id.playlist_item_delete);

        playlist_item_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                remove(getItem(position));
            }
        });
        playlist_item_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onPlayFromPlaylist(getItem(position));
            }
        });
        playlist_item_title.setText(getItem(position).getSnippet().getTitle());
        if (currentPLaying == position) {
            playlist_item_play.setVisibility(View.VISIBLE);
        } else {
            playlist_item_play.setVisibility(View.INVISIBLE);
        }

        return view;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    public void setCurrentPLaying(String videoId) {
        for (int i = 0; i < objects.size(); i++) {
            if (objects.get(i).getId().getVideoId().equals(videoId)) {
                currentPLaying = i;
                notifyDataSetChanged();
            }
        }
    }

    public String getNext() {
        if (currentPLaying < objects.size() - 1) {
            return objects.get(currentPLaying + 1).getId().getVideoId();
        }
        return null;
    }

    @Override
    public void add(@Nullable SearchResult object) {
        for (int i = 0; i < objects.size(); i++) {
            if (objects.get(i).equals(object)) {
                return;
            }
        }
        super.add(object);
    }

    @Override
    public void remove(@Nullable SearchResult object) {

        super.remove(object);
    }

    public interface PlaylistListener{
        void onPlayFromPlaylist(SearchResult result);
    }

}