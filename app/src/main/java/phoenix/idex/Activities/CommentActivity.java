package phoenix.idex.Activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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
import phoenix.idex.R;
import phoenix.idex.RecyclerViewFeed.CommentRecyclerView.adapter.CommentListAdapter;
import phoenix.idex.RecyclerViewFeed.CommentRecyclerView.data.CommentItem;
import phoenix.idex.RecyclerViewFeed.MainRecyclerView.app.AppController;
import phoenix.idex.ServerRequestCallBacks.NetworkConnectionCallBack;
import phoenix.idex.ServerRequestCallBacks.PostExecutionCallBack;
import phoenix.idex.UserLocalStore;
import phoenix.idex.Util;
import phoenix.idex.VolleyServerConnections.VolleyComments;

public class CommentActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {
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
    private RecyclerView commentRecyclerView;
    String receivedName, receivedTime, receivedPost, receivedProfilePic;
    int receivedNumFill, receivedNumKill;
    private Bundle savedIntent;
    private ProgressDialog progressDialog;
    private VolleyComments volleyComments;
    Util util = Util.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
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
        refreshLayoutComment = (SwipeRefreshLayout) findViewById(R.id.refreshLayoutComment);


        refreshLayoutComment.setOnRefreshListener(this);
        bComment.setOnClickListener(this);
        userLocalStore = new UserLocalStore(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        volleyComments = new VolleyComments(this);

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

        volleyComments.fetchCommentVolley(commentListAdapter, commentItems, postID, userLocalStore.getLoggedInUser().getUserID());

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


            Bundle outState = new Bundle();
            outState.putString("name", receivedName);
            outState.putString("time", receivedTime);
            outState.putInt("numKill", receivedNumKill);
            outState.putInt("numFill", receivedNumKill);
            outState.putString("post", receivedPost);
            outState.putString("profilePic", receivedProfilePic);
            outState.putInt("postID", postID);
            savedIntent = outState;
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

                volleyComments.storeAComment(postID, userID, comment, unixTime, new PostExecutionCallBack() {
                    @Override
                    public void postExecution() {
                        Intent newIntent = new Intent(CommentActivity.this, CommentActivity.class);
                        newIntent.putExtra("name", receivedName);
                        newIntent.putExtra("time", receivedTime);
                        newIntent.putExtra("numKill", receivedNumKill);
                        newIntent.putExtra("numFill", receivedNumKill);
                        newIntent.putExtra("post", receivedPost);
                        newIntent.putExtra("profilePic", receivedProfilePic);
                        newIntent.putExtra("postID", postID);

                        startActivity(newIntent);
                        finish();
                    }
                });


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

        util.getInternetStatus(CommentActivity.this, new NetworkConnectionCallBack() {
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
        });
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

        System.out.println("SAVED STATE REACHED");
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
