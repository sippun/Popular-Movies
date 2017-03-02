package com.example.android.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Joel on 3/2/2017.
 */

public class MovieParcel implements Parcelable {
    String movieID;
    String posterPath;

    public MovieParcel(String id, String path) {
        this.movieID = id;
        this.posterPath = path;
    }

    private MovieParcel(Parcel in) {
        movieID = in.readString();
        posterPath = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String toString() {
        return movieID + "--" + posterPath;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(movieID);
        dest.writeString(posterPath);
    }

    public final Parcelable.Creator<MovieParcel> CREATOR = new Parcelable.Creator<MovieParcel>() {

        @Override
        public MovieParcel createFromParcel(Parcel source) {
            return new MovieParcel(source);
        }

        @Override
        public MovieParcel[] newArray(int i) {
            return new MovieParcel[i];
        }
    };
}
