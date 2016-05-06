package phoenix.idex.Fragments;

import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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

import java.util.ArrayList;
import java.util.List;

import phoenix.idex.Activities.MainActivity;
import phoenix.idex.ButtonClickedSingleton;
import phoenix.idex.R;
import phoenix.idex.RecyclerViewFeed.MainRecyclerView.adapter.FeedListAdapter;
import phoenix.idex.RecyclerViewFeed.MainRecyclerView.app.AppController;
import phoenix.idex.RecyclerViewFeed.MainRecyclerView.data.FeedItem;
import phoenix.idex.ServerRequestCallBacks.NetworkConnectionCallBack;
import phoenix.idex.UserLocalStore;
import phoenix.idex.Util;
import phoenix.idex.VolleyServerConnections.VolleyMainPosts;

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
    private Button bMainLog, bMainRoll, bMainInfo;
    private FragmentManager fragmentManager;
    private VolleyMainPosts volleyMainPosts;
    Util util = Util.getInstance();
    ButtonClickedSingleton  buttonMonitor = ButtonClickedSingleton.getInstance();
    private Toolbar toolbar;
    //private ImageView bottomBanner;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_mainpost,container, false);
        //postWidget = (FloatingActionButton) v.findViewById(R.id.postWidget);
        refreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.refreshLayout);
        spinner = (ProgressBar) v.findViewById(R.id.progress_bar);

/*
        toolbar = (Toolbar) v.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(true);*/
       // String url = "http://idex.site88.net/fetchCurrentValue.php?postID=59";
        //getJsonLive(url);

        bMainRoll = (Button) v.findViewById(R.id.bMainRoll);
        bMainLog = (Button) v.findViewById(R.id.bMainLog);
        bMainInfo = (Button) v.findViewById(R.id.bMainInfo);
       // bottomBanner = (ImageView) v.findViewById(R.id.bottomBanner);

        bMainRoll.setOnClickListener(this);
        bMainLog.setOnClickListener(this);
        bMainInfo.setOnClickListener(this);

        spinner.setVisibility(View.VISIBLE);


        // set up the buttons
        buttonMonitor.setUpButtons(bMainRoll, bMainLog, bMainInfo);

        int screenSize = getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK;

        switch (screenSize) {
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                System.out.println("SMALL SCREEN");
                break;
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                System.out.println("NORMAL SCREEN");
                MainActivity.rLayoutMain.setPadding(0, 310, 0, 0);

                break;
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                System.out.println("LARGE SCREEN");
                break;
            case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                System.out.println("X-LARGE SCREEN");
                break;
            default:
                break;
        }

        //int sizeOfActionBar = MainActivity.getThemeAttributeDimensionSize(getActivity(), R.attr.actionBarSize);

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
                   // bottomBanner.setVisibility(View.GONE);
                    //bMainLog.setVisibility(View.GONE);
                    //bMainRoll.setVisibility(View.GONE);
                    //bMainInfo.setVisibility(View.GONE);
                } else {
                   // bottomBanner.setVisibility(View.VISIBLE);
                    // Scroll up
                    //bMainRoll.setVisibility(View.VISIBLE);
                    //bMainLog.setVisibility(View.VISIBLE);
                    //bMainInfo.setVisibility(View.VISIBLE);
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

        volleyMainPosts = new VolleyMainPosts(getContext());

        // We first check for cached request
        Cache cache = AppController.getInstance().getRequestQueue().getCache();

        if (!UserLocalStore.isUserLoggedIn) {
            entry = cache.get("http://idex.site88.net/fetchUserPosts.php");
        } else {
            entry = cache.get("http://idex.site88.net/fetchLoggedInUserPosts.php?userID=" + userLocalStore.getLoggedInUser().getUserID());
        }

        if (UserLocalStore.allowRefresh) {
            UserLocalStore.allowRefresh = false;

            util.getInternetStatus(getContext(), new NetworkConnectionCallBack() {
                @Override
                public void networkConnection(boolean isConnected) {
                    // If user refreshes the page, load current updated JSON
                    if (isConnected) {
                        if (UserLocalStore.isUserLoggedIn) {
                            volleyMainPosts.getUserPostsLoggedIn(feedListAdapter, feedItems, spinner, userLocalStore.getLoggedInUser().getUserID());
                            //jsonParserFeed.getJsonLiveLoggedIn(URL_LoggedInUser);
                        } else {
                            volleyMainPosts.getUserPostsNotLoggedIn(feedListAdapter, feedItems, spinner);
                            //jsonParserFeed.getJsonLive(URL_TEST);
                        }
                    } else {
                        util.displayNoInternet(getContext());
                        volleyMainPosts.getUserPostsCache(feedListAdapter, feedItems, spinner, entry);
                        //jsonParserFeed.getJsonOffline(entry);
                    }
                }
            });
            // If user is not connected to the internet or if the user already load the page once
            // User cache so the page doesn't need to reload
        } else if ((UserLocalStore.visitCounter > 0)) {
            volleyMainPosts.getUserPostsCache(feedListAdapter, feedItems, spinner, entry);
            // Else if the user is connected to the internet, simple load current updated JSON
            // Ex. when user opens app for the first time
        } else {

            util.getInternetStatus(getContext(), new NetworkConnectionCallBack() {
                @Override
                public void networkConnection(boolean isConnected) {
                    if (isConnected) {
                        if (!UserLocalStore.isUserLoggedIn) {
                            volleyMainPosts.getUserPostsNotLoggedIn(feedListAdapter, feedItems, spinner);
                            //jsonParserFeed.getJsonLive(URL_TEST);
                        } else {
                            volleyMainPosts.getUserPostsLoggedIn(feedListAdapter, feedItems, spinner, userLocalStore.getLoggedInUser().getUserID());
                            //jsonParserFeed.getJsonLiveLoggedIn(URL_LoggedInUser);
                        }
                    } else {
                        volleyMainPosts.getUserPostsCache(feedListAdapter, feedItems, spinner, entry);
                        util.displayNoInternet(getContext());
                        //jsonParserFeed.getJsonOffline(entry);
                    }
                }
            });
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

                buttonMonitor.cancelClicks(bMainRoll, bMainLog, bMainInfo);
                buttonMonitor.setRollClicked();
                bMainRoll.setBackgroundResource(R.drawable.rolled);

                fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.rLayoutMain,
                        new PostListFragment()).commit();
                MainActivity.listView.setItemChecked(0, true);

                break;
            case R.id.bMainLog:
                /*
                if (userLocalStore.getLoggedInUser().getUsername().equals("")) {
                    notLoggedInMessage();
                } else {*/
                    buttonMonitor.cancelClicks(bMainRoll, bMainLog, bMainInfo);
                    buttonMonitor.setLogClicked();
                    bMainLog.setBackgroundResource(R.drawable.logged);

                    fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.rLayoutMain,
                            new DashFragment()).commit();
                //}
                break;
            case R.id.bMainInfo:
               /* if (userLocalStore.getLoggedInUser().getUsername().equals("")) {
                    notLoggedInMessage();
                } else {*/
                    buttonMonitor.cancelClicks(bMainRoll, bMainLog, bMainInfo);
                    buttonMonitor.setInfoClicked();
                    bMainInfo.setBackgroundResource(R.drawable.infoed);

                    fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.rLayoutMain,
                            new TabFragment()).commit();
                    MainActivity.listView.setItemChecked(1, true);
                //}
                break;

        }
    }

    @Override
    public void onRefresh() {
        refreshLayout.setRefreshing(false);
        util.getInternetStatus(getContext(), new NetworkConnectionCallBack() {
            @Override
            public void networkConnection(boolean isConnected) {
                if (isConnected) {
                    UserLocalStore.allowRefresh = true;
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.detach(PostListFragment.this).attach(PostListFragment.this).commit();
                    feedItems.clear();
                } else {
                    Util.displayNoInternet(getContext());
                }
            }
        });
    }





    // If user is not logged in, hide the widget. Otherwise, show the widget.
    private void hideWidget() {
        if (!UserLocalStore.isUserLoggedIn) {
            //postWidget.hide();
        }
    }

    private void notLoggedInMessage() {
        Toast.makeText(getContext(), "You are not logged in", Toast.LENGTH_SHORT).show();

    }

}