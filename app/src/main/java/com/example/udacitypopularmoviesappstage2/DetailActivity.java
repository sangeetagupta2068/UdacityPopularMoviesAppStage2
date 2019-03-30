package com.example.udacitypopularmoviesappstage2;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.udacitypopularmoviesappstage2.database.AppExecutors;
import com.example.udacitypopularmoviesappstage2.utils.NetworkUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class DetailActivity extends AppCompatActivity {

    public static int MOVIE_ITEM_INDEX = 0;
    boolean isFavouriteFlag;
    ImageView moviePosterView;
    Button markAsFavouriteView;
    TextView movieReleaseView, movieDescriptionView, movieRatingView;

    RecyclerView trailerRecyclerView;
    LinearLayoutManager trailerLinearLayoutManager;
    TrailerAdapter trailerAdapter;

    RecyclerView reviewRecyclerView;
    ReviewAdapter reviewAdapter;
    LinearLayoutManager reviewLinearLayoutManager;


    public static ArrayList<String> trailerIDs = new ArrayList<>();
    public static ArrayList<String> reviews = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        initialize();

        Intent intent = getIntent();
        if (intent.hasExtra("MOVIE_ITEM_INDEX")) {
            MOVIE_ITEM_INDEX = intent.getIntExtra("MOVIE_ITEM_INDEX", 0);
            isFavouriteFlag = intent.getBooleanExtra("MOVIE_FAVOURITE_FLAG", false);

            if (isFavouriteFlag) {
                markAsFavouriteView.setText(R.string.remove_favourite_label);
            } else {
                markAsFavouriteView.setText(R.string.mark_as_favourite_label);
            }

            System.out.println(isFavouriteFlag);
        }

        markAsFavouriteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFavouriteFlag) {
                    AppExecutors.getInstance().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.appDatabase.movieDao().deleteMovie(MainActivity.movies.get(MOVIE_ITEM_INDEX));
                        }
                    });

                    Toast.makeText(DetailActivity.this, MainActivity.movies.get(MOVIE_ITEM_INDEX).getMovieName() + " removed from favourites!", Toast.LENGTH_SHORT).show();
                    markAsFavouriteView.setText(R.string.mark_as_favourite_label);

                } else {
                    AppExecutors.getInstance().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.appDatabase.movieDao().insertMovie(MainActivity.movies.get(MOVIE_ITEM_INDEX));
                        }
                    });
                    Toast.makeText(DetailActivity.this, MainActivity.movies.get(MOVIE_ITEM_INDEX).getMovieName() + " added to favourites!", Toast.LENGTH_SHORT).show();
                    markAsFavouriteView.setText(R.string.remove_favourite_label);
                }

                finish();
            }
        });

        new DetailMovieAsyncTask().execute();
        setMovieView();

    }


    public void initialize() {
        markAsFavouriteView = findViewById(R.id.button);
        moviePosterView = findViewById(R.id.movie_poster);
        movieDescriptionView = findViewById(R.id.movie_plot_summary);
        movieRatingView = findViewById(R.id.movie_rating);
        movieReleaseView = findViewById(R.id.movie_release_date);

        trailerRecyclerView = findViewById(R.id.detail_trailer_recycler_view);
        trailerLinearLayoutManager = new LinearLayoutManager(this);
        trailerAdapter = new TrailerAdapter(this, new TrailerAdapter.ItemClickListener() {
            @Override
            public void onItemClick(int position) {

                Intent videoIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(NetworkUtils.YOUTUBE_BASE_URL + trailerIDs.get(position)));
                startActivity(videoIntent);
            }
        });

        trailerRecyclerView.setNestedScrollingEnabled(false);
        trailerRecyclerView.setLayoutManager(trailerLinearLayoutManager);
        trailerRecyclerView.setAdapter(trailerAdapter);

        reviewRecyclerView = findViewById(R.id.detail_review_recycler_view);
        reviewRecyclerView.setNestedScrollingEnabled(false);
        reviewAdapter = new ReviewAdapter(this);
        reviewRecyclerView.setAdapter(reviewAdapter);
        reviewLinearLayoutManager = new LinearLayoutManager(this);
        reviewRecyclerView.setLayoutManager(reviewLinearLayoutManager);

    }

    public void setMovieView() {
        setTitle(MainActivity.movies.get(MOVIE_ITEM_INDEX).getMovieName());

        movieRatingView.setText(String.format("%s: %s", getString(R.string.rating_label), MainActivity.movies.get(MOVIE_ITEM_INDEX).getMovieRating()));
        movieDescriptionView.setText(String.format("%s %s", getString(R.string.summary_label), MainActivity.movies.get(MOVIE_ITEM_INDEX).getMovieSummary()));
        movieReleaseView.setText(String.format("%s %s", getString(R.string.release_date_label), MainActivity.movies.get(MOVIE_ITEM_INDEX).getMovieReleaseDate()));

        Picasso.with(DetailActivity.this)
                .load(MainActivity.movies.get(MOVIE_ITEM_INDEX).getMovieImageUrl())
                .into(moviePosterView);
    }

    private class DetailMovieAsyncTask extends AsyncTask<Boolean, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Boolean... params) {

            NetworkUtils.getTrailers();
            NetworkUtils.getReviews();
            return true;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected void onPostExecute(Boolean bool) {
            super.onPostExecute(bool);
            trailerAdapter.notifyDataSetChanged();
            reviewAdapter.notifyDataSetChanged();
        }
    }
}