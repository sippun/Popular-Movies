package com.example.android.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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

import static com.example.android.popularmovies.BuildConfig.TMD_API_KEY;

/**
 * Created by Joel on 2/21/2017.
 */

public class MovieFragment extends Fragment {

    private GridView gridView;
    private MovieAdapter mMovieAdapter;
    private ArrayList<MovieParcel> movieList;

    public MovieFragment() {
    }

    @Override
    public void onStart() {
        new FetchPosterTask().execute();
        setHasOptionsMenu(true);
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
        gridView = (GridView) rootView.findViewById(R.id.gridview_poster);

        movieList = new ArrayList<MovieParcel>();
        mMovieAdapter = new MovieAdapter(getActivity(), movieList);
        gridView.setAdapter(mMovieAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                String movieID = mMovieAdapter.getItem(position).movieID;
                Intent detailIntent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, movieID);
                startActivity(detailIntent);
            }
        });

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.moviefragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            refreshPosters();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshPosters() {
        new FetchPosterTask().execute();
        mMovieAdapter.notifyDataSetChanged();
    }

    private class FetchPosterTask extends AsyncTask<Void, Void, Void> {

        private final String LOG_TAG = FetchPosterTask.class.getSimpleName();

        @Override
        protected Void doInBackground(Void... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String movieListJsonStr = null;

            // Query strings
            String lang = "en-US";
            int numPages = 1;

            // Get sort order from settings
            SharedPreferences mySettings = PreferenceManager
                    .getDefaultSharedPreferences(getActivity());
            String order = mySettings.getString(
                    getResources().getString(R.string.pref_sort_key),
                    getString(R.string.pref_sort_popular));
            try {
                String BASE_URL;
                if (order.equals(getString(R.string.pref_sort_popular))) {
                    BASE_URL = "https://api.themoviedb.org/3/movie/popular?";
                } else {
                    BASE_URL = "https://api.themoviedb.org/3/movie/top_rated?";
                }
                final String APPID_PARAM = "api_key";
                final String LANG_PARAM = "language";
                final String PAGE_PARAM = "page";

                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(APPID_PARAM, TMD_API_KEY)
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
                movieList = getMovieListFromJson(movieListJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error ", e);
            }
            return null;
        }

        private ArrayList<MovieParcel> getMovieListFromJson(String JsonStr) throws JSONException {
            final String TMD_RESULTS = "results";
            final String TMD_ID = "id";
            final String TMD_POSTER = "poster_path";

            JSONObject movieJson = new JSONObject(JsonStr);
            JSONArray movieArray = movieJson.getJSONArray(TMD_RESULTS);

            ArrayList<MovieParcel> movieList = new ArrayList();
            for (int i = 0; i < movieArray.length(); i++) {
                MovieParcel movie = new MovieParcel(
                        movieArray.getJSONObject(i).getString(TMD_ID),
                        movieArray.getJSONObject(i).getString(TMD_POSTER));
                movieList.add(movie);
            }
            return movieList;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mMovieAdapter.setList(movieList);
            mMovieAdapter.notifyDataSetChanged();
            super.onPostExecute(aVoid);
        }
    }
}


