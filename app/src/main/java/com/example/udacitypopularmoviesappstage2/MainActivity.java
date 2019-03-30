package com.example.udacitypopularmoviesappstage2;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.res.Configuration;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.udacitypopularmoviesappstage2.data.Movie;
import com.example.udacitypopularmoviesappstage2.database.AppDatabase;
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


    RecyclerView movieRecyclerView;
    ProgressBar progressBar;
    TextView textView;
    MovieAdapter movieAdapter;
    GridLayoutManager gridLayoutManager;

    int sort_value = POPULARITY;

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
            progressBar.setVisibility(View.VISIBLE);
            movieRecyclerView.setVisibility(View.INVISIBLE);
            textView.setVisibility(View.INVISIBLE);

        }

        @Override
        protected void onPostExecute(Integer bool) {
            super.onPostExecute(bool);
            progressBar.setVisibility(View.INVISIBLE);
            if (MainActivity.movies.size() == 0 || MainActivity.movies == null) {
                textView.setText(getString(R.string.log_failed_data_retrieval));
                textView.setVisibility(View.VISIBLE);
                movieRecyclerView.setVisibility(View.INVISIBLE);
            } else {
                movieRecyclerView.setVisibility(View.VISIBLE);
                textView.setVisibility(View.INVISIBLE);
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
                textView.setText("No favourite movies added!");
                textView.setVisibility(View.VISIBLE);
                movieRecyclerView.setVisibility(View.INVISIBLE);
                return;
            }

            MainActivity.movies.addAll(MainActivity.favourite_movies);
            textView.setVisibility(View.INVISIBLE);
            movieRecyclerView.setVisibility(View.VISIBLE);

            movieAdapter.notifyDataSetChanged();
        }
        else{
            new MovieAsyncTask().execute(sort_value);
        }

    }

    public void initialize() {
        setTitle(R.string.home_screen_title);

        appDatabase = AppDatabase.getInstance(MainActivity.this);

        textView = findViewById(R.id.log_view);
        progressBar = findViewById(R.id.connection_status);
        movieRecyclerView = findViewById(R.id.movie_poster_list);


        int gridColumnCount;
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            gridColumnCount = 2;
        } else {
            gridColumnCount = 3;
        }

        gridLayoutManager = new GridLayoutManager(this, gridColumnCount);
        movieAdapter = new MovieAdapter(this, this);

        new MovieAsyncTask().execute(POPULARITY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();

        movieRecyclerView.setHasFixedSize(true);
        movieRecyclerView.setLayoutManager(gridLayoutManager);
        movieRecyclerView.setAdapter(movieAdapter);
        movieAdapter.notifyDataSetChanged();

        setupViewModel();

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

}
