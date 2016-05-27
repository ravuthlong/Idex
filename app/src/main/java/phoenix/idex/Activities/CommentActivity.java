package phoenix.idex.Activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.daimajia.swipe.SwipeLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import phoenix.idex.Fragments.PostListFragment;
import phoenix.idex.Graphing.GraphActivity;
import phoenix.idex.R;
import phoenix.idex.RecyclerViewFeed.CommentRecyclerView.adapter.CommentListAdapter;
import phoenix.idex.RecyclerViewFeed.CommentRecyclerView.data.CommentItem;
import phoenix.idex.RecyclerViewFeed.MainRecyclerView.app.AppController;
import phoenix.idex.RecyclerViewFeed.MainRecyclerView.data.FeedItem;
import phoenix.idex.ServerConnections.ServerRequests;
import phoenix.idex.ServerRequestCallBacks.JSONObjectCallBack;
import phoenix.idex.ServerRequestCallBacks.NetworkConnectionCallBack;
import phoenix.idex.ServerRequestCallBacks.PostExecutionCallBack;
import phoenix.idex.SoundPlayer;
import phoenix.idex.UserLocalStore;
import phoenix.idex.Util;
import phoenix.idex.VolleyServerConnections.VolleyComments;
import phoenix.idex.VolleyServerConnections.VolleyMainPosts;

public class CommentActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {
    private SwipeLayout swipeLayout;
    private NetworkImageView profilePic;
    private ImageButton imgbFill, imgbKill;
    private TextView tvGraph, txtStatusMsg, timestamp, name, tvManagePost, tvFillNum, tvKillNum,
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
    private String URL_Comment, receivedUsername;
    private EditText etComment;
    private Button bComment;
    private RecyclerView commentRecyclerView;
    private LinearLayout layoutNoComment;
    private String receivedName, receivedTime, receivedPost, receivedProfilePic;
    private int receivedNumFill, receivedNumKill, userID, postID, fillOrKill;
    private Bundle savedIntent;
    private ProgressDialog progressDialog;
    private VolleyComments volleyComments;
    private Util util = Util.getInstance();
    private SoundPlayer fillSound, killSound;
    private ServerRequests serverRequests;
    private LinearLayout bottomWrapper1;
    private AlertDialog.Builder editPostDialog;
    private VolleyMainPosts volleyMainPosts;
    Bundle postInfo;
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        SharedPreferences sharedPref = getSharedPreferences("mySettings", MODE_PRIVATE);
        // Initialize fill and kill sounds
        fillSound = new SoundPlayer(this, R.raw.fillsound);
        killSound = new SoundPlayer(this, R.raw.killsound);

        userID = sharedPref.getInt("userID", 0);
        postID = sharedPref.getInt("postID", 0);
        fillOrKill = sharedPref.getInt("clickStatus", 0);
        receivedUsername = sharedPref.getString("username", "");


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
        bottomWrapper1 = (LinearLayout) findViewById(R.id.bottom_wrapper1);
        layoutNoComment = (LinearLayout) findViewById(R.id.layoutNoComment);

        name.setTextColor(ContextCompat.getColor(this, R.color.font));
        txtStatusMsg.setTextColor(ContextCompat.getColor(this, R.color.font));
        timestamp.setTextColor(ContextCompat.getColor(this, R.color.font));
        tvFillNum.setTextColor(ContextCompat.getColor(this, R.color.font));
        tvKillNum.setTextColor(ContextCompat.getColor(this, R.color.font));

        imgbFill.setOnClickListener(this);
        imgbKill.setOnClickListener(this);
        tvGraph.setOnClickListener(this);
        tvEditPost.setOnClickListener(this);
        tvManagePost.setOnClickListener(this);


        volleyMainPosts = new VolleyMainPosts(this);
        serverRequests = new ServerRequests(this);
        refreshLayoutComment.setOnRefreshListener(this);
        bComment.setOnClickListener(this);
        userLocalStore = new UserLocalStore(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        volleyComments = new VolleyComments(this);


        swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);
        // Drag from left
        swipeLayout.addDrag(SwipeLayout.DragEdge.Left, swipeLayout.findViewById(R.id.bottom_wrapper1));
        // Drag from right
        swipeLayout.addDrag(SwipeLayout.DragEdge.Right, swipeLayout.findViewById(R.id.bottom_wrapper));


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

        setUpPostToComment();

        // Set up the button to filled or killed icon based on data
        if (fillOrKill == 0) {
            imgbKill.setBackgroundResource(R.drawable.killed);
        } else if (fillOrKill == 1) {
            imgbFill.setBackgroundResource(R.drawable.filled);
        }
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

        postInfo = getIntent().getExtras();

        if (postInfo != null) {
            // User click on the post, bundle extra was sent to the top post of comment
            userID = postInfo.getInt("userID");
            receivedName = postInfo.getString("name");
            receivedTime = postInfo.getString("time");
            receivedPost = postInfo.getString("post");
            receivedNumFill = postInfo.getInt("numFill");
            receivedNumKill = postInfo.getInt("numKill");
            receivedProfilePic = postInfo.getString("profilePic");
            postID = postInfo.getInt("postID");
            fillOrKill = postInfo.getInt("clickStatus");
            receivedUsername = postInfo.getString("username");
            fillOrKill = postInfo.getInt("fillOrkill");

            // Converting timestamp into x ago format
            CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
                    Long.parseLong(receivedTime),
                    System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
            imageLoader = AppController.getInstance().getImageLoader();
            profilePic.setImageUrl(receivedProfilePic, imageLoader);
            name.setText(receivedName);
            timestamp.setText(timeAgo);
            txtStatusMsg.setText(receivedPost);

            if (receivedNumKill >= 100) {
                tvKillNum.setTextSize(TypedValue.COMPLEX_UNIT_PX, tvFillNum.getTextSize() / (float) 1.2);
            }
            if (receivedNumFill >= 100) {
                tvFillNum.setTextSize(TypedValue.COMPLEX_UNIT_PX, tvFillNum.getTextSize() / (float) 1.2);
            }

            tvKillNum.setText("-" + receivedNumKill);
            tvFillNum.setText("" + receivedNumFill);

            postInfo = null;
            setSharedPreference();
            volleyComments.fetchCommentVolley(layoutNoComment, commentRecyclerView, commentListAdapter,
                    commentItems, postID, userLocalStore.getLoggedInUser().getUserID());
        } else {

            volleyComments.fetchAUniquePost(userID, postID, new JSONObjectCallBack() {
                @Override
                public void returnedJSONObject(JSONObject jsonObject) {
                    try {
                        FeedItem item = new FeedItem();
                        JSONArray feedArray = jsonObject.getJSONArray("feed");
                        for (int i = 0; i < feedArray.length(); i++) {
                            JSONObject feedObj = (JSONObject) feedArray.get(i);

                            receivedName = feedObj.getString("firstname") + " " + feedObj.getString("lastname");
                            item.setId(feedObj.getInt("postID"));
                            item.setName(receivedName);
                            item.setUsername(feedObj.getString("username"));
                            String post = feedObj.getString("post");
                            post = post.replace("\\", "");
                            item.setStatus(post);
                            item.setProfilePic(feedObj.getString("userpic"));
                            item.setTimeStamp(feedObj.getString("timeStamp"));
                            item.setTotalFill(feedObj.getInt("totalFill"));
                            item.setTotalKill(feedObj.getInt("totalKill"));

                            receivedNumKill = feedObj.getInt("totalKill");
                            receivedNumFill = feedObj.getInt("totalFill");
                            postID = feedObj.getInt("postID");
                            receivedPost = feedObj.getString("post");


                            try {
                                item.setFillOrKill(feedObj.getInt("status"));
                            } catch (JSONException e) {
                                item.setFillOrKill(-1); // Default value for posts user never clicked fill nor kill
                            }

                            // Converting timestamp into x ago format
                            CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
                                    Long.parseLong(item.getTimeStamp()),
                                    System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
                            imageLoader = AppController.getInstance().getImageLoader();
                            profilePic.setImageUrl(item.getProfilePic(), imageLoader);
                            name.setText(item.getName());
                            timestamp.setText(timeAgo);
                            txtStatusMsg.setText(item.getStatus());
                            tvKillNum.setText("-" + item.getTotalKill());
                            tvFillNum.setText("" + item.getTotalFill());

                            //feedItems.add(item);
                        }
                        volleyComments.fetchCommentVolley(layoutNoComment, commentRecyclerView, commentListAdapter,
                                commentItems, postID, userLocalStore.getLoggedInUser().getUserID());
                    } catch (JSONException e) {
                        //spinner.setVisibility(View.GONE);
                        e.printStackTrace();
                    }
                }
            });
        }

        if (receivedUsername.equals(userLocalStore.getLoggedInUser().getUsername())) {
            tvManagePost.setText("Delete");
            tvEditPost.setVisibility(View.VISIBLE);
        } else {
            bottomWrapper1.setWeightSum(1);
            tvManagePost.setText("Report");
        }
        if (receivedUsername.equals(userLocalStore.getLoggedInUser().getUsername())) {
            tvManagePost.setText("Delete");
            tvEditPost.setVisibility(View.VISIBLE);
        } else {
            bottomWrapper1.setWeightSum(1);
            tvManagePost.setText("Report");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvManagePost:
                if (tvManagePost.getText().toString().equals("Delete")) {
                    volleyMainPosts.deleteAPostVolley(postID);
                } else {
                    Toast.makeText(v.getContext(), "Clicked on Report ", Toast.LENGTH_SHORT).show();

                    // LATER.... push notify reported person
                }
                break;
            case R.id.tvEditPost:
                editPostDialog = new AlertDialog.Builder(this);
                final EditText edittext = new EditText(this);
                edittext.setText(receivedPost);

                //editPostDialog.setMessage("Enter Your Message");
                editPostDialog.setTitle("Edit Post");
                editPostDialog.setView(edittext);

                editPostDialog.setPositiveButton("Save Changes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        volleyMainPosts.updateAPost(postID, edittext.getText().toString(), new PostExecutionCallBack() {
                            @Override
                            public void postExecution() {
                                // Refresh the comment page
                                startActivity(new Intent(CommentActivity.this, CommentActivity.class));
                            }
                        });
                        setSharedPreference();
                    }
                });

                editPostDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // what ever
                    }
                });

                editPostDialog.show();
                break;
            case R.id.tvGraph:
                Intent intent = new Intent(this, GraphActivity.class);
                intent.putExtra("postID", postID);
                startActivity(intent);
                break;
            case R.id.imgbFill:
                fillSound.playSound();

                PostListFragment.allowRefreshFromBackButton = true;
                if (userLocalStore.getLoggedInUser().getUsername().equals("dge93") ||
                        userLocalStore.getLoggedInUser().getUsername().equals("a") ||
                        userLocalStore.getLoggedInUser().getUsername().equals("sealcub22")) {

                    // There's no fill so do normal operations
                    serverRequests.updateFillAndFillColumnInBackground(postID);
                    receivedNumFill++;
                    tvFillNum.setText("" + (receivedNumFill));

                } else {
                    if (fillOrKill == 1) {
                        System.out.println("HAS FILL CANCEL FILL");

                        // There's a fill already so cancel fill
                        serverRequests.minusFillInBackground(postID);
                        serverRequests.cancelClickInBackground(userLocalStore.getLoggedInUser().getUserID(), postID);
                        receivedNumFill--;
                        fillOrKill = -1;
                        imgbFill.setBackgroundResource(R.drawable.fill);
                    } else {

                        if (fillOrKill == 0) {
                            serverRequests.updateFillAndFillColumnInBackground(postID);
                            serverRequests.minusKillInBackground(postID);

                            // Case the post already has a Kill. Replace the kill with fill. Switching option.
                            receivedNumKill--;
                            tvKillNum.setText("-" + receivedNumKill);
                            serverRequests.cancelClickInBackground(userLocalStore.getLoggedInUser().getUserID(),postID);
                            //serverRequests.updateFillAndCancelKillInBackground(currentPos.getId(), currentPos.getCurrentColumn());
                            imgbKill.setBackgroundResource(R.drawable.kill);

                        } else {
                            // There's no fill so do normal operations
                            serverRequests.updateFillAndFillColumnInBackground(postID);
                        }

                        // Add to user list of Fill clicks database table
                        serverRequests.addToFillListInBackground(userLocalStore.getLoggedInUser().getUserID(), postID);

                        imgbFill.setBackgroundResource(R.drawable.filled);
                        receivedNumFill++;
                        fillOrKill = 1;
                    }
                    tvFillNum.setText("" + (receivedNumFill));
                }
                break;

            case R.id.imgbKill:

                PostListFragment.allowRefreshFromBackButton = true;
                killSound.playSound();

                if (userLocalStore.getLoggedInUser().getUsername().equals("dge93") ||
                        userLocalStore.getLoggedInUser().getUsername().equals("a") ||
                        userLocalStore.getLoggedInUser().getUsername().equals("sealcub22")) {

                    //update fill only. change name
                    serverRequests.updateKillAndKillColumnInBackground(postID);
                    receivedNumKill++;
                    tvKillNum.setText("-" + receivedNumKill);

                }  else {
                    if (fillOrKill == 0) {

                        // There's a kill already so cancel kill
                        serverRequests.minusKillInBackground(postID);
                        serverRequests.cancelClickInBackground(userLocalStore.getLoggedInUser().getUserID(), postID);
                        receivedNumKill--;
                        fillOrKill = -1;
                        imgbKill.setBackgroundResource(R.drawable.kill);
                    } else {

                        if (fillOrKill == 1) {

                            serverRequests.updateKillAndKillColumnInBackground(postID);
                            serverRequests.minusFillInBackground(postID);

                            // Case the post already has a Kill. Replace the kill with fill. Switching option.
                            receivedNumFill--;
                            tvFillNum.setText("" + receivedNumFill);

                            serverRequests.cancelClickInBackground(userLocalStore.getLoggedInUser().getUserID(), postID);
                            //serverRequests.updateKillAndCancelFillInBackground(currentPos.getId(), currentPos.getCurrentColumn());
                            imgbFill.setBackgroundResource(R.drawable.fill);

                        } else {
                            // There's no fill so do normal operations
                            //update fill only. change name
                            serverRequests.updateKillAndKillColumnInBackground(postID);
                        }

                        // Add to user list of Fill clicks database table
                        serverRequests.addToKillListInBackground(userLocalStore.getLoggedInUser().getUserID(), postID);
                        imgbKill.setBackgroundResource(R.drawable.killed);
                        receivedNumKill++;
                        fillOrKill = 0;
                    }
                }
               tvKillNum.setText("-" + receivedNumKill);

                break;
            case R.id.bComment:
                String unixTime = Long.toString(System.currentTimeMillis());
                int userID = userLocalStore.getLoggedInUser().getUserID();
                String comment = etComment.getText().toString();

                volleyComments.storeAComment(postID, userID, comment, unixTime, new PostExecutionCallBack() {
                    @Override
                    public void postExecution() {
                        setSharedPreference();
                        Intent newIntent = new Intent(CommentActivity.this, CommentActivity.class);
                        startActivity(newIntent);
                        //finish();
                    }
                });
                break;
        }
    }

    @Override
    public void onRefresh() {

        util.getInternetStatus(CommentActivity.this, new NetworkConnectionCallBack() {
            @Override
            public void networkConnection(boolean isConnected) {
                if (isConnected) {
                    UserLocalStore.allowRefresh = true;
                    setSharedPreference();
                    startActivity(new Intent(CommentActivity.this, CommentActivity.class));
                    //commentItems.clear();
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

    private void setSharedPreference() {
        sharedPref = getSharedPreferences("mySettings", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("userID", userID);
        editor.putInt("postID", postID);
        editor.putInt("clickStatus", fillOrKill);
        editor.putString("username", receivedUsername);
        editor.putString("receivedPost", receivedPost);
        editor.apply();
    }
}
