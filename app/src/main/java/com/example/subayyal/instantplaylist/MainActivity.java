package com.example.subayyal.instantplaylist;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.PopupWindow;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.api.services.youtube.model.SearchResult;

public class MainActivity extends AppCompatActivity implements OnAddedToQueueListener{

    private FragmentTransaction transaction;

    SearchFragment searchFragment = new SearchFragment();
    VideoFragment videoFragment = new VideoFragment();

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_container, searchFragment, "SearchFragment");
                    transaction.addToBackStack(null);
                    transaction.commit();
                    return true;
                case R.id.navigation_dashboard:
                    transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_container, videoFragment, "VideoFragment");
                    transaction.addToBackStack(null);
                    transaction.commit();
                    return true;
                case R.id.navigation_notifications:
                    //mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);


    }

    public void search(){
        SearchFragment searchFragment = (SearchFragment) getSupportFragmentManager().findFragmentByTag("SearchFragment");
        searchFragment.search();
    }

    @Override
    public void onAddedToQueue(SearchResult result) {
        VideoFragment videoFragment = (VideoFragment) getSupportFragmentManager().findFragmentByTag("VideoFragment");
        videoFragment.addToQueue(result);
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
