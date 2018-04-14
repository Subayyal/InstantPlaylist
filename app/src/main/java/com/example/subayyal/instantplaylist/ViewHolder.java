package com.example.subayyal.instantplaylist;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by subayyal on 4/13/2018.
 */

public class ViewHolder extends RecyclerView.ViewHolder {
    public TextView mVideoTitle;
    public TextView mVideoDetail;
    public ImageView mImageView;
    public ImageView mOptions;
    public OnOptionsClickListener listener;

    public ViewHolder(View v) {
        super(v);
        mVideoDetail = v.findViewById(R.id.video_detail);
        mVideoTitle = v.findViewById(R.id.video_title);
        mImageView = v.findViewById(R.id.video_thumbnail);
        mOptions = v.findViewById(R.id.video_options);
        mOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Test", "onClick");
                listener.onOptionsClick();
            }
        });
    }

    public void setListener(OnOptionsClickListener listener) {
        this.listener = listener;
    }

    public interface OnOptionsClickListener{
        void onOptionsClick();
    }

}
