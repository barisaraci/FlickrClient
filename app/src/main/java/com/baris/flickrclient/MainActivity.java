package com.baris.flickrclient;

import android.app.SearchManager;
import android.content.Context;
import android.database.MatrixCursor;
import android.provider.BaseColumns;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.baris.flickrclient.flickr.Image;
import com.baris.flickrclient.flickr.ImageAdapter;
import com.baris.flickrclient.utility.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;

public class MainActivity extends AppCompatActivity {

    private Context context;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeLayout;
    private GridLayoutManager layoutManager;
    private ImageAdapter imageAdapter;
    private View layoutMain;

    private ArrayList<Image> images;
    private ArrayList<String> popularTags;
    private boolean isPhotoLoading = true, isTherePhoto = true, isTagLoading = false, isSearchActive = false;
    private int page = 1;
    private String query;

    private static final String API_KEY = "8bce99018aeeeee48f369a7f67de0a2f";
    private static final int NUM_PHOTOS_PER_PAGE = 40;
    private static final int NUM_POPULAR_TAGS = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        init();
    }

    private void init() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initRecyclerView();
        initSwipeLayout();

        layoutMain = findViewById(R.id.layout_main);

        images = new ArrayList<>();
        popularTags = new ArrayList<>();
        loadImages(query, false);
    }

    private void initRecyclerView() {
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        layoutManager = new GridLayoutManager(context, 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setScrollBarStyle(RecyclerView.SCROLLBARS_OUTSIDE_OVERLAY);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            int pastVisiblesItems, visibleItemCount, totalItemCount;

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    visibleItemCount = layoutManager.getChildCount();
                    totalItemCount = layoutManager.getItemCount();
                    pastVisiblesItems = layoutManager.findFirstVisibleItemPosition();
                    if (visibleItemCount + pastVisiblesItems >= totalItemCount && !isPhotoLoading && isTherePhoto) {
                        isPhotoLoading = true;
                        page++;
                        loadImages(query, isSearchActive);
                        Snackbar.make(layoutMain, context.getResources().getString(R.string.new_page), Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                    }
                }
            }
        });
    }

    private void initSwipeLayout() {
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                page = 1;
                isTherePhoto = true;
                isSearchActive = false;
                loadImages(query, false);
                Snackbar.make(layoutMain, context.getResources().getString(R.string.refresh), Snackbar.LENGTH_SHORT).setAction("Action", null).show();
            }
        });
    }

    private void loadImages(String query, boolean isSearchActive) {
        String postMethod;
        if (isSearchActive) {
            postMethod = "https://api.flickr.com/services/rest/?format=json&nojsoncallback=1&method=flickr.photos.search&per_page=" + NUM_PHOTOS_PER_PAGE + "&tags=" + query + "&page=" + page + "&api_key=" + API_KEY;
        } else {
            postMethod = "https://api.flickr.com/services/rest/?format=json&nojsoncallback=1&method=flickr.photos.getRecent&per_page=" + NUM_PHOTOS_PER_PAGE + "&page=" + page + "&api_key=" + API_KEY;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, postMethod, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject result) {
                if (page == 1) // clean all entities if there is a refresh
                    images.clear();

                addImages(result);

                if (images.isEmpty())
                    Snackbar.make(layoutMain, context.getResources().getString(R.string.no_image), Snackbar.LENGTH_LONG).setAction("Action", null).show();

                if (page == 1) {
                    imageAdapter = new ImageAdapter(context, images);
                    recyclerView.setAdapter(new ScaleInAnimationAdapter(imageAdapter));
                }

                isPhotoLoading = false;
                swipeLayout.setRefreshing(false);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                isPhotoLoading = false;
                swipeLayout.setRefreshing(false);
                Snackbar.make(layoutMain, context.getResources().getString(R.string.error), Snackbar.LENGTH_LONG).setAction("Action", null).show();
                Log.e("Volley Error:", error.toString());
            }
        });

        VolleySingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

    private void addImages(JSONObject result) {
        try {
            if (page >= result.getJSONObject("photos").getInt("pages")) {
                isTherePhoto = false;
            }

            JSONArray photos = result.getJSONObject("photos").getJSONArray("photo");
            for (int i = 0; i < photos.length(); i++) {
                JSONObject imageObject = photos.getJSONObject(i);
                Image image = new Image();
                image.setId(imageObject.getString("id"));
                image.setFarmId(imageObject.getString("farm"));
                image.setServerId(imageObject.getString("server"));
                image.setSecret(imageObject.getString("secret"));
                image.setTitle(imageObject.getString("title"));
                image.setNo((page - 1) * NUM_PHOTOS_PER_PAGE + i + 1);
                image.createLinks();

                if (page == 1) {
                    images.add(image);
                } else {
                    imageAdapter.addItem(imageAdapter.getItemCount() - 1, image);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        initSearchView(menu);
        return true;
    }

    private void initSearchView(Menu menu) {
        final MenuItem item = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setQueryHint(getResources().getString(R.string.tag_info));
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        final CursorAdapter suggestionAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, null, new String[]{SearchManager.SUGGEST_COLUMN_TEXT_1}, new int[]{android.R.id.text1}, 0);
        searchView.setSuggestionsAdapter(suggestionAdapter);

        initSearchViewSuggestionListener(searchView);
        initSearchViewQueryTextListener(searchView, suggestionAdapter);
    }

    private void initSearchViewSuggestionListener(final SearchView searchView) {
        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                searchView.setQuery(searchView.getQuery() + "," + popularTags.get(position), true);
                return true;
            }
        });
    }

    private void initSearchViewQueryTextListener(final SearchView searchView, final CursorAdapter suggestionAdapter) {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String curQuery) {
                query = curQuery;
                isPhotoLoading = true; isSearchActive = true; isTherePhoto = true; page = 1;
                loadImages(query, true);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if (!isTagLoading)
                    getPopularTags();

                isTagLoading = true;

                String[] columns = {BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_INTENT_DATA,};
                MatrixCursor cursor = new MatrixCursor(columns);
                for (int i = 0; i < popularTags.size(); i++) {
                    String[] temp = {Integer.toString(i), "popular tag: " + popularTags.get(i), "popular tag: " + popularTags.get(i)};
                    cursor.addRow(temp);
                }
                suggestionAdapter.swapCursor(cursor);
                return true;
            }
        });
    }

    private void getPopularTags() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, "https://api.flickr.com/services/rest/?format=json&nojsoncallback=1&method=flickr.tags.getHotList&count=" + NUM_POPULAR_TAGS + "&api_key=" + API_KEY, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject result) {
                popularTags.clear();
                addTags(result);

                isTagLoading = false;
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                isTagLoading = false;
                Snackbar.make(layoutMain, context.getResources().getString(R.string.error), Snackbar.LENGTH_LONG).setAction("Action", null).show();
                Log.e("Volley Error:", error.toString());
            }
        });

        VolleySingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

    private void addTags(JSONObject result) {
        try {
            JSONArray tags = result.getJSONObject("hottags").getJSONArray("tag");
            for (int i = 0; i < tags.length(); i++) {
                JSONObject tagObject = tags.getJSONObject(i);
                String tag = tagObject.getString("_content");

                popularTags.add(tag);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

}
