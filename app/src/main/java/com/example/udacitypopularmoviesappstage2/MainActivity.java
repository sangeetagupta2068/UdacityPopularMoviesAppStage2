package com.example.udacitypopularmoviesappstage2;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.udacitypopularmoviesappstage2.data.Movie;
import com.example.udacitypopularmoviesappstage2.database.AppDatabase;
import com.example.udacitypopularmoviesappstage2.databinding.ActivityMainBinding;
import com.example.udacitypopularmoviesappstage2.utils.JSONUtils;
import com.example.udacitypopularmoviesappstage2.utils.NetworkUtils;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MovieAdapter.MovieListItemHandler {

    static public ArrayList<Movie> movies = new ArrayList<>();
    static private int POPULARITY = 1;
    static private int TOP_RATED = 0;
    static private int FAVOURITE = 2;
    static public ArrayList<Movie> favourite_movies = new ArrayList<>();
    static public AppDatabase appDatabase;

    ActivityMainBinding activityMainBinding;
    MovieAdapter movieAdapter;
    RecyclerView.LayoutManager gridLayoutManager;

    int sort_value;
    int gridCount;

    private class MovieAsyncTask extends AsyncTask<Integer, Void, Integer> {

        @Override
        protected Integer doInBackground(Integer... filter) {

            if (filter.length == 0) {
                return null;
            }

            int sortFlag = filter[0];
            URL url = NetworkUtils.buildURLForMovies(sortFlag);
            try {
                String jsonData = NetworkUtils.getResponseFromHttpUrl(url);
                JSONUtils.getMovieList(jsonData);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d("MainActivity", "This called!");
            return 0;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            MainActivity.movies.clear();
            activityMainBinding.connectionStatus.setVisibility(View.VISIBLE);
            activityMainBinding.moviePosterList.setVisibility(View.INVISIBLE);
            activityMainBinding.logView.setVisibility(View.INVISIBLE);

        }

        @Override
        protected void onPostExecute(Integer bool) {
            super.onPostExecute(bool);
            activityMainBinding.connectionStatus.setVisibility(View.INVISIBLE);
            if (MainActivity.movies.size() == 0 || MainActivity.movies == null) {
                activityMainBinding.logView.setText(getString(R.string.log_failed_data_retrieval));
                activityMainBinding.logView.setVisibility(View.VISIBLE);
                activityMainBinding.connectionStatus.setVisibility(View.INVISIBLE);
            } else {
                activityMainBinding.moviePosterList.setVisibility(View.VISIBLE);
                activityMainBinding.logView.setVisibility(View.INVISIBLE);
                activityMainBinding.connectionStatus.setVisibility(View.INVISIBLE);
            }

            movieAdapter.notifyDataSetChanged();
        }
    }

    public void setupViewModel(){
        MainViewModel viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel.getMovies().observe(this, new Observer<List<Movie>>() {
            @Override
            public void onChanged(@Nullable List<Movie> movies) {
                MainActivity.movies.clear();
                if(movies.size()>0){
                    MainActivity.favourite_movies.clear();
                    MainActivity.favourite_movies = (ArrayList) movies;
                }

                displayMovies();
            }
        });
    }


    public void displayMovies() {

        if(sort_value== FAVOURITE){
            MainActivity.movies.clear();
            if (MainActivity.favourite_movies.size() == 0) {
                activityMainBinding.logView.setText("No favourite movies added!");
                activityMainBinding.connectionStatus.setVisibility(View.INVISIBLE);
                activityMainBinding.logView.setVisibility(View.VISIBLE);
                activityMainBinding.moviePosterList.setVisibility(View.INVISIBLE);
                return;
            }

            MainActivity.movies.addAll(MainActivity.favourite_movies);
            activityMainBinding.logView.setVisibility(View.INVISIBLE);
            activityMainBinding.moviePosterList.setVisibility(View.VISIBLE);

            movieAdapter.notifyDataSetChanged();
        }
        else{
            new MovieAsyncTask().execute(sort_value);
        }

    }

    @Override
    public void onMovieItemClick(final int index) {
        boolean flag = false;

        for(int count = 0; count < MainActivity.favourite_movies.size(); count++ ){
            System.out.println("Movie" + count + " ," + MainActivity.favourite_movies.get(count).getMovieName()) ;
            if(MainActivity.favourite_movies.get(count).getMovieName().equals(MainActivity.movies.get(index).getMovieName())){
                flag = true;
                break;
            }
        }

        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("MOVIE_ITEM_INDEX", index);
        intent.putExtra("MOVIE_FAVOURITE_FLAG", flag);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.movie_menu_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.popularity_sort) {
            sort_value = POPULARITY;
            displayMovies();
        } else if (item.getItemId() == R.id.rating_sort) {
            sort_value = TOP_RATED;
            displayMovies();
        } else if (item.getItemId() == R.id.favourite_sort) {
            sort_value = FAVOURITE;
            displayMovies();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(savedInstanceState!=null){
            gridCount = savedInstanceState.getInt("LAST_GRID_COUNT");
            sort_value = savedInstanceState.getInt("LAST_SEARCH_KEY");
        } else {
            sort_value = POPULARITY;
            if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                gridCount = 2;
            } else {
                gridCount = 3;
            }
        }

        setTitle(R.string.home_screen_title);
        activityMainBinding = DataBindingUtil.setContentView(MainActivity.this, R.layout.activity_main);
        gridLayoutManager = new GridLayoutManager(this, gridCount);
        movieAdapter = new MovieAdapter(this, this);
        activityMainBinding.moviePosterList.setHasFixedSize(true);
        activityMainBinding.moviePosterList.setLayoutManager(gridLayoutManager);
        activityMainBinding.moviePosterList.setAdapter(movieAdapter);
        movieAdapter.notifyDataSetChanged();

        new MovieAsyncTask().execute(sort_value);

        appDatabase = AppDatabase.getInstance(MainActivity.this);
        setupViewModel();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        int lastSearch = sort_value;
        outState.putInt("LAST_SEARCH_KEY",lastSearch);
        outState.putInt("LAST_GRID_COUNT",gridCount);
    }
}
