package com.example.android.popularmovies;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Joel on 2/21/2017.
 */

public class MovieFragment extends Fragment {
    private ArrayList<String> mPosterPaths;

    public MovieFragment() {
    }

    @Override
    public void onStart() {
        mPosterPaths = new ArrayList<>();
        new FetchPosterTask().execute();
        super.onStart();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        GridView gridView = (GridView) rootView.findViewById(R.id.gridview_poster);
        gridView.setAdapter(new PosterAdapter(getActivity(), mPosterPaths));
        return rootView;
    }

    private class FetchPosterTask extends AsyncTask<Void, Void, ArrayList<String>> {

        private final String LOG_TAG = FetchPosterTask.class.getSimpleName();

        @Override
        protected ArrayList<String> doInBackground(Void... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String movieListJsonStr = null;

            String lang = "en-US";
            int numPages = 1;

            try {
                final String BASE_URL =
                    "https://api.themoviedb.org/3/movie/popular?";
                final String APPID_PARAM = "api_key";
                final String LANG_PARAM = "language";
                final String PAGE_PARAM = "page";

                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(APPID_PARAM, "***REMOVED***")
                        .appendQueryParameter(LANG_PARAM, lang)
                        .appendQueryParameter(PAGE_PARAM, Integer.toString(numPages))
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
                movieListJsonStr = buffer.toString();
            } catch(IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the movie data, there's no point in attemping
                // to parse it.
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
                return getMovieListFromJson(movieListJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            }
        }

        private ArrayList<String> getMovieListFromJson(String JsonStr) throws JSONException {
            final String TMD_RESULTS = "results";
            final String TMD_POSTER = "poster_path";

            JSONObject movieJson = new JSONObject(JsonStr);
            JSONArray movieArray = movieJson.getJSONArray(TMD_RESULTS);

            ArrayList<String> posterPaths = new ArrayList();
            for (int i = 0; i < movieArray.length(); i++) {
                posterPaths.add(movieArray.getJSONObject(i).getString(TMD_POSTER));
            }
            return posterPaths;
        }

        @Override
        protected void onPostExecute(ArrayList<String> strings) {
            mPosterPaths.addAll(strings);
            super.onPostExecute(strings);
        }
    }
}


