package com.example.android.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.example.android.popularmovies.BuildConfig.TMD_API_KEY;

/**
 * Created by Joel on 3/3/2017.
 */

public class DetailFragment extends Fragment {
        private String movieID;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

            Intent intent = getActivity().getIntent();
            if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
                movieID = intent.getStringExtra(Intent.EXTRA_TEXT);
            }

            new FetchMovieTask().execute();
            return rootView;
        }

    private class FetchMovieTask extends AsyncTask<Void, Void, String[]> {

        private final String LOG_TAG = DetailFragment.FetchMovieTask.class.getSimpleName();

        @Override
        protected String[] doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String detailJsonStr = null;

            // Query strings
            String lang = "en-US";

            try {
                final String BASE_URL = "https://api.themoviedb.org/3/movie/";
                final String APPID_PARAM = "api_key";
                final String LANG_PARAM = "language";

                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendPath(movieID)
                        .appendQueryParameter(APPID_PARAM, TMD_API_KEY)
                        .appendQueryParameter(LANG_PARAM, lang)
                        .build();

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                detailJsonStr = buffer.toString();
            } catch(IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            // Convert JSON string
            try {
                return getMovieDetails(detailJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error ", e);
            }
            return null;
        }

        private String[] getMovieDetails(String JsonStr) throws JSONException {
            final String DETAIL_TITLE = "original_title";
            final String DETAIL_POSTER_PATH = "poster_path";
            final String DETAIL_OVERVIEW = "overview";
            final String DETAIL_DATE = "release_date";
            final String DETAIL_RATING = "vote_average";

            JSONObject movieJson = new JSONObject(JsonStr);
            String[] movieDetails = new String[5];
            movieDetails[0] = movieJson.getString(DETAIL_TITLE);
            movieDetails[1] = movieJson.getString(DETAIL_POSTER_PATH);
            String releaseDate = movieJson.getString(DETAIL_DATE);
            // Get just the year from release date
            movieDetails[2] = releaseDate.substring(0, releaseDate.indexOf("-"));
            movieDetails[3] = movieJson.getString(DETAIL_RATING);
            movieDetails[4] = movieJson.getString(DETAIL_OVERVIEW);
            return movieDetails;
        }

        @Override
        protected void onPostExecute(String[] s) {
            ((TextView) getView().findViewById(R.id.detail_movie_title)).setText(s[0]);
            ((TextView) getView().findViewById(R.id.detail_movie_year)).setText(s[2]);
            ((TextView) getView().findViewById(R.id.detail_movie_rating)).setText(s[3]);

            final String BASE_URL = "http://image.tmdb.org/t/p/";
            final String IMG_SIZE = "w185";
            Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                    .appendPath(IMG_SIZE)
                    .appendEncodedPath(s[1])
                    .build();

            Picasso.with(getView().getContext())
                    .load(builtUri.toString())
                    .into((ImageView) getView().findViewById(R.id.detail_movie_thumb));
            super.onPostExecute(s);
        }
    }
}
