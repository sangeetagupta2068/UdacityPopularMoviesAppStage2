package com.example.udacitypopularmoviesappstage2.utils;

import android.util.Log;

import com.example.udacitypopularmoviesappstage2.DetailActivity;
import com.example.udacitypopularmoviesappstage2.MainActivity;
import com.example.udacitypopularmoviesappstage2.data.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONUtils {
    private static String POSTER_BASE_URL = "http://image.tmdb.org/t/p/w500/";

    public static void getMovieList(String jsonString) {
        try {

            JSONObject moviesRootNode = new JSONObject(jsonString);
            JSONArray moviesResultNode = moviesRootNode.getJSONArray("results");

            for (int count = 0; count < moviesResultNode.length(); count++) {

                JSONObject movieJSON = moviesResultNode.getJSONObject(count);

                String movieName = movieJSON.getString("title");
                String id = movieJSON.getString("id");
                String movieImageURL = POSTER_BASE_URL + movieJSON.getString("poster_path");
                String movieSummary = movieJSON.getString("overview");
                String movieReleaseDate = movieJSON.getString("release_date");
                String movieRating = movieJSON.getString("vote_average");

                Log.d("Movie", id + " " + movieName);

                Movie movie = new Movie(movieName, movieRating, movieSummary, movieImageURL, movieReleaseDate,id);
                Log.d("MOVIE_DETAILS", movie.getMovieId());
                MainActivity.movies.add(new Movie(movieName, movieRating, movieSummary, movieImageURL, movieReleaseDate, id));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public static void parseTrailers(String result) {
        try {
            JSONObject j = new JSONObject(result);
            JSONArray trailers = j.getJSONArray("results");
            for (int i = 0; i < trailers.length() - 1; i++) {
                DetailActivity.trailerIDs.add(trailers.getJSONObject(i).getString("key"));

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void parseReviews(String result) {
        try {
            JSONObject j = new JSONObject(result);
            JSONArray reviews = j.getJSONArray("results");
            for (int i = 0; i < reviews.length() - 1; i++) {
                DetailActivity.reviews.add(reviews.getJSONObject(i).getString("content"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
