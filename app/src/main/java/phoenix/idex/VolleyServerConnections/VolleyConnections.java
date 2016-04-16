package phoenix.idex.VolleyServerConnections;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ProgressBar;

import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import phoenix.idex.Activities.MainActivity;
import phoenix.idex.Graphing.Graph;
import phoenix.idex.RecyclerViewFeed.CommentRecyclerView.adapter.CommentListAdapter;
import phoenix.idex.RecyclerViewFeed.CommentRecyclerView.data.CommentItem;
import phoenix.idex.RecyclerViewFeed.MainRecyclerView.adapter.FeedListAdapter;
import phoenix.idex.RecyclerViewFeed.MainRecyclerView.app.AppController;
import phoenix.idex.RecyclerViewFeed.MainRecyclerView.data.FeedItem;
import phoenix.idex.ServerRequestCallBacks.GraphInfoCallBack;
import phoenix.idex.User;
import phoenix.idex.UserLocalStore;

/**
 * Created by Ravinder on 4/12/16.
 */
public class VolleyConnections {

    private ProgressDialog progressDialog;
    private Context context;

    public VolleyConnections(Context context) {
        this.context = context;
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");
    }
    public void fetchCommentVolley(CommentListAdapter commentListAdapter, List<CommentItem> commentItems, int postID) {
        progressDialog.show();
        new FetchCommentVolley(commentListAdapter, commentItems, postID).fetchCommentVolley();
    }

    public void fetchAGraph(int postID, ProgressDialog progressDialog, GraphInfoCallBack graphInfoCallBack) {
        progressDialog.setMessage("Loading Graph...");
        new FetchGraphInfoVolley(postID, progressDialog, graphInfoCallBack).fetchGraphVolley();
    }
    public void storeAPostVolley(String post, int userID, String timeStamp) {
        progressDialog.show();
        new StoreAPostVolley(post, userID, timeStamp).storeAPost();
    }
    public void deleteAPostVolley(int postID) {
        progressDialog.setMessage("Deleting...");
        progressDialog.show();
        new DeletePostVolley(postID).deletePostVolley();
    }
    public void getUserPostsNotLoggedIn(FeedListAdapter feedListAdapter, List<FeedItem> feedItems, ProgressBar spinner) {
        new FetchUserPostsVolley(feedListAdapter, feedItems, spinner).getJsonLive();
    }
    public void getUserPostsLoggedIn(FeedListAdapter feedListAdapter, List<FeedItem> feedItems, ProgressBar spinner, int userID) {
        new FetchUserPostsVolley(feedListAdapter, feedItems, spinner).getJsonLiveLoggedIn(userID);
    }
    public void getUserPostsCache(FeedListAdapter feedListAdapter, List<FeedItem> feedItems, ProgressBar spinner, Cache.Entry cache) {
        new FetchUserPostsVolley(feedListAdapter, feedItems, spinner).getJsonOffline(cache);
    }
    public void getAUniqueUserPosts(FeedListAdapter feedListAdapter, List<FeedItem> feedItems, ProgressBar spinner, int userID) {
        new FetchUserPostsVolley(feedListAdapter, feedItems, spinner).getJsonLiveUniqueUser(userID);
    }
    public void updateUserInfo(User user) {
        progressDialog.show();
        new UpdateUserInfoVolley(user).updateUserInfo();
    }

    public class UpdateUserInfoVolley {
        private User user;

        public UpdateUserInfoVolley(User user) {
            this.user = user;
        }

        // Pull JSON directly from the PHP JSON result
        public void updateUserInfo() {

            // making fresh volley request and getting json
            StringRequest jsonReq = new StringRequest(Request.Method.POST,
                    "http://idex.site88.net/updateUserInfo.php", new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    progressDialog.dismiss();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    progressDialog.dismiss();
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("userID", Integer.toString(user.getUserID()));
                    params.put("firstName", user.getFirstname());
                    params.put("lastName", user.getLastname());
                    params.put("email", user.getEmail());
                    params.put("username", user.getUsername());
                    return params;
                }
            };
            // Adding request to volley request queue
            AppController.getInstance().addToRequestQueue(jsonReq);
        }
    }

    public class StoreAPostVolley {
        private String post;
        private int userID;
        private String timeStamp;

        public StoreAPostVolley(String post, int userID, String timeStamp) {
            this.post = post;
            this.userID = userID;
            this.timeStamp = timeStamp;
        }

        // Pull JSON directly from the PHP JSON result
        public void storeAPost() {

            // making fresh volley request and getting json
            StringRequest jsonReq = new StringRequest(Request.Method.POST,
                    "http://idex.site88.net/insertnewpost.php", new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    progressDialog.dismiss();
                    UserLocalStore.allowRefresh = true;
                    context.startActivity(new Intent(context, MainActivity.class));
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    progressDialog.dismiss();
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("post", post);
                    params.put("timeStamp", timeStamp);
                    params.put("userID", Integer.toString(userID));

                    return params;
                }
            };
            // Adding request to volley request queue
            AppController.getInstance().addToRequestQueue(jsonReq);
        }

    }

    public class DeletePostVolley {
        private int postID;

        public DeletePostVolley(int postID) {
            this.postID = postID;
        }

        // Pull JSON directly from the PHP JSON result
        public void deletePostVolley() {
            // making fresh volley request and getting json
            StringRequest jsonReq = new StringRequest(Request.Method.POST,
                    "http://idex.site88.net/deleteAPost.php", new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {
                    progressDialog.dismiss();
                    UserLocalStore.allowRefresh = true;
                    context.startActivity(new Intent(context, MainActivity.class));
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    progressDialog.dismiss();
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("postID", Integer.toString(postID));
                    return params;
                }
            };
            // Adding request to volley request queue
            AppController.getInstance().addToRequestQueue(jsonReq);
        }
    }

    public class FetchUserPostsVolley {
        private FeedListAdapter feedListAdapter;
        private List<FeedItem> feedItems;
        private ProgressBar spinner;

        public FetchUserPostsVolley(FeedListAdapter feedListAdapter, List<FeedItem> feedItems, ProgressBar spinner) {
            this.feedListAdapter = feedListAdapter;
            this.feedItems = feedItems;
            this.spinner = spinner;
        }

        // Populate FeedItem Array with JSONArray from PHP
        public void fetchUserPostsVolley(JSONObject response) {
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

                    String post = feedObj.getString("post");
                    post = post.replace("\\", "");
                    item.setStatus(post);
                    item.setProfilePic(feedObj.getString("userpic"));
                    item.setTimeStamp(feedObj.getString("timeStamp"));
                    item.setTotalFill(feedObj.getInt("totalFill"));
                    item.setTotalKill(feedObj.getInt("totalKill"));

                    try{
                        item.setFillOrKill(feedObj.getInt("status"));
                    } catch (JSONException e) {
                        item.setFillOrKill(-1); // Default value for posts user never clicked fill nor kill
                    }
                    feedItems.add(item);
                }
                // notify data changes to list adapater
                feedListAdapter.notifyDataSetChanged();
                spinner.setVisibility(View.GONE);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // Pull JSON directly from the PHP JSON result
        public void getJsonLive() {
            UserLocalStore.visitCounter++;
            // making fresh volley request and getting json
            JsonObjectRequest jsonReq = new JsonObjectRequest(Request.Method.GET,
                    "http://idex.site88.net/fetchUserPosts.php", null, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    if (response != null) {
                        fetchUserPostsVolley(response);
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

        // Pull JSON directly from the PHP JSON result
        public void getJsonLiveLoggedIn(int userID) {

            UserLocalStore.visitCounter++;
            // making fresh volley request and getting json
            JsonObjectRequest jsonReq = new JsonObjectRequest(Request.Method.GET,
                    "http://idex.site88.net/fetchLoggedInUserPosts.php?userID=" + userID, null, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    if (response != null) {
                        fetchUserPostsVolley(response);
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

        public void getJsonLiveUniqueUser(int userID) {

            UserLocalStore.visitCounter++;
            // making fresh volley request and getting json
            JsonObjectRequest jsonReq = new JsonObjectRequest(Request.Method.GET,
                    "http://idex.site88.net/fetchOneUserPosts.php?userID=" + userID, null, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    if (response != null) {
                        fetchUserPostsVolley(response);
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


        // Pull saved JSON in the cache.
        public void getJsonOffline(Cache.Entry entry){
            // fetch the data from cache if the user is offline
            try {
                String data = new String(entry.data, "UTF-8");
                try {
                    fetchUserPostsVolley(new JSONObject(data));
                    //parseJsonFeed(new JSONObject(data));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    public class FetchCommentVolley {
        private CommentListAdapter commentListAdapter;
        private List<CommentItem> commentItems;
        private int postID;

        public FetchCommentVolley(CommentListAdapter commentListAdapter, List<CommentItem> commentItems, int postID) {
            this.commentListAdapter = commentListAdapter;
            this.commentItems = commentItems;
            this.postID = postID;
        }

        // Pull JSON directly from the PHP JSON result
        public void fetchCommentVolley() {
            // making fresh volley request and getting json
            JsonObjectRequest jsonReq = new JsonObjectRequest(Request.Method.GET,
                    "http://idex.site88.net/fetchComments.php?postID=" + postID, null, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    if (response != null) {
                        try {
                            JSONArray feedArray = response.getJSONArray("feed");

                            for (int i = 0; i < feedArray.length(); i++) {
                                JSONObject feedObj = (JSONObject) feedArray.get(i);

                                CommentItem item = new CommentItem();
                                String name;
                                name = feedObj.getString("firstname") + " " + feedObj.getString("lastname");
                                item.setId(feedObj.getInt("postID"));
                                item.setName(name);
                                item.setUsername(feedObj.getString("username"));
                                item.setComment(feedObj.getString("comment"));
                                item.setProfilePic(feedObj.getString("userpic"));
                                item.setTimeStamp(feedObj.getString("date"));
                                commentItems.add(item);
                            }
                            // notify data changes to list adapater
                            commentListAdapter.notifyDataSetChanged();
                            //spinner.setVisibility(View.GONE);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        progressDialog.dismiss();
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    progressDialog.dismiss();
                }
            });
            // Adding request to volley request queue
            AppController.getInstance().addToRequestQueue(jsonReq);
        }
    }

    public class FetchGraphInfoVolley {
        private GraphInfoCallBack graphInfoCallBack;
        private int postID;
        private Graph aGraph;

        public FetchGraphInfoVolley(int postID, ProgressDialog progressDialog, GraphInfoCallBack graphInfoCallBack) {
            this.graphInfoCallBack = graphInfoCallBack;
            this.postID = postID;
            progressDialog.show();
        }

        // Pull JSON directly from the PHP JSON result
        public void fetchGraphVolley() {

            JsonObjectRequest jsonReq = new JsonObjectRequest(Request.Method.GET,
                    "http://idex.site88.net/fetchGraphInfo.php?postID=" + postID, null, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {

                    if (response != null) {
                        System.out.println("NOT NULL;jhk");

                        try {

                            JSONArray feedArray = response.getJSONArray("feed");

                            for (int i = 0; i < feedArray.length(); i++) {
                                JSONObject feedObj = (JSONObject) feedArray.get(i);

                                aGraph = new Graph();
                                aGraph.setTotalFillFloor(feedObj.getInt("totalFillFloor"));
                                aGraph.setTotalKillFloor(feedObj.getInt("totalKillFloor"));
                                aGraph.setValue(feedObj.getInt("value"));
                                aGraph.setCurrentColumn(feedObj.getInt("currentColumn"));
                                aGraph.setC1(feedObj.getInt("c1"));
                                aGraph.setC2(feedObj.getInt("c2"));
                                aGraph.setC3(feedObj.getInt("c3"));
                                aGraph.setC4(feedObj.getInt("c4"));
                                aGraph.setC5(feedObj.getInt("c5"));
                                aGraph.setC6(feedObj.getInt("c6"));
                                aGraph.setC7(feedObj.getInt("c7"));
                                aGraph.setC8(feedObj.getInt("c8"));
                                aGraph.setC9(feedObj.getInt("c9"));
                                aGraph.setC10(feedObj.getInt("c10"));
                                aGraph.setC11(feedObj.getInt("c11"));
                                aGraph.setC12(feedObj.getInt("c12"));
                                aGraph.setC13(feedObj.getInt("c13"));
                                aGraph.setC14(feedObj.getInt("c14"));
                                aGraph.setC15(feedObj.getInt("c15"));
                                aGraph.setC16(feedObj.getInt("c16"));
                                aGraph.setC17(feedObj.getInt("c17"));
                                aGraph.setC18(feedObj.getInt("c18"));
                                aGraph.setC19(feedObj.getInt("c19"));
                                aGraph.setC20(feedObj.getInt("c20"));
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        graphInfoCallBack.getGraphInfo(aGraph);
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
    }




}
