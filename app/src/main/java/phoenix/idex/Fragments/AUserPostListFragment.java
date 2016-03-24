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
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Cache;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import phoenix.idex.DashActivity;
import phoenix.idex.R;
import phoenix.idex.RecyclerViewFeed.MainRecyclerView.adapter.FeedListAdapter;
import phoenix.idex.RecyclerViewFeed.MainRecyclerView.data.FeedItem;
import phoenix.idex.ServerConnections.ServerRequests;
import phoenix.idex.UserLocalStore;
import phoenix.idex.UserPostsCallBack;

/**
 * Created by Ravinder on 3/19/16.
 */
public class AUserPostListFragment extends Fragment implements  View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = AUserPostListFragment.class.getSimpleName();
    private ProgressDialog progressDialog;
    private RecyclerView recyclerView;
    private List<FeedItem> feedItems;
    private FeedListAdapter feedListAdapter;
    private FloatingActionButton postWidget;
    private String JSONResult;
    private SwipeRefreshLayout refreshLayout;
    private ProgressBar spinner;
    private Cache.Entry entry;
    private ServerRequests serverRequests;
    private UserLocalStore userLocalStore;

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

        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo  = connectivityManager.getActiveNetworkInfo();

        getJsonLive();
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
                refreshLayout.setRefreshing(false);
                UserLocalStore.allowRefresh = true;
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.detach(AUserPostListFragment.this).attach(AUserPostListFragment.this).commit();
            }
        }, 1000);
    }

    private void getJsonLive() {

        serverRequests = new ServerRequests(getActivity());
        userLocalStore = new UserLocalStore(getActivity());

        serverRequests.fetchOneUserPostsInBackground(userLocalStore.getLoggedInUser().getUserID(), new UserPostsCallBack() {
            @Override
            public void jsonString(String JSONString) {
                JSONResult = JSONString;

                try {
                    System.out.println("JSONRESULT : ");
                    JSONObject response = new JSONObject(JSONString);
                    JSONArray jArray = response.getJSONArray("feed");

                    for (int i = 0; i < jArray.length(); i++) {
                        JSONObject feedObj = jArray.getJSONObject(i);

                        FeedItem item = new FeedItem();

                        String name;
                        String picLinkJSON = feedObj.getString("userpic");
                        name = feedObj.getString("firstname") + feedObj.getString("lastname");
                        item.setId(feedObj.getInt("postID"));
                        item.setName(name);
                        item.setUsername(feedObj.getString("username"));
                        item.setStatus(feedObj.getString("post"));
                        item.setProfilePic(picLinkJSON);
                        item.setTimeStamp(feedObj.getString("timeStamp"));
                        //item.setFill(feedObj.getInt("fill"));
                        //item.setKill(feedObj.getInt("kill"));
                        //item.setValue();

                        feedItems.add(item);
                        // notify data changes to list adapater
                        feedListAdapter.notifyDataSetChanged();
                        //progressDialog.hide();
                        spinner.setVisibility(View.GONE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });




        /*
        //UserLocalStore.visitCounter++;
        //progressDialog.show();
        // making fresh volley request and getting json
        JsonObjectRequest jsonReq = new JsonObjectRequest(Request.Method.GET,
                JSONResult, null, new Response.Listener<JSONObject>() {

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
    private void getJsonOffline(){

        // fetch the data from cache if the user is offline
        try {
            String data = new String(entry.data, "UTF-8");
            try {
                parseJsonFeed(new JSONObject(data));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    private void parseJsonFeed(JSONObject response) {
        try {
            JSONArray feedArray = response.getJSONArray("feed");

            for (int i = 0; i < feedArray.length(); i++) {
                JSONObject feedObj = (JSONObject) feedArray.get(i);

                FeedItem item = new FeedItem();
                String name;
                String picLinkJSON = feedObj.getString("userpic");
                //String picLink = picLinkJSON.replace("\\", "");

                System.out.println("!!!!!!!!IMAGE LINK: " + picLinkJSON);

                name = feedObj.getString("firstname") + feedObj.getString("lastname");
                item.setId(feedObj.getInt("postID"));
                item.setName(name);
                item.setUsername(feedObj.getString("username"));
                item.setStatus(feedObj.getString("post"));
                System.out.println("!!!!!!!!POST: " + feedObj.getString("post"));

                item.setProfilePic(picLinkJSON);
                item.setTimeStamp(feedObj.getString("timeStamp"));
                item.setFill(feedObj.getInt("fill"));
                item.setKill(feedObj.getInt("kill"));
                //item.setValue();

                feedItems.add(item);
            }

            // notify data changes to list adapater
            feedListAdapter.notifyDataSetChanged();
            //progressDialog.hide();
            spinner.setVisibility(View.GONE);

        } catch (JSONException e) {
            e.printStackTrace();
        }*/
    }
}
