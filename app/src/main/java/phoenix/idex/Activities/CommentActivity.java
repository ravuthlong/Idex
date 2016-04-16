package phoenix.idex.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.daimajia.swipe.SwipeLayout;

import java.util.ArrayList;
import java.util.List;

import phoenix.idex.Fragments.PostListFragment;
import phoenix.idex.VolleyServerConnections.VolleyConnections;
import phoenix.idex.R;
import phoenix.idex.RecyclerViewFeed.CommentRecyclerView.adapter.CommentListAdapter;
import phoenix.idex.RecyclerViewFeed.CommentRecyclerView.data.CommentItem;
import phoenix.idex.RecyclerViewFeed.MainRecyclerView.app.AppController;
import phoenix.idex.ServerConnections.CommentServerRequests;
import phoenix.idex.ServerRequestCallBacks.NetworkConnectionCallBack;
import phoenix.idex.UserLocalStore;

public class CommentActivity extends Activity implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {
    SwipeLayout swipeLayout;
    NetworkImageView profilePic;
    ImageButton imgbFill, imgbKill;
    TextView tvGraph, txtStatusMsg, timestamp, name, tvManagePost, tvFillNum, tvKillNum,
            tvEditPost;
    private ImageLoader imageLoader = AppController.getInstance().getImageLoader();
    private static final String TAG = PostListFragment.class.getSimpleName();
    //private RecyclerView commentRecyclerView;
    private List<CommentItem> commentItems;
    private CommentListAdapter commentListAdapter;
    private SwipeRefreshLayout refreshLayoutComment;
    private ProgressBar spinner;
    private Cache.Entry entry;
    private UserLocalStore userLocalStore;
    private String URL_Comment;
    private int postID;
    private EditText etComment;
    private Button bComment;
    private CommentServerRequests commentServerRequests;
    private RecyclerView commentRecyclerView;
    String receivedName, receivedTime, receivedPost, receivedProfilePic;
    int receivedNumFill, receivedNumKill;
    private Bundle savedIntent;
    private ProgressDialog progressDialog;
    private VolleyConnections volleyConnections;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        swipeLayout = (SwipeLayout) findViewById(R.id.swipe);
        tvGraph = (TextView) findViewById(R.id.tvGraph);
        tvManagePost = (TextView) findViewById(R.id.tvManagePost);
        profilePic = (NetworkImageView) findViewById(R.id.profilePic);
        name = (TextView) findViewById(R.id.name);
        timestamp = (TextView) findViewById(R.id.timestamp);
        txtStatusMsg = (TextView) findViewById(R.id.txtStatusMsg);
        imgbFill = (ImageButton) findViewById(R.id.imgbFill);
        imgbKill = (ImageButton) findViewById(R.id.imgbKill);
        tvFillNum = (TextView) findViewById(R.id.tvFillNum);
        tvKillNum = (TextView) findViewById(R.id.tvKillNum);
        tvEditPost = (TextView) findViewById(R.id.tvEditPost);
        etComment = (EditText) findViewById(R.id.etComment);
        bComment = (Button) findViewById(R.id.bComment);
        commentRecyclerView = (RecyclerView) findViewById(R.id.commentRecyclerView);

        bComment.setOnClickListener(this);
        userLocalStore = new UserLocalStore(this);
        commentServerRequests = new CommentServerRequests(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        volleyConnections = new VolleyConnections(this);

        setUpPostToComment();


        //refreshLayoutComment = (SwipeRefreshLayout) findViewById(R.id.refreshLayoutComment);
        //spinner = (ProgressBar) findViewById(R.id.progress_barComment);
        //spinner.setVisibility(View.VISIBLE);
       // commentRecyclerView = (RecyclerView) findViewById(R.id.commentRecyclerView);
        commentItems = new ArrayList<>();
        commentListAdapter = new CommentListAdapter(this, commentItems);
        commentRecyclerView.setHasFixedSize(true);
        commentRecyclerView.setAdapter(commentListAdapter);
        commentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        //refreshLayoutComment.setOnRefreshListener(this);

        volleyConnections.fetchCommentVolley(commentListAdapter, commentItems, postID);

        // We first check for cached request
        Cache cache = AppController.getInstance().getRequestQueue().getCache();
        entry = cache.get(URL_Comment);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    public void setUpPostToComment() {

        Bundle postInfo = getIntent().getExtras();

        if (postInfo != null) {
            receivedName = postInfo.getString("name");
            receivedTime = postInfo.getString("time");
            receivedPost = postInfo.getString("post");
            receivedNumFill = postInfo.getInt("numFill");
            receivedNumKill = postInfo.getInt("numKill");
            receivedProfilePic = postInfo.getString("profilePic");
            postID = postInfo.getInt("postID");
        } else {
            savedIntent = getIntent().getExtras();
            receivedName = savedIntent.getString("name");
            receivedTime = savedIntent.getString("time");
            receivedPost = savedIntent.getString("post");
            receivedNumFill = savedIntent.getInt("numFill");
            receivedNumKill = savedIntent.getInt("numKill");
            receivedProfilePic = savedIntent.getString("profilePic");
            postID = savedIntent.getInt("postID");
        }

        // Converting timestamp into x ago format
        CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
                Long.parseLong(receivedTime),
                System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);

        imageLoader = AppController.getInstance().getImageLoader();
        profilePic.setImageUrl(receivedProfilePic, imageLoader);

        name.setText(receivedName);
        timestamp.setText(timeAgo);
        txtStatusMsg.setText(receivedPost);
        tvKillNum.setText("-" + receivedNumKill);
        tvFillNum.setText("" + receivedNumFill);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bComment:
                String unixTime = Long.toString(System.currentTimeMillis());
                int userID = userLocalStore.getLoggedInUser().getUserID();
                String comment = etComment.getText().toString();

                commentServerRequests.storeACommentInBackground(postID, userID, comment, unixTime);

                Intent newIntent = new Intent(this, CommentActivity.class);
                newIntent.putExtra("name", receivedName);
                newIntent.putExtra("time", receivedTime);
                newIntent.putExtra("numKill", receivedNumKill);
                newIntent.putExtra("numFill", receivedNumKill);
                newIntent.putExtra("post", receivedPost);
                newIntent.putExtra("profilePic", receivedProfilePic);
                newIntent.putExtra("postID", postID);

                startActivity(newIntent);
                finish();
                break;
        }
    }

    /*
    // Pull JSON directly from the PHP JSON result
    private void getJsonLive() {

        progressDialog.show();
        // making fresh volley request and getting json
        JsonObjectRequest jsonReq = new JsonObjectRequest(Request.Method.GET,
                URL_Comment, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                VolleyLog.d(TAG, "Response: " + response.toString());
                if (response != null) {
                    jsonParserComment.parseJsonFeed(response);
                    progressDialog.dismiss();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                VolleyLog.d(TAG, "Error: " + error.getMessage());
            }
        });
        // Adding request to volley request queue
        AppController.getInstance().addToRequestQueue(jsonReq);
    }*/


    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshLayoutComment.setRefreshing(false);
                new PostListFragment.InternetAccess(CommentActivity.this, new NetworkConnectionCallBack() {
                    @Override
                    public void networkConnection(boolean isConnected) {
                        if (isConnected) {
                            UserLocalStore.allowRefresh = true;
                            startActivity(new Intent(CommentActivity.this, CommentActivity.class));
                            commentItems.clear();
                        } else {
                            displayNoInternet(CommentActivity.this);
                        }
                    }
                }).execute();
            }
        }, 200);
    }

    public  static void displayNoInternet(Context context) {
        Toast toast= Toast.makeText(context,
                "Not connected to the internet", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.show();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if(outState != null) {
            outState.putString("name", receivedName);
            outState.putString("time", receivedTime);
            outState.putInt("numKill", receivedNumKill);
            outState.putInt("numFill", receivedNumKill);
            outState.putString("post", receivedPost);
            outState.putString("profilePic", receivedProfilePic);
            outState.putInt("postID", postID);
            savedIntent = outState;
        }
    }
}
