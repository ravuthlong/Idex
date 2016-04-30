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
import phoenix.idex.RecyclerViewFeed.MainRecyclerView.adapter.FeedListAdapter;
import phoenix.idex.RecyclerViewFeed.MainRecyclerView.app.AppController;
import phoenix.idex.RecyclerViewFeed.MainRecyclerView.data.FeedItem;
import phoenix.idex.ServerRequestCallBacks.GraphInfoCallBack;
import phoenix.idex.UserLocalStore;

/**
 * Created by Ravinder on 4/12/16.
 */
public class VolleyMainPosts {

    private ProgressDialog progressDialog;
    private Context context;

    public VolleyMainPosts(Context context) {
        this.context = context;
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");
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
                spinner.setVisibility(View.GONE);
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
                    } else {
                        spinner.setVisibility(View.GONE);
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    spinner.setVisibility(View.GONE);
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

                        try {

                            JSONArray feedArray = response.getJSONArray("feed");

                            for (int i = 0; i < feedArray.length(); i++) {
                                JSONObject feedObj = (JSONObject) feedArray.get(i);

                                aGraph = new Graph();
                                aGraph.setTotalFillFloor(feedObj.getInt("totalFillFloor"));
                                aGraph.setTotalKillFloor(feedObj.getInt("totalKillFloor"));
                                aGraph.setValue(feedObj.getInt("value"));
                                aGraph.setCurrentColumn(feedObj.getInt("currentColumn"));

                                int[] columns = new int[100];
                                columns[0] = feedObj.getInt("c1");
                                columns[1] = feedObj.getInt("c2");
                                columns[2] = feedObj.getInt("c3");
                                columns[3] = feedObj.getInt("c4");
                                columns[4] = feedObj.getInt("c5");
                                columns[5] = feedObj.getInt("c6");
                                columns[6] = feedObj.getInt("c7");
                                columns[7] = feedObj.getInt("c8");
                                columns[8] = feedObj.getInt("c9");
                                columns[9] = feedObj.getInt("c10");
                                columns[10] = feedObj.getInt("c11");
                                columns[11] = feedObj.getInt("c12");
                                columns[12] = feedObj.getInt("c13");
                                columns[13] = feedObj.getInt("c14");
                                columns[14] = feedObj.getInt("c15");
                                columns[15] = feedObj.getInt("c16");
                                columns[16] = feedObj.getInt("c17");
                                columns[17] = feedObj.getInt("c18");
                                columns[18] = feedObj.getInt("c19");
                                columns[19] = feedObj.getInt("c20");
                                columns[20] = feedObj.getInt("c21");
                                columns[21] = feedObj.getInt("c22");
                                columns[22] = feedObj.getInt("c23");
                                columns[23] = feedObj.getInt("c24");
                                columns[24] = feedObj.getInt("c25");
                                columns[25] = feedObj.getInt("c26");
                                columns[26] = feedObj.getInt("c27");
                                columns[27] = feedObj.getInt("c28");
                                columns[28] = feedObj.getInt("c29");
                                columns[29] = feedObj.getInt("c30");
                                columns[30] = feedObj.getInt("c31");
                                columns[31] = feedObj.getInt("c32");
                                columns[32] = feedObj.getInt("c33");
                                columns[33] = feedObj.getInt("c34");
                                columns[34] = feedObj.getInt("c35");
                                columns[35] = feedObj.getInt("c36");
                                columns[36] = feedObj.getInt("c37");
                                columns[37] = feedObj.getInt("c38");
                                columns[38] = feedObj.getInt("c39");
                                columns[39] = feedObj.getInt("c40");
                                columns[40] = feedObj.getInt("c41");
                                columns[41] = feedObj.getInt("c42");
                                columns[42] = feedObj.getInt("c43");
                                columns[43] = feedObj.getInt("c44");
                                columns[44] = feedObj.getInt("c45");
                                columns[45] = feedObj.getInt("c46");
                                columns[46] = feedObj.getInt("c47");
                                columns[47] = feedObj.getInt("c48");
                                columns[48] = feedObj.getInt("c49");
                                columns[49] = feedObj.getInt("c50");
                                columns[50] = feedObj.getInt("c51");
                                columns[51] = feedObj.getInt("c52");
                                columns[52] = feedObj.getInt("c53");
                                columns[53] = feedObj.getInt("c54");
                                columns[54] = feedObj.getInt("c55");
                                columns[55] = feedObj.getInt("c56");
                                columns[56] = feedObj.getInt("c57");
                                columns[57] = feedObj.getInt("c58");
                                columns[58] = feedObj.getInt("c59");
                                columns[59] = feedObj.getInt("c60");
                                columns[60] = feedObj.getInt("c61");
                                columns[61] = feedObj.getInt("c62");
                                columns[62] = feedObj.getInt("c63");
                                columns[63] = feedObj.getInt("c64");
                                columns[64] = feedObj.getInt("c65");
                                columns[65] = feedObj.getInt("c66");
                                columns[66] = feedObj.getInt("c67");
                                columns[67] = feedObj.getInt("c68");
                                columns[68] = feedObj.getInt("c69");
                                columns[69] = feedObj.getInt("c70");
                                columns[70] = feedObj.getInt("c71");
                                columns[71] = feedObj.getInt("c72");
                                columns[72] = feedObj.getInt("c73");
                                columns[73] = feedObj.getInt("c74");
                                columns[74] = feedObj.getInt("c75");
                                columns[75] = feedObj.getInt("c76");
                                columns[76] = feedObj.getInt("c77");
                                columns[77] = feedObj.getInt("c78");
                                columns[78] = feedObj.getInt("c79");
                                columns[79] = feedObj.getInt("c80");
                                columns[80] = feedObj.getInt("c81");
                                columns[81] = feedObj.getInt("c82");
                                columns[82] = feedObj.getInt("c83");
                                columns[83] = feedObj.getInt("c84");
                                columns[84] = feedObj.getInt("c85");
                                columns[85] = feedObj.getInt("c86");
                                columns[86] = feedObj.getInt("c87");
                                columns[87] = feedObj.getInt("c88");
                                columns[88] = feedObj.getInt("c89");
                                columns[89] = feedObj.getInt("c90");
                                columns[90] = feedObj.getInt("c91");
                                columns[91] = feedObj.getInt("c92");
                                columns[92] = feedObj.getInt("c93");
                                columns[93] = feedObj.getInt("c94");
                                columns[94] = feedObj.getInt("c95");
                                columns[95] = feedObj.getInt("c96");
                                columns[96] = feedObj.getInt("c97");
                                columns[97] = feedObj.getInt("c98");
                                columns[98] = feedObj.getInt("c99");
                                columns[99] = feedObj.getInt("c100");

                                aGraph.setColumns(columns);
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
