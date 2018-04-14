package com.example.subayyal.instantplaylist;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.subayyal.instantplaylist.DynamicList.Adapter;
import com.example.subayyal.instantplaylist.DynamicList.CustomListView;
import com.google.api.services.youtube.model.SearchResult;

import java.util.ArrayList;

/**
 * Created by subayyal on 4/11/2018.
 */

public class VideoFragment extends Fragment {

    private ArrayList<SearchResult> list;
    private Adapter adapter;
    CustomListView listView;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.video_display, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        list = new ArrayList<>();

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.d("Test", "onActivityCreated");
        listView = getView().findViewById(R.id.playlist_listview);
        listView.setData(this.list);
        adapter = new Adapter(getContext(),list);
        listView.setAdapter(adapter);


    }



    public void addToQueue(SearchResult result) {
        Log.d("Test", "addToQueue called");
        adapter.add(result);
    }

}