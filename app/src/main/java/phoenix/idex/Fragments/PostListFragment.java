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
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import phoenix.idex.DashActivity;
import phoenix.idex.JSONParser;
import phoenix.idex.MainActivity;
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
    private String URL_TEST = "http://idex.site88.net/fetchUserPosts.php";
    private SwipeRefreshLayout refreshLayout;
    private ProgressBar spinner;
    private Cache.Entry entry;
    private UserLocalStore userLocalStore;
    private String URL_LoggedInUser;
    private JSONParser jsonParser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_mainpost,container, false);
        postWidget = (FloatingActionButton) v.findViewById(R.id.postWidget);
        refreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.refreshLayout);
        spinner = (ProgressBar) v.findViewById(R.id.progress_bar);

        spinner.setVisibility(View.VISIBLE);

        int sizeOfActionBar = MainActivity.getThemeAttributeDimensionSize(getActivity(), R.attr.actionBarSize);
        MainActivity.rLayoutMain.setPadding(0, sizeOfActionBar, 0, 0);

        recyclerView = (RecyclerView) v.findViewById(R.id.postRecyclerView1);
        feedItems = new ArrayList<>();

        feedListAdapter = new FeedListAdapter(getActivity(), feedItems);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(feedListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        refreshLayout.setOnRefreshListener(this);
        postWidget.setOnClickListener(this);
        userLocalStore = new UserLocalStore(getActivity());

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Processing");
        progressDialog.setMessage("Please Wait...");

        jsonParser = new JSONParser(feedListAdapter, feedItems, spinner);

        URL_LoggedInUser = "http://idex.site88.net/fetchLoggedInUserPosts.php?userID=" + userLocalStore.getLoggedInUser().getUserID();

        // We first check for cached request
        Cache cache = AppController.getInstance().getRequestQueue().getCache();

        if (!UserLocalStore.isUserLoggedIn) {
            entry = cache.get(URL_TEST);
        } else {
            entry = cache.get(URL_LoggedInUser);
        }

        if (UserLocalStore.allowRefresh) {
            UserLocalStore.allowRefresh = false;
            new InternetAccess(getContext(), new NetworkConnectionCallBack() {
                @Override
                public void networkConnection(boolean isConnected) {
                    // If user refreshes the page, load current updated JSON
                    if (isConnected) {
                        if (UserLocalStore.isUserLoggedIn) {
                            getJsonLiveLoggedIn();
                        } else {
                            getJsonLive();
                        }
                    } else {
                        displayNoInternet(getContext());
                        getJsonOffline();
                    }
                }
            }).execute();
            // If user is not connected to the internet or if the user already load the page once
            // User cache so the page doesn't need to reload
        } else if ((UserLocalStore.visitCounter > 0)) {
            getJsonOffline();
            // Else if the user is connected to the internet, simple load current updated JSON
            // Ex. when user opens app for the first time
        } else {
            new InternetAccess(getContext(), new NetworkConnectionCallBack() {
                @Override
                public void networkConnection(boolean isConnected) {
                    if (isConnected) {
                        if (userLocalStore.getLoggedInUser() == null) {
                            getJsonLive();
                        } else {
                            getJsonLiveLoggedIn();
                        }
                    } else {
                        displayNoInternet(getContext());
                        getJsonOffline();
                    }
                }
            }).execute();
        }
        hideWidget();
        return v;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.postWidget:
                Intent intent = new Intent(getActivity(), DashActivity.class);
                startActivity(intent);
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

    // Pull JSON directly from the PHP JSON result
    private void getJsonLive() {
        UserLocalStore.visitCounter++;
        // making fresh volley request and getting json
        JsonObjectRequest jsonReq = new JsonObjectRequest(Request.Method.GET,
                URL_TEST, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                VolleyLog.d(TAG, "Response: " + response.toString());
                if (response != null) {
                    jsonParser.parseJsonFeed(response);
                    //parseJsonFeed(response);
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
            }
        });

        // Adding request to volley request queue
        AppController.getInstance().addToRequestQueue(jsonReq);
    }

    // Pull JSON directly from the PHP JSON result
    private void getJsonLiveLoggedIn() {

        UserLocalStore.visitCounter++;
        // making fresh volley request and getting json
        JsonObjectRequest jsonReq = new JsonObjectRequest(Request.Method.GET,
                URL_LoggedInUser, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                VolleyLog.d(TAG, "Response: " + response.toString());
                if (response != null) {
                    jsonParser.parseJsonFeed(response);
                    //parseJsonFeed(response);
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
            }
        });

        // Adding request to volley request queue
        AppController.getInstance().addToRequestQueue(jsonReq);
    }


    // Pull saved JSON in the cache.
    private void getJsonOffline(){
            // fetch the data from cache if the user is offline
            try {
                String data = new String(entry.data, "UTF-8");
                try {
                    jsonParser.parseJsonFeed(new JSONObject(data));
                    //parseJsonFeed(new JSONObject(data));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
    }
/*
    // Populate FeedItem Array with JSONArray from PHP
    private void parseJsonFeed(JSONObject response) {
        try {
            JSONArray feedArray = response.getJSONArray("feed");

            for (int i = 0; i < feedArray.length(); i++) {
                JSONObject feedObj = (JSONObject) feedArray.get(i);

                FeedItem item = new FeedItem();
                String name;
                name = feedObj.getString("firstname") + " " + feedObj.getString("lastname");
                item.setId(feedObj.getInt("postID"));
                item.setName(name);
                item.setUsername(feedObj.getString("username"));
                item.setStatus(feedObj.getString("post"));
                item.setProfilePic(feedObj.getString("userpic"));
                item.setTimeStamp(feedObj.getString("timeStamp"));
                item.setCurrentColumn(feedObj.getInt("currentColumn"));
                item.setOneFill(feedObj.getInt("oneFill"));
                item.setTwoFill(feedObj.getInt("twoFill"));
                item.setThreeFill(feedObj.getInt("threeFill"));
                item.setFourFill(feedObj.getInt("fourFill"));
                item.setFiveFill(feedObj.getInt("fiveFill"));
                item.setSixFill(feedObj.getInt("sixFill"));
                item.setSevenFill(feedObj.getInt("sevenFill"));
                item.setEightFill(feedObj.getInt("eightFill"));
                item.setNineFill(feedObj.getInt("nineFill"));
                item.setTenFill(feedObj.getInt("tenFill"));
                item.setOneKill(feedObj.getInt("oneKill"));
                item.setTwoKill(feedObj.getInt("twoKill"));
                item.setThreeKill(feedObj.getInt("threeKill"));
                item.setFourKill(feedObj.getInt("fourKill"));
                item.setFiveKill(feedObj.getInt("fiveKill"));
                item.setSixKill(feedObj.getInt("sixKill"));
                item.setSevenKill(feedObj.getInt("sevenKill"));
                item.setEightKill(feedObj.getInt("eightKill"));
                item.setNineKill(feedObj.getInt("nineKill"));
                item.setTenKill(feedObj.getInt("tenKill"));
                item.setTotalFill(feedObj.getInt("totalFill"));
                item.setTotalKill(feedObj.getInt("totalKill"));

                try{
                    item.setFillOrKill(feedObj.getInt("status"));
                } catch (JSONException e) {
                    item.setFillOrKill(-1); // Default value for posts user never clicked fill nor kill
                }
                //item.setFill(feedObj.getInt("fill"));
                //item.setKill(feedObj.getInt("kill"));
                //item.setValue();
                feedItems.add(item);
            }
            // notify data changes to list adapater
            feedListAdapter.notifyDataSetChanged();
            //progressDialog.hide();
            spinner.setVisibility(View.GONE);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
*/
    // If user is not logged in, hide the widget. Otherwise, show the widget.
    private void hideWidget() {
        if (!UserLocalStore.isUserLoggedIn) {
            postWidget.hide();
        }
    }

    public  static void displayNoInternet(Context context) {
        Toast toast= Toast.makeText(context,
                "Not connected to the internet", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.show();

    }
}