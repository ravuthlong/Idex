package phoenix.idex.Fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import phoenix.idex.Activities.DashActivity;
import phoenix.idex.Activities.MainActivity;
import phoenix.idex.VolleyServerConnections.VolleyConnections;
import phoenix.idex.R;
import phoenix.idex.RecyclerViewFeed.MainRecyclerView.adapter.FeedListAdapter;
import phoenix.idex.RecyclerViewFeed.MainRecyclerView.app.AppController;
import phoenix.idex.RecyclerViewFeed.MainRecyclerView.data.FeedItem;
import phoenix.idex.ServerRequestCallBacks.NetworkConnectionCallBack;
import phoenix.idex.UserLocalStore;

public class PostListFragment extends Fragment implements  View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = PostListFragment.class.getSimpleName();
    private ProgressDialog progressDialog;
    private RecyclerView recyclerView;
    private List<FeedItem> feedItems;
    private FeedListAdapter feedListAdapter;
    private FloatingActionButton postWidget;
    private SwipeRefreshLayout refreshLayout;
    private ProgressBar spinner;
    private Cache.Entry entry;
    private UserLocalStore userLocalStore;
    private String URL_LoggedInUser;
    private Button bMainDash, bMainRoll, bMainInfo;
    private FragmentManager fragmentManager;
    private VolleyConnections volleyConnections;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_mainpost,container, false);
        //postWidget = (FloatingActionButton) v.findViewById(R.id.postWidget);
        refreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.refreshLayout);
        spinner = (ProgressBar) v.findViewById(R.id.progress_bar);


       // String url = "http://idex.site88.net/fetchCurrentValue.php?postID=59";
        //getJsonLive(url);

        bMainRoll = (Button) v.findViewById(R.id.bMainRoll);
        bMainDash = (Button) v.findViewById(R.id.bMainDash);
        bMainInfo = (Button) v.findViewById(R.id.bMainInfo);

        bMainRoll.setOnClickListener(this);
        bMainDash.setOnClickListener(this);
        bMainInfo.setOnClickListener(this);

        spinner.setVisibility(View.VISIBLE);

        int sizeOfActionBar = MainActivity.getThemeAttributeDimensionSize(getActivity(), R.attr.actionBarSize);
        MainActivity.rLayoutMain.setPadding(0, sizeOfActionBar, 0, 0);

        recyclerView = (RecyclerView) v.findViewById(R.id.postRecyclerView1);
        feedItems = new ArrayList<>();

        feedListAdapter = new FeedListAdapter(getActivity(), feedItems);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(feedListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));


        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // Scroll down
                if (dy > 0) {
                    bMainDash.setVisibility(View.GONE);
                    bMainRoll.setVisibility(View.GONE);
                    bMainInfo.setVisibility(View.GONE);
                } else {
                    // Scroll up
                    bMainRoll.setVisibility(View.VISIBLE);
                    bMainDash.setVisibility(View.VISIBLE);
                    bMainInfo.setVisibility(View.VISIBLE);
                }
            }
        });

        refreshLayout.setOnRefreshListener(this);
        //postWidget.setOnClickListener(this);
        userLocalStore = new UserLocalStore(getActivity());

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Processing");
        progressDialog.setMessage("Please Wait...");

        volleyConnections = new VolleyConnections(getContext());

        // We first check for cached request
        Cache cache = AppController.getInstance().getRequestQueue().getCache();

        if (!UserLocalStore.isUserLoggedIn) {
            entry = cache.get("http://idex.site88.net/fetchUserPosts.php");
        } else {
            entry = cache.get("http://idex.site88.net/fetchLoggedInUserPosts.php?userID=" + userLocalStore.getLoggedInUser().getUserID());
        }

        if (UserLocalStore.allowRefresh) {
            UserLocalStore.allowRefresh = false;
            new InternetAccess(getContext(), new NetworkConnectionCallBack() {
                @Override
                public void networkConnection(boolean isConnected) {
                    // If user refreshes the page, load current updated JSON
                    if (isConnected) {
                        if (UserLocalStore.isUserLoggedIn) {
                            volleyConnections.getUserPostsLoggedIn(feedListAdapter, feedItems, spinner, userLocalStore.getLoggedInUser().getUserID());
                            //jsonParserFeed.getJsonLiveLoggedIn(URL_LoggedInUser);
                        } else {
                            volleyConnections.getUserPostsNotLoggedIn(feedListAdapter, feedItems, spinner);
                            //jsonParserFeed.getJsonLive(URL_TEST);
                        }
                    } else {
                        displayNoInternet(getContext());
                        volleyConnections.getUserPostsCache(feedListAdapter, feedItems, spinner, entry);
                        //jsonParserFeed.getJsonOffline(entry);
                    }
                }
            }).execute();
            // If user is not connected to the internet or if the user already load the page once
            // User cache so the page doesn't need to reload
        } else if ((UserLocalStore.visitCounter > 0)) {
            volleyConnections.getUserPostsCache(feedListAdapter, feedItems, spinner, entry);
            // Else if the user is connected to the internet, simple load current updated JSON
            // Ex. when user opens app for the first time
        } else {
            new InternetAccess(getContext(), new NetworkConnectionCallBack() {
                @Override
                public void networkConnection(boolean isConnected) {
                    if (isConnected) {
                        if (!UserLocalStore.isUserLoggedIn) {
                            volleyConnections.getUserPostsNotLoggedIn(feedListAdapter, feedItems, spinner);
                            //jsonParserFeed.getJsonLive(URL_TEST);
                        } else {
                            volleyConnections.getUserPostsLoggedIn(feedListAdapter, feedItems, spinner, userLocalStore.getLoggedInUser().getUserID());
                            //jsonParserFeed.getJsonLiveLoggedIn(URL_LoggedInUser);
                        }
                    } else {
                        volleyConnections.getUserPostsCache(feedListAdapter, feedItems, spinner, entry);
                        displayNoInternet(getContext());
                        //jsonParserFeed.getJsonOffline(entry);
                    }
                }
            }).execute();
        }
        //hideWidget();
        return v;
    }

    // Pull JSON directly from the PHP JSON result
    public void getJsonLive(String URL_TEST) {
        // making fresh volley request and getting json
        JsonObjectRequest jsonReq = new JsonObjectRequest(Request.Method.GET,
                URL_TEST, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (response != null) {
                    parseJsonFeed(response);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        // Adding request to volley request queue
        AppController.getInstance().addToRequestQueue(jsonReq);
    }

    // Populate FeedItem Array with JSONArray from PHP
    public void parseJsonFeed(JSONObject response) {

    }


    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.bMainRoll:
                fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.rLayoutMain,
                        new PostListFragment()).commit();
                MainActivity.listView.setItemChecked(0, true);
                break;
            case R.id.bMainDash:
                getContext().startActivity(new Intent(getActivity(), DashActivity.class));
                break;
            case R.id.bMainInfo:
                fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.rLayoutMain,
                        new TabFragment()).commit();
                MainActivity.listView.setItemChecked(1, true);
                break;

        }
    }

    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setRefreshing(false);
                new InternetAccess(getContext(), new NetworkConnectionCallBack() {
                    @Override
                    public void networkConnection(boolean isConnected) {
                        if (isConnected) {
                            UserLocalStore.allowRefresh = true;
                            FragmentTransaction ft = getFragmentManager().beginTransaction();
                            ft.detach(PostListFragment.this).attach(PostListFragment.this).commit();
                            feedItems.clear();
                        } else {
                            displayNoInternet(getContext());
                        }
                    }
                }).execute();
            }
        }, 200);
    }

    public static class InternetAccess extends AsyncTask <Void, Void, Boolean> {
        private Context context;
        NetworkConnectionCallBack networkConnectionCallBack;

        public InternetAccess(Context context, NetworkConnectionCallBack networkConnectionCallBack) {
            this.context = context;
            this.networkConnectionCallBack = networkConnectionCallBack;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

            if (networkInfo != null) {
                try {
                    HttpURLConnection urlc = (HttpURLConnection)
                            (new URL("http://clients3.google.com/generate_204")
                                    .openConnection());
                    urlc.setRequestProperty("User-Agent", "Android");
                    urlc.setRequestProperty("Connection", "close");
                    urlc.setConnectTimeout(1500);
                    urlc.connect();
                    return (urlc.getResponseCode() == 204 &&
                            urlc.getContentLength() == 0);
                } catch (IOException e) {
                    Log.e(TAG, "Error checking internet connection", e);
                }
            } else {
                Log.d(TAG, "No network available!");
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            networkConnectionCallBack.networkConnection(aBoolean);
        }
    }

    // If user is not logged in, hide the widget. Otherwise, show the widget.
    private void hideWidget() {
        if (!UserLocalStore.isUserLoggedIn) {
            //postWidget.hide();
        }
    }

    public  static void displayNoInternet(Context context) {
        Toast toast= Toast.makeText(context,
                "Not connected to the internet", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.show();

    }
}