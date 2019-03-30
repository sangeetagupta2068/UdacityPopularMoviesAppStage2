package com.example.udacitypopularmoviesappstage2;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.Toast;

import com.example.udacitypopularmoviesappstage2.database.AppExecutors;
import com.example.udacitypopularmoviesappstage2.databinding.ActivityDetailBinding;
import com.example.udacitypopularmoviesappstage2.utils.NetworkUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class DetailActivity extends AppCompatActivity {

    public static int MOVIE_ITEM_INDEX = 0;
    boolean isFavouriteFlag;
    ActivityDetailBinding activityDetailBinding;

    LinearLayoutManager trailerLinearLayoutManager;
    TrailerAdapter trailerAdapter;

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
                activityDetailBinding.button.setText(R.string.remove_favourite_label);
            } else {
                activityDetailBinding.button.setText(R.string.mark_as_favourite_label);
            }

            System.out.println(isFavouriteFlag);
        }

        activityDetailBinding.button.setOnClickListener(new View.OnClickListener() {
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
                    activityDetailBinding.button.setText(R.string.mark_as_favourite_label);

                } else {
                    AppExecutors.getInstance().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.appDatabase.movieDao().insertMovie(MainActivity.movies.get(MOVIE_ITEM_INDEX));
                        }
                    });
                    Toast.makeText(DetailActivity.this, MainActivity.movies.get(MOVIE_ITEM_INDEX).getMovieName() + " added to favourites!", Toast.LENGTH_SHORT).show();
                    activityDetailBinding.button.setText(R.string.remove_favourite_label);
                }

                finish();
            }
        });

        new DetailMovieAsyncTask().execute();
        setMovieView();

    }


    public void initialize() {
        activityDetailBinding = DataBindingUtil.setContentView(DetailActivity.this, R.layout.activity_detail);

        trailerLinearLayoutManager = new LinearLayoutManager(this);
        trailerAdapter = new TrailerAdapter(this, new TrailerAdapter.ItemClickListener() {
            @Override
            public void onItemClick(int position) {

                Intent videoIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(NetworkUtils.YOUTUBE_BASE_URL + trailerIDs.get(position)));
                startActivity(videoIntent);
            }
        });

        activityDetailBinding.trailerReviewLayout.detailTrailerRecyclerView.setNestedScrollingEnabled(false);
        activityDetailBinding.trailerReviewLayout.detailTrailerRecyclerView.setLayoutManager(trailerLinearLayoutManager);
        activityDetailBinding.trailerReviewLayout.detailTrailerRecyclerView.setAdapter(trailerAdapter);

        activityDetailBinding.trailerReviewLayout.detailReviewRecyclerView.setNestedScrollingEnabled(false);
        reviewAdapter = new ReviewAdapter(this);
        activityDetailBinding.trailerReviewLayout.detailReviewRecyclerView.setAdapter(reviewAdapter);
        reviewLinearLayoutManager = new LinearLayoutManager(this);
        activityDetailBinding.trailerReviewLayout.detailReviewRecyclerView.setLayoutManager(reviewLinearLayoutManager);

    }

    public void setMovieView() {
        setTitle(MainActivity.movies.get(MOVIE_ITEM_INDEX).getMovieName());

        activityDetailBinding.movieRating.setText(String.format("%s: %s", getString(R.string.rating_label), MainActivity.movies.get(MOVIE_ITEM_INDEX).getMovieRating()));
        activityDetailBinding.moviePlotSummary.setText(String.format("%s %s", getString(R.string.summary_label), MainActivity.movies.get(MOVIE_ITEM_INDEX).getMovieSummary()));
        activityDetailBinding.movieReleaseDate.setText(String.format("%s %s", getString(R.string.release_date_label), MainActivity.movies.get(MOVIE_ITEM_INDEX).getMovieReleaseDate()));

        Picasso.with(DetailActivity.this)
                .load(MainActivity.movies.get(MOVIE_ITEM_INDEX).getMovieImageUrl())
                .into(activityDetailBinding.moviePoster);
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