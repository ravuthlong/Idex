package phoenix.idex.Fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import com.android.volley.Cache;

import java.util.ArrayList;
import java.util.List;

import phoenix.idex.ButtonClickedSingleton;
import phoenix.idex.R;
import phoenix.idex.RecyclerViewFeed.MainRecyclerView.adapter.FeedListAdapter;
import phoenix.idex.RecyclerViewFeed.MainRecyclerView.app.AppController;
import phoenix.idex.RecyclerViewFeed.MainRecyclerView.data.FeedItem;
import phoenix.idex.ServerRequestCallBacks.NetworkConnectionCallBack;
import phoenix.idex.UserLocalStore;
import phoenix.idex.Util;
import phoenix.idex.VolleyServerConnections.VolleyMainPosts;

/**
 * Created by Ravinder on 3/19/16.
 */
public class AUserPostListFragment extends Fragment implements  View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = AUserPostListFragment.class.getSimpleName();
    private ProgressDialog progressDialog;
    private RecyclerView recyclerView;
    private List<FeedItem> feedItems;
    private FeedListAdapter feedListAdapter;
    private String JSONResult;
    private SwipeRefreshLayout refreshLayout;
    private ProgressBar spinner;
    private Cache.Entry entry;
    private UserLocalStore userLocalStore;
    private String URL_LoggedInUser;
    private VolleyMainPosts volleyMainPosts;
    private Button bMainLog, bMainRoll, bMainInfo;
    private FragmentManager fragmentManager;
    private Util util = Util.getInstance();
    private ButtonClickedSingleton buttonMonitor = ButtonClickedSingleton.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_mainpost,container, false);
        //postWidget = (FloatingActionButton) v.findViewById(R.id.postWidget);
        refreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.refreshLayout);
        spinner = (ProgressBar) v.findViewById(R.id.progress_bar);
        spinner.setVisibility(View.VISIBLE);
        UserLocalStore.visitCounter = 0;

        bMainRoll = (Button) v.findViewById(R.id.bMainRoll);
        bMainLog = (Button) v.findViewById(R.id.bMainLog);
        bMainInfo = (Button) v.findViewById(R.id.bMainInfo);

        bMainRoll.setOnClickListener(this);
        bMainLog.setOnClickListener(this);
        bMainInfo.setOnClickListener(this);

        buttonMonitor.setUpButtons(bMainRoll, bMainLog, bMainInfo);

        recyclerView = (RecyclerView) v.findViewById(R.id.postRecyclerView1);
        feedItems = new ArrayList<>();

        feedListAdapter = new FeedListAdapter(getActivity(), feedItems);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(feedListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        refreshLayout.setOnRefreshListener(this);
        //postWidget.setOnClickListener(this);
        userLocalStore = new UserLocalStore(getActivity());

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Processing");
        progressDialog.setMessage("Please Wait...");

        volleyMainPosts = new VolleyMainPosts(getContext());
        URL_LoggedInUser = "http://idex.site88.net/fetchOneUserPosts.php?userID=" + userLocalStore.getLoggedInUser().getUserID();

        // We first check for cached request
        Cache cache = AppController.getInstance().getRequestQueue().getCache();
        entry = cache.get(URL_LoggedInUser);

        if (UserLocalStore.allowRefresh) {
            UserLocalStore.allowRefresh = false;

            util.getInternetStatus(getContext(), new NetworkConnectionCallBack() {
                @Override
                public void networkConnection(boolean isConnected) {
                    // If user refreshes the page, load current updated JSON
                    if (isConnected) {
                        volleyMainPosts.getAUniqueUserPosts(feedListAdapter, feedItems, spinner, userLocalStore.getLoggedInUser().getUserID());
                        //getJsonLiveLoggedIn();
                    } else {
                        util.displayNoInternet(getContext());
                        volleyMainPosts.getUserPostsCache(feedListAdapter, feedItems, spinner, entry);
                        //getJsonOffline();
                    }
                }
            });

            // If user is not connected to the internet or if the user already load the page once
            // User cache so the page doesn't need to reload
        } else if ((UserLocalStore.visitCounter > 0)) {
            //getJsonOffline();
            volleyMainPosts.getUserPostsCache(feedListAdapter, feedItems, spinner, entry);

            // Else if the user is connected to the internet, simple load current updated JSON
            // Ex. when user opens app for the first time
        } else {

            util.getInternetStatus(getContext(), new NetworkConnectionCallBack() {
                @Override
                public void networkConnection(boolean isConnected) {
                    if (isConnected) {
                        //getJsonLiveLoggedIn();
                        volleyMainPosts.getAUniqueUserPosts(feedListAdapter, feedItems, spinner, userLocalStore.getLoggedInUser().getUserID());
                    } else {
                        util.displayNoInternet(getContext());
                        //getJsonOffline();
                        volleyMainPosts.getUserPostsCache(feedListAdapter, feedItems, spinner, entry);
                    }
                }
            });

        }
        return v;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
             case R.id.bMainRoll:
                 buttonMonitor.cancelClicks(bMainRoll, bMainLog, bMainInfo);
                 buttonMonitor.setRollClicked();
                 bMainRoll.setBackgroundResource(R.drawable.rolled);

                 fragmentManager = getParentFragment().getFragmentManager();
                 fragmentManager.beginTransaction().replace(R.id.rLayoutMain,
                         new PostListFragment()).commit();
                break;
            case R.id.bMainLog:
                buttonMonitor.cancelClicks(bMainRoll, bMainLog, bMainInfo);
                buttonMonitor.setLogClicked();
                bMainLog.setBackgroundResource(R.drawable.logged);

                fragmentManager = getParentFragment().getFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.rLayoutMain,
                        new DashFragment()).commit();
                break;
            case R.id.bMainInfo:
                break;
        }
    }

    @Override
    public void onRefresh() {

        util.getInternetStatus(getContext(), new NetworkConnectionCallBack() {
            @Override
            public void networkConnection(boolean isConnected) {
                if (isConnected) {
                    UserLocalStore.allowRefresh = true;
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.detach(AUserPostListFragment.this).attach(AUserPostListFragment.this).commit();
                    feedItems.clear();
                } else {
                    util.displayNoInternet(getContext());
                }
            }
        });
    }

    /*

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
                    volleyMainPosts
                    //jsonParserFeed.parseJsonFeed(response);
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
                jsonParserFeed.parseJsonFeed(new JSONObject(data));
                //parseJsonFeed(new JSONObject(data));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }*/
}
