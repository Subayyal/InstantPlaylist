package com.example.subayyal.instantplaylist;

import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

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
    MenuItem searchItem;

    YouTubePlayerSupportFragment youtubeFragment;
    YouTubePlayer youtubePlayer;
    DraggablePanel draggablePanel;

    RecyclerView searchRecyclerView;
    SearchListAdapter searchListAdapter;
    LinearLayoutManager layoutManager;
    List<SearchResult> searchResults;

    private boolean draggableInitialized;

    private ConstraintLayout search_filter_layout;
    private TextView sort_by_tv;
    private Spinner sort_by_spinner;
    private TextView upload_by_tv;
    private Spinner upload_by_spinner;
    private SearchView searchView;
    private FrameLayout progressBar;
    private EndlessRecyclerViewScrollListener endlessRecyclerViewScrollListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.progress_bar_container);

        repository = new Repository(this);
        setupSearchFilter();

        draggablePanel = findViewById(R.id.draggable_panel_view);
        setDraggableInitialized(false);

        searchRecyclerView = findViewById(R.id.my_recycler_view);
        searchResults = new ArrayList<>(0);
        searchListAdapter = new SearchListAdapter(searchResults, this, new SearchListAdapter.SearchListAdapterListener() {
            @Override
            public void onAddToQueue(SearchResult result) {
                if (draggableInitialized) {
                    playlistFragment.addToQueue(result);
                } else {
                    playSelectedVideo(result);
                }
            }

            @Override
            public void onPlayVideo(SearchResult result) {
                searchItem.collapseActionView();
                playSelectedVideo(result);
            }
        });
        layoutManager = new LinearLayoutManager(this);
        searchRecyclerView.setLayoutManager(layoutManager);
        endlessRecyclerViewScrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, View view) {
                loadMore();
            }
        };
        searchRecyclerView.addOnScrollListener(endlessRecyclerViewScrollListener);
        searchRecyclerView.setAdapter(searchListAdapter);
        hookDraggablePanelListeners();

    }

    public void setupSearchFilter(){
        search_filter_layout = findViewById(R.id.search_filters_layout);
        sort_by_spinner = findViewById(R.id.sort_by_spinner);
        upload_by_spinner = findViewById(R.id.upload_date_spinner);

        ArrayAdapter<CharSequence> sortByAdapter = ArrayAdapter.createFromResource(this, R.array.sort_by, R.layout.support_simple_spinner_dropdown_item);
        sortByAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        sort_by_spinner.setAdapter(sortByAdapter);

        ArrayAdapter<CharSequence> uploadByAdapter = ArrayAdapter.createFromResource(this, R.array.upload_date, R.layout.support_simple_spinner_dropdown_item);
        uploadByAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        upload_by_spinner.setAdapter(uploadByAdapter);

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
                searchItem.setVisible(false);
                search_filter_layout.setVisibility(View.GONE);
                playVideo();
            }

            @Override
            public void onMinimized() {
                searchItem.setVisible(true);
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

    public boolean isDraggableInitialized() {
        return draggableInitialized;
    }

    public void setDraggableInitialized(boolean draggableInitialized) {
        this.draggableInitialized = draggableInitialized;
    }

    public void search(SearchObject searchObject) {
        endlessRecyclerViewScrollListener.resetState();
        repository.search(searchObject)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<SearchResult>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        progressBar.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onSuccess(List<SearchResult> results) {
                        progressBar.setVisibility(View.GONE);
                        searchListAdapter.setData(results);
                    }

                    @Override
                    public void onError(Throwable e) {
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    public void loadMore(){
        repository.nextPage()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<SearchResult>>() {
                    @Override
                    public void onSubscribe(Disposable d) {}

                    @Override
                    public void onSuccess(List<SearchResult> results) {
                        searchListAdapter.nextPage(results);
                    }

                    @Override
                    public void onError(Throwable e) {}
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.top_menu, menu);
        searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();

        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                search_filter_layout.setVisibility(View.VISIBLE);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                search_filter_layout.setVisibility(View.GONE);
                return true;
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                SearchObject searchObject = new SearchObject();
                searchObject.setQuery(query);
                searchObject.setSortBy(sort_by_spinner.getSelectedItem().toString());
                searchObject.setUploadDate(upload_by_spinner.getSelectedItem().toString());
                search(searchObject);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }
}
