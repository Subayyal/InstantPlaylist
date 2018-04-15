package com.example.subayyal.instantplaylist;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.github.pedrovgs.DraggableListener;
import com.github.pedrovgs.DraggablePanel;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.google.api.services.youtube.model.SearchResult;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    PlaylistFragment playlistFragment = new PlaylistFragment();
    Repository repository;

    YouTubePlayerSupportFragment youtubeFragment;
    YouTubePlayer youtubePlayer;
    DraggablePanel draggablePanel;

    RecyclerView searchRecyclerView;
    SearchListAdapter searchListAdapter;
    RecyclerView.LayoutManager layoutManager;
    List<SearchResult> searchResults;

    private boolean draggableInitialized;
    private String curVideoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        repository = new Repository(this);

        draggablePanel = findViewById(R.id.draggable_panel_view);
        setDraggableInitialized(false);

        searchRecyclerView = findViewById(R.id.my_recycler_view);
        searchResults = new ArrayList<>(0);
        searchListAdapter = new SearchListAdapter(searchResults, this, new SearchListAdapter.SearchListAdapterListener() {
            @Override
            public void onAddToQueue(SearchResult result) {
                playlistFragment.addToQueue(result);
                Snackbar.make(findViewById(R.id.activity_main), "Added to queue", Snackbar.LENGTH_SHORT).show();
            }

            @Override
            public void onPlayVideo(SearchResult result) {
                playSelectedVideo(result);
            }
        });
        layoutManager = new LinearLayoutManager(this);
        searchRecyclerView.setLayoutManager(layoutManager);
        searchRecyclerView.setAdapter(searchListAdapter);
        hookDraggablePanelListeners();

    }


    private void initializeYoutubeFragment(final String videoId) {
        if (youtubeFragment == null) {
            youtubeFragment = new YouTubePlayerSupportFragment();
            youtubeFragment.initialize(Constants.apiKey, new YouTubePlayer.OnInitializedListener() {

                @Override
                public void onInitializationSuccess(YouTubePlayer.Provider provider,
                                                    YouTubePlayer player, boolean wasRestored) {
                    if (!wasRestored) {
                        youtubePlayer = player;
                        youtubePlayer.loadVideo(videoId);
                        youtubePlayer.setShowFullscreenButton(false);
                    }
                    setupYouTubePlayerListeners(youtubePlayer);
                }

                @Override
                public void onInitializationFailure(YouTubePlayer.Provider provider,
                                                    YouTubeInitializationResult error) {
                }
            });
        } else {
            youtubePlayer.loadVideo(videoId);
            youtubePlayer.setShowFullscreenButton(false);
        }
    }

    public void setupYouTubePlayerListeners(YouTubePlayer player) {
        player.setPlayerStateChangeListener(new YouTubePlayer.PlayerStateChangeListener() {
            @Override
            public void onLoading() {

            }

            @Override
            public void onLoaded(String s) {
                Log.d("test", "onLoaded " + s);
                playlistFragment.setCurrentPlaying(s);
            }

            @Override
            public void onAdStarted() {

            }

            @Override
            public void onVideoStarted() {

            }

            @Override
            public void onVideoEnded() {
                Log.d("TEST", "onVideoEnded()");
                youtubePlayer.loadVideo(playlistFragment.getNext());
            }

            @Override
            public void onError(YouTubePlayer.ErrorReason errorReason) {
            }
        });
    }

    public void playSelectedVideo(final SearchResult result) {
        if (draggablePanel != null && isDraggableInitialized()) {
            draggablePanel.maximize();
            initializeYoutubeFragment(result.getId().getVideoId());
            playlistFragment.newQueue(result);
        } else {
            initializeYoutubeFragment(result.getId().getVideoId());
            initializeDraggablePanel();
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    playlistFragment.newQueue(result);
                }
            }, 1000);

        }
    }

    public void playSelectedVideoFromPlaylist(final SearchResult result) {
        youtubePlayer.loadVideo(result.getId().getVideoId());
        youtubePlayer.setShowFullscreenButton(false);
    }

    private void initializeDraggablePanel() {
        draggablePanel.setFragmentManager(getSupportFragmentManager());
        draggablePanel.setTopFragment(youtubeFragment);
        draggablePanel.setTopViewHeight(500);
        draggablePanel.setBottomFragment(playlistFragment);
        draggablePanel.initializeView();
        setDraggableInitialized(true);
    }

    private void hookDraggablePanelListeners() {
        draggablePanel.setDraggableListener(new DraggableListener() {
            @Override
            public void onMaximized() {
                playVideo();
            }

            @Override
            public void onMinimized() {
                //Empty
            }

            @Override
            public void onClosedToLeft() {
                pauseVideo();
            }

            @Override
            public void onClosedToRight() {
                pauseVideo();
            }
        });
    }

    /**
     * Pause the video reproduced in the YouTubePlayer.
     */
    private void pauseVideo() {
        if (youtubePlayer.isPlaying()) {
            youtubePlayer.pause();
        }
    }

    /**
     * Resume the video reproduced in the YouTubePlayer.
     */
    private void playVideo() {
        if (!youtubePlayer.isPlaying()) {
            youtubePlayer.play();
        }
    }

    public String getCurVideoId() {
        return curVideoId;
    }

    public void setCurVideoId(String curVideoId) {
        this.curVideoId = curVideoId;
    }

    public boolean isDraggableInitialized() {
        return draggableInitialized;
    }

    public void setDraggableInitialized(boolean draggableInitialized) {
        this.draggableInitialized = draggableInitialized;
    }

    public void search() {
        repository.search()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<SearchResult>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onSuccess(List<SearchResult> results) {
                        searchListAdapter.setData(results);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.top_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.search:
                search();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
