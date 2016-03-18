package phoenix.idex.Fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import phoenix.idex.DashActivity;
import phoenix.idex.R;
import phoenix.idex.RecyclerViewFeed.MainRecyclerView.adapter.FeedListAdapter;
import phoenix.idex.RecyclerViewFeed.MainRecyclerView.app.AppController;
import phoenix.idex.RecyclerViewFeed.MainRecyclerView.data.FeedItem;
import phoenix.idex.UserLocalStore;

public class PostListFragment extends Fragment implements  View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = PostListFragment.class.getSimpleName();
    private ProgressDialog progressDialog;
    private RecyclerView recyclerView;
    private List<FeedItem> feedItems;
    private FeedListAdapter feedListAdapter;
    private FloatingActionButton postWidget;
    private String URL_FEED = "http://paulphoenix.netai.net/feed_temporary.json";
    private String URL_TEST = "http://idex.site88.net/fetchUserPosts.php";
    private SwipeRefreshLayout refreshLayout;
    private ProgressBar spinner;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_mainpost,container, false);
        postWidget = (FloatingActionButton) v.findViewById(R.id.postWidget);
        refreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.refreshLayout);
        spinner = (ProgressBar) v.findViewById(R.id.progress_bar);
        spinner.setVisibility(View.VISIBLE);

        recyclerView = (RecyclerView) v.findViewById(R.id.postRecyclerView1);
        feedItems = new ArrayList<>();

        feedListAdapter = new FeedListAdapter(getActivity(), feedItems);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(feedListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        refreshLayout.setOnRefreshListener(this);
        postWidget.setOnClickListener(this);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Processing");
        progressDialog.setMessage("Please Wait...");

        hideWidget();
        getJson();
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
        Toast.makeText(getActivity(), "Refresh", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getJson();
                refreshLayout.setRefreshing(false);
            }
        }, 2000);
    }

    private void getJson(){

        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo  = connectivityManager.getActiveNetworkInfo();

        // We first check for cached request
        //Cache cache = AppController.getInstance().getRequestQueue().getCache();
        //Cache.Entry entry = cache.get(URL_FEED);
        //if ((entry != null) && (networkInfo == null) || (UserLocalStore.visitCounter > 0)) {
        if ((networkInfo == null)) {
            // fetch the data from cache if the user is offline

        } else {
            UserLocalStore.visitCounter++;

            //progressDialog.show();
            // making fresh volley request and getting json
            JsonObjectRequest jsonReq = new JsonObjectRequest(Request.Method.GET,
                    URL_TEST, null, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    VolleyLog.d(TAG, "Response: " + response.toString());
                    if (response != null) {
                        parseJsonFeed(response);
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
    }

    private void parseJsonFeed(JSONObject response) {
        try {
            JSONArray feedArray = response.getJSONArray("feed");

            for (int i = 0; i < feedArray.length(); i++) {
                JSONObject feedObj = (JSONObject) feedArray.get(i);

                FeedItem item = new FeedItem();
                String name;

                name = feedObj.getString("firstname") + feedObj.getString("lastname");

                //item.setId(feedObj.getInt("id"));
                item.setName(name);
                item.setUsername(feedObj.getString("username"));
                item.setStatus(feedObj.getString("post"));
                //item.setProfilePic(feedObj.getString("profilePic"));
                item.setTimeStamp(feedObj.getString("timeStamp"));
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

    // If user is not logged in, hide the widget. Otherwise, show the widget.
    private void hideWidget() {
        if (!UserLocalStore.isUserLoggedIn) {
            postWidget.hide();
        }
    }
}