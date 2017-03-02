package com.example.android.popularmovies;

import android.app.Activity;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Joel on 2/21/2017.
 */

public class MovieAdapter extends ArrayAdapter<MovieParcel> {

    private final String LOG_TAG = MovieAdapter.class.getSimpleName();

    public MovieAdapter(Activity context, List<MovieParcel> movies) {
        super(context, 0, movies);
    }

    public void setList(List<MovieParcel> movies) {
        this.clear();
        this.addAll(movies);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imgView;
        MovieParcel movie = getItem(position);

        if (convertView == null) {
            imgView = new ImageView(this.getContext());
            imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            imgView = (ImageView) convertView;
        }
        final String BASE_URL = "http://image.tmdb.org/t/p/";
        final String IMG_SIZE = "w185";
        Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                .appendPath(IMG_SIZE)
                .appendEncodedPath(movie.posterPath)
                .build();

        Picasso.with(this.getContext()).load(builtUri.toString()).into(imgView);
        return imgView;
    }

}