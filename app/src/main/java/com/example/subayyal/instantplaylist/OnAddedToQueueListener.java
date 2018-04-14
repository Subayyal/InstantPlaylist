package com.example.subayyal.instantplaylist;

import com.google.api.services.youtube.model.SearchResult;

/**
 * Created by subayyal on 4/13/2018.
 */

public interface OnAddedToQueueListener {
    void onAddedToQueue(SearchResult result);
}
