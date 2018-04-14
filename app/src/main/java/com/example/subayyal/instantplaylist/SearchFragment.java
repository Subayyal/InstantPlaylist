package com.example.subayyal.instantplaylist;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.api.services.youtube.model.SearchResult;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by subayyal on 4/11/2018.
 */

public class SearchFragment extends Fragment {
    Repository repository;
    RecyclerView recyclerView;
    SearchListAdapter adapter;
    RecyclerView.LayoutManager layoutManager;
    List<SearchResult> searchResults;
    private OnAddedToQueueListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (OnAddedToQueueListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnArticleSelectedListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.search_list, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = new Repository(getActivity());
        searchResults = new ArrayList<>();
        adapter = new SearchListAdapter(searchResults, getContext(), new SearchListAdapter.SearchListAdapterListener() {
            @Override
            public void onAddToQueue(SearchResult result) {
                listener.onAddedToQueue(result);
            }
        });

    }

    public void search(){
        repository.search()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<SearchResult>>() {
                    @Override
                    public void onSubscribe(Disposable d) {}

                    @Override
                    public void onSuccess(List<SearchResult> results) {
                        searchResults = results;
                        adapter = new SearchListAdapter(searchResults, getContext(), new SearchListAdapter.SearchListAdapterListener() {
                            @Override
                            public void onAddToQueue(SearchResult result) {
                                listener.onAddedToQueue(result);
                            }
                        });
                        recyclerView.setAdapter(adapter);
                        Toast.makeText(getActivity(),  "Completed", Toast.LENGTH_LONG);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(getActivity(),  "Error", Toast.LENGTH_LONG);
                    }
                });
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView = getView().findViewById(R.id.my_recycler_view);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);


    }


}
