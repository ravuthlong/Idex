package phoenix.idex.VolleyServerConnections;

import android.app.ProgressDialog;
import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import phoenix.idex.RecyclerViewFeed.CommentRecyclerView.adapter.CommentListAdapter;
import phoenix.idex.RecyclerViewFeed.CommentRecyclerView.data.CommentItem;
import phoenix.idex.RecyclerViewFeed.MainRecyclerView.app.AppController;
import phoenix.idex.ServerRequestCallBacks.PostExecutionCallBack;

/**
 * Created by Ravinder on 4/21/16.
 */
public class VolleyComments {
    private ProgressDialog progressDialog;
    private Context context;

    public VolleyComments(Context context) {
        this.context = context;
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");
    }

    public void fetchCommentVolley(CommentListAdapter commentListAdapter, List<CommentItem> commentItems, int postID, int userID) {
        progressDialog.show();
        new FetchCommentVolley(commentListAdapter, commentItems, postID, userID).fetchCommentVolley();
    }
    public void updateRecommend(int commentID) {
        new UpdateRecommendVolley(commentID).updateRecommend();
    }
    public void addToUserCommentList(int commentID, int userID) {
        new AddToCommentList(commentID, userID).addToCommentList();
    }
    public void removeFromRecommendedList(int commentID, int userID) {
        new RemoveRecommendVolley(commentID, userID).removeRecommend();
    }
    public void updateMinusARecommendCount(int commentID) {
        new UpdateMinusRecommend(commentID).updateMinusARecommendCount();
    }
    public void storeAComment(int postID, int userID, String comment, String timeStamp, PostExecutionCallBack postExecutionCallBack) {
        new StoreAComment(postID, userID, comment, timeStamp, postExecutionCallBack).storeAComment();
    }

    public class StoreAComment {
        private int postID, userID;
        private String comment, timeStamp;
        private PostExecutionCallBack postExecutionCallBack;

        public StoreAComment(int postID, int userID, String comment, String timeStamp, PostExecutionCallBack postExecutionCallBack) {
            this.postID = postID;
            this.userID = userID;
            this.comment = comment;
            this.timeStamp = timeStamp;
            this.postExecutionCallBack = postExecutionCallBack;
        }

        // Pull JSON directly from the PHP JSON result
        public void storeAComment() {

            // making fresh volley request and getting json
            StringRequest jsonReq = new StringRequest(Request.Method.POST,
                    "http://idex.site88.net/insertNewComment.php", new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    postExecutionCallBack.postExecution();
                    //progressDialog.dismiss();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //progressDialog.dismiss();
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("postID", Integer.toString(postID));
                    params.put("userID", Integer.toString(userID));
                    params.put("comment", comment);
                    params.put("timeStamp", timeStamp);
                    return params;
                }
            };
            // Adding request to volley request queue
            AppController.getInstance().addToRequestQueue(jsonReq);
        }
    }

    public class UpdateMinusRecommend {
        private int commentID;

        public UpdateMinusRecommend(int commentID) {
            this.commentID = commentID;
        }

        // Pull JSON directly from the PHP JSON result
        public void updateMinusARecommendCount() {

            // making fresh volley request and getting json
            StringRequest jsonReq = new StringRequest(Request.Method.POST,
                    "http://idex.site88.net/updateMinusCommentRecommend.php", new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    //progressDialog.dismiss();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //progressDialog.dismiss();
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("commentID", Integer.toString(commentID));
                    return params;
                }
            };
            // Adding request to volley request queue
            AppController.getInstance().addToRequestQueue(jsonReq);
        }
    }

    public class RemoveRecommendVolley {
        private int commentID, userID;

        public RemoveRecommendVolley(int commentID, int userID) {
            this.commentID = commentID;
            this.userID = userID;
        }

        // Pull JSON directly from the PHP JSON result
        public void removeRecommend() {

            // making fresh volley request and getting json
            StringRequest jsonReq = new StringRequest(Request.Method.POST,
                    "http://idex.site88.net/deleteFromRecommendedList.php", new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    //progressDialog.dismiss();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //progressDialog.dismiss();
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("commentID", Integer.toString(commentID));
                    params.put("userID", Integer.toString(userID));
                    return params;
                }
            };
            // Adding request to volley request queue
            AppController.getInstance().addToRequestQueue(jsonReq);
        }
    }

    public class FetchCommentVolley {
        private CommentListAdapter commentListAdapter;
        private List<CommentItem> commentItems;
        private int postID, userID;

        public FetchCommentVolley(CommentListAdapter commentListAdapter, List<CommentItem> commentItems, int postID, int userID) {
            this.commentListAdapter = commentListAdapter;
            this.commentItems = commentItems;
            this.postID = postID;
            this.userID = userID;
        }

        // Pull JSON directly from the PHP JSON result
        public void fetchCommentVolley() {
            // making fresh volley request and getting json
            JsonObjectRequest jsonReq = new JsonObjectRequest(Request.Method.GET,
                    "http://idex.site88.net/fetchComments.php?postID=" + postID + "&userID=" + userID, null, new Response.Listener<JSONObject>() {

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
                                item.setPostId(feedObj.getInt("postID"));
                                item.setCommentID(feedObj.getInt("commentID"));
                                item.setName(name);
                                item.setUsername(feedObj.getString("username"));
                                item.setComment(feedObj.getString("comment"));
                                item.setProfilePic(feedObj.getString("userpic"));
                                item.setTimeStamp(feedObj.getString("date"));
                                try {
                                    item.setRecommended(feedObj.getInt("status"));
                                } catch (JSONException e) {
                                    item.setRecommended(0);
                                }
                                item.setRecommendTotalCount(feedObj.getInt("recommend"));
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

    public class UpdateRecommendVolley {
        private int commentID;

        public UpdateRecommendVolley(int commentID) {
            this.commentID = commentID;
        }

        // Pull JSON directly from the PHP JSON result
        public void updateRecommend() {

            // making fresh volley request and getting json
            StringRequest jsonReq = new StringRequest(Request.Method.POST,
                    "http://idex.site88.net/updateCommentRecommend.php", new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    System.out.println("added to database");
                    //progressDialog.dismiss();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //progressDialog.dismiss();
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("commentID", Integer.toString(commentID));
                    return params;
                }
            };
            // Adding request to volley request queue
            AppController.getInstance().addToRequestQueue(jsonReq);
        }
    }

    public class AddToCommentList {
        private int commentID;
        private int userID;

        public AddToCommentList(int commentID, int userID) {
            this.commentID = commentID;
            this.userID = userID;
        }

        // Pull JSON directly from the PHP JSON result
        public void addToCommentList() {

            // making fresh volley request and getting json
            StringRequest jsonReq = new StringRequest(Request.Method.POST,
                    "http://idex.site88.net/insertCommentList.php", new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    //progressDialog.dismiss();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //progressDialog.dismiss();
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("commentID", Integer.toString(commentID));
                    params.put("userID", Integer.toString(userID));

                    return params;
                }
            };
            // Adding request to volley request queue
            AppController.getInstance().addToRequestQueue(jsonReq);
        }
    }


}
