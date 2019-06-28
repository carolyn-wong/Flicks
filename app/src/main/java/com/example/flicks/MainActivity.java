package com.example.flicks;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.example.flicks.models.Config;
import com.example.flicks.models.Movie;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    // constants
    // base URL for API
    public final static String API_BASE_URL = "https://api.themoviedb.org/3";
    // parameter name for API key
    public final static String API_KEY_PARAM = "api_key";
    // tag for logging from this activity
    public final static String TAG = "MovieListActivity";

    // instance values - associated with specific instance of movie list activity
    AsyncHttpClient client;
    // list of currently playing movies
    ArrayList<Movie> movies;
    // recycler view
    RecyclerView rvMovies;
    // adapter wired to recycler view
    MovieAdapter adapter;
    // track config model object
    Config config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // initialize the client
        client = new AsyncHttpClient();
        // initialize before first network call since want all objects to be created first
        movies = new ArrayList<>();
        // initialize adapter after creation of ArrayList
        adapter = new MovieAdapter(movies);
        // resolve reference to recycler view from layout, connect layout manager/adapter
        rvMovies = (RecyclerView) findViewById(R.id.rvMovies);
        rvMovies.setLayoutManager(new LinearLayoutManager(this));
        rvMovies.setAdapter(adapter);

        // get configuration on app creation
        getConfiguration();
    }

    // network call that gets list of currently playing movies from API
    private void getNowPlaying() {
        // create URL
        String url = API_BASE_URL + "/movie/now_playing";
        // set request parameters (appended to URL)
        RequestParams params = new RequestParams();
        params.put(API_KEY_PARAM, getString(R.string.api_key)); // API key, always required
        // GET request expecting JSON object response
        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // parse JSON response, populate list of movies with movies created from each entry in result
                // parse results value as JSON array
                try {
                    JSONArray results = response.getJSONArray("results");
                    // iterate through movie array, pass every object to movie constructor
                    // add new movie object to list
                    for (int i = 0; i < results.length(); i++) {
                        Movie movie = new Movie(results.getJSONObject(i));
                        movies.add(movie);
                        // notify adapter that row was added
                        adapter.notifyItemInserted(movies.size() - 1);
                    }
                    Log.i(TAG, String.format("Loaded %s movies", results.length()));
                } catch (JSONException e) {
                    logError("Failed to parse now playing movies", e, true);
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                logError("Failed to get data from now playing endpoint", throwable, true);
            }
        });
    }

    // get configuration from API
    private void getConfiguration() {
        // create URL
        String url = API_BASE_URL + "/configuration";
        // set request parameters (appended to URL)
        RequestParams params = new RequestParams();
        params.put(API_KEY_PARAM, getString(R.string.api_key)); // API key, always required
        // GET request expecting JSON object response
        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    config = new Config(response);
                    Log.i(TAG, String.format("Loaded configuration with imageBaseUrl %s and posterSize %s",
                                        config.getImageBaseUrl(),
                                        config.getPosterSize()));
                    // pass config object to adapter
                    adapter.setConfig(config);
                    // get now playing movie list, move to the end of getConfiguration so configuration completes first
                    getNowPlaying();
                } catch (JSONException e) {
                    logError("Failed parsing configuration", e, true);
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                logError("Failed getting configuration", throwable, true);
            }
        });
    }

    // handle errors, log and alert user of silent failures (if indicated)
    // Throwable - base class of all errors/exceptions in Java
    private void logError(String message, Throwable error, boolean alertUser) {
        // always log error
        Log.e(TAG, message, error);
        // optionally fail non-silently - alert user
        if (alertUser) {
            // show long toast with error message
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        }
    }
}
