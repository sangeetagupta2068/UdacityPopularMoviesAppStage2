package com.example.udacitypopularmoviesappstage2.utils;

import android.net.Uri;
import android.util.Log;

import com.example.udacitypopularmoviesappstage2.DetailActivity;
import com.example.udacitypopularmoviesappstage2.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class NetworkUtils {
    private static final String API_KEY = "ce34cbe6a1ce756fd273d16fea7331ab";
    private static String SORT_TYPE = "popular";
    private static String ID_CHOSEN = "";
    public static final String YOUTUBE_BASE_URL = "https://www.youtube.com/watch?v=";
    private static final String MOVIE_BASE_URL = "http://api.themoviedb.org/3/movie/";

    public static URL buildURLForMovies(boolean flag) {
        if (flag) {
            SORT_TYPE = "popular";
        } else {
            SORT_TYPE = "top_rated";
        }

        try {
            return new URL(MOVIE_BASE_URL + SORT_TYPE + "?api_key=" + API_KEY);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }

    public static void getTrailers() {
        DetailActivity.trailerIDs.clear();

        ID_CHOSEN = MainActivity.movies.get(DetailActivity.MOVIE_ITEM_INDEX).getMovieId();
        Log.d("ID", ID_CHOSEN);
        String TRAILER_BASE_URL = MOVIE_BASE_URL + ID_CHOSEN + "/videos?api_key=" + API_KEY;
        Uri uri = Uri.parse(TRAILER_BASE_URL).buildUpon().build();
        String result = null;

        try {
            result = getResponseFromHttpUrl(new URL(uri.toString()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        JSONUtils.parseTrailers(result);

    }

    public static void getReviews() {
        DetailActivity.reviews.clear();
        ;

        ID_CHOSEN = MainActivity.movies.get(DetailActivity.MOVIE_ITEM_INDEX).getMovieId();
        Log.d("ID_CHOSEN", ID_CHOSEN);
        String REVIEWS_BASE_URL = MOVIE_BASE_URL + ID_CHOSEN + "/reviews?api_key=" + API_KEY;
        Uri uri = Uri.parse(REVIEWS_BASE_URL).buildUpon().build();
        String result = null;

        try {
            result = getResponseFromHttpUrl(new URL(uri.toString()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        JSONUtils.parseReviews(result);

    }


}


