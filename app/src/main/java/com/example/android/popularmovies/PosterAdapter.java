package com.example.android.popularmovies;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Joel on 2/21/2017.
 */

public class PosterAdapter extends BaseAdapter {

    private final String LOG_TAG = PosterAdapter.class.getSimpleName();

    private Context mContext;
    private ArrayList<String> mPosterPaths;

    public PosterAdapter(Context c, ArrayList<String> p) {
        mContext = c;
        mPosterPaths = p;
    }

    public void setPaths(ArrayList<String> p) {
        mPosterPaths = p;
    }

    @Override
    public int getCount() {
        if (mPosterPaths != null)
            return mPosterPaths.size();
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imgView;
        if (convertView == null) {
            imgView = new ImageView(mContext);
            imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            imgView = (ImageView) convertView;
        }
        final String BASE_URL = "http://image.tmdb.org/t/p/";
        final String IMG_SIZE = "w185";
        Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                .appendPath(IMG_SIZE)
                .appendEncodedPath(mPosterPaths.get(position))
                .build();

        Picasso.with(mContext).load(builtUri.toString()).into(imgView);
        return imgView;
    }

}