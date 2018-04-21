package com.example.subayyal.instantplaylist;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.api.services.youtube.model.SearchResult;

import java.util.ArrayList;

/**
 * Created by subayyal on 4/11/2018.
 */

public class PlaylistFragment extends Fragment {

    private ArrayList<SearchResult> list;
    private PlaylistAdapter playlistAdapter;
    private ListView listView;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.video_display, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        list = new ArrayList<>();
        playlistAdapter = new PlaylistAdapter(getContext(), list, new PlaylistAdapter.PlaylistListener() {
            @Override
            public void onPlayFromPlaylist(SearchResult result) {
                ((MainActivity)getActivity()).playSelectedVideoFromPlaylist(result);
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        listView = getView().findViewById(R.id.playlist_listview);
        listView.setAdapter(playlistAdapter);


    }

    public String getNext(){
        return playlistAdapter.getNext();
    }


    public void setCurrentPlaying(String videoId) {
        playlistAdapter.setCurrentPLaying(videoId);
    }

    public void addToQueue(SearchResult result) {
        Log.d("Test", "addToQueue called");
        playlistAdapter.add(result);
    }

    public void newQueue(SearchResult result) {
        playlistAdapter.clear();
        playlistAdapter.add(result);
    }

}
