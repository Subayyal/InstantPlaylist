package com.example.subayyal.instantplaylist;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import com.google.api.services.youtube.model.SearchResult;

import org.ocpsoft.prettytime.PrettyTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by subayyal on 4/11/2018.
 */

public class SearchListAdapter extends RecyclerView.Adapter<ViewHolder> {


    private List<SearchResult> searchResults;
    private Context context;
    public SearchListAdapterListener listAdapterListener;

    public SearchListAdapter(List<SearchResult> searchResults, Context context, SearchListAdapterListener listAdapterListener ) {
        this.searchResults = searchResults;
        this.context = context;
        this.listAdapterListener = listAdapterListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_list_item_layout, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.setListener(new ViewHolder.OnClickListener() {
            @Override
            public void onOptionsClick() {
                Log.d("Test", " onOptionsClick pos:"+holder.getAdapterPosition());
                showMenu(holder.mOptions, holder.getAdapterPosition());
            }

            @Override
            public void onViewClick() {
                listAdapterListener.onPlayVideo(searchResults.get(holder.getAdapterPosition()));
            }
        });

        holder.mVideoDetail.setText(searchResults.get(position).getSnippet().getChannelTitle()+ " " +"\u00B7"+" " +
        formatTime(searchResults.get(position).getSnippet().getPublishedAt().toString()));

        holder.mVideoTitle.setText(searchResults.get(position).getSnippet().getTitle());

        Repository.loadBitmap(searchResults.get(position).getSnippet().getThumbnails().getMedium().getUrl())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Bitmap>() {
                    @Override
                    public void onSubscribe(Disposable d) {}

                    @Override
                    public void onNext(Bitmap bitmap) {
                        holder.mImageView.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("Test", "Unable to set bitmap!");
                    }

                    @Override
                    public void onComplete() {}
                });
    }

    private void showMenu(final View view, final int position) {
        PopupMenu menu = new PopupMenu(context,view);
        menu.inflate(R.menu.add_to_queue);
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.add_to_queue:
                        listAdapterListener.onAddToQueue(searchResults.get(position));
                        return true;
                    default:
                        return false;
                }
            }
        });
        menu.show();
    }

    private String formatTime(String time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date date = null;

        try {
            date = dateFormat.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        PrettyTime p = new PrettyTime();
        return p.format(date);
    }

    public void setData(List<SearchResult> list) {
        this.searchResults = list;
        notifyDataSetChanged();
    }

    public void nextPage(List<SearchResult> searchResults) {
        this.searchResults.addAll(searchResults);
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return searchResults.size();
    }

    public interface SearchListAdapterListener {
        void onAddToQueue(SearchResult result);

        void onPlayVideo(SearchResult result);
    }

}
