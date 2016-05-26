package phoenix.idex.VolleyServerConnections;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import com.android.volley.DefaultRetryPolicy;
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

import phoenix.idex.Activities.CommentActivity;
import phoenix.idex.RecyclerViewFeed.CommentRecyclerView.adapter.CommentListAdapter;
import phoenix.idex.RecyclerViewFeed.CommentRecyclerView.data.CommentItem;
import phoenix.idex.RecyclerViewFeed.MainRecyclerView.app.AppController;
import phoenix.idex.ServerRequestCallBacks.JSONObjectCallBack;
import phoenix.idex.ServerRequestCallBacks.PostExecutionCallBack;
import phoenix.idex.UserLocalStore;

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

    public void fetchCommentVolley(LinearLayout linearLayout, RecyclerView recyclerView,
                                   CommentListAdapter commentListAdapter, List<CommentItem> commentItems, int postID, int userID) {
        progressDialog.show();
        new FetchCommentVolley(linearLayout, recyclerView, commentListAdapter, commentItems, postID, userID).fetchCommentVolley();
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
        progressDialog.show();
        new StoreAComment(postID, userID, comment, timeStamp, postExecutionCallBack).storeAComment();
    }
    public void deleteAComment(int commentID) {
        progressDialog.show();
        new DeleteCommentVolley(commentID).deleteCommentVolley();
    }
    public void fetchAUniquePost(int userID, int postID, JSONObjectCallBack jsonObjectCallBack) {
        new FetchUniquePost(userID, postID, jsonObjectCallBack).fetchAUniquePost();
    }
    public void updateAComment(int commentID, String newCommnet) {
        new UpdateAComment(commentID, newCommnet).updateAComment();
    }

    public class UpdateAComment {
        private int commentID;
        private String newComment;

        public UpdateAComment(int commentID, String newComment) {
            this.commentID = commentID;
            this.newComment = newComment;
        }

        public void updateAComment() {

            // making fresh volley request and getting json
            StringRequest jsonReq = new StringRequest(Request.Method.POST,
                    "http://idex.site88.net/updateComment.php", new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    progressDialog.dismiss();
                    context.startActivity(new Intent(context, CommentActivity.class));
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
                    params.put("commentID", Integer.toString(commentID));
                    params.put("newComment", newComment);
                    return params;
                }
            };
            // Adding request to volley request queue
            AppController.getInstance().addToRequestQueue(jsonReq);
        }
    }

    public class FetchUniquePost {
        private int userID, postID;
        private JSONObjectCallBack jsonObjectCallBack;

        public FetchUniquePost(int userID, int postID, JSONObjectCallBack jsonObjectCallBack) {
            this.userID = userID;
            this.postID = postID;
            this.jsonObjectCallBack = jsonObjectCallBack;
        }

        // Populate FeedItem Array with JSONArray from PHP
        public void readJSON(JSONObject response) {
            jsonObjectCallBack.returnedJSONObject(response);

            /*
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


                    //feedItems.add(item);
                }


            } catch (JSONException e) {
                //spinner.setVisibility(View.GONE);
                e.printStackTrace();
            }*/
        }

        public void fetchAUniquePost() {

            UserLocalStore.visitCounter++;
            // making fresh volley request and getting json
            JsonObjectRequest jsonReq = new JsonObjectRequest(Request.Method.GET,
                    "http://idex.site88.net/fetchOneUniquePost.php?userID=" + userID + "&postID=" + postID
                    , null, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    if (response != null) {
                        readJSON(response);
                    } else {
                        //spinner.setVisibility(View.GONE);
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    //spinner.setVisibility(View.GONE);
                }
            });

            // Adding request to volley request queue
            AppController.getInstance().addToRequestQueue(jsonReq);
        }
    }

    public class DeleteCommentVolley {
        private int commentID;

        public DeleteCommentVolley(int commentID) {
            this.commentID = commentID;
        }

        // Pull JSON directly from the PHP JSON result
        public void deleteCommentVolley() {
            // making fresh volley request and getting json
            StringRequest jsonReq = new StringRequest(Request.Method.POST,
                    "http://idex.site88.net/deleteAComment.php", new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {
                    progressDialog.dismiss();
                    context.startActivity(new Intent(context, CommentActivity.class));
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
                    params.put("commentID", Integer.toString(commentID));
                    return params;
                }
            };
            // Adding request to volley request queue
            AppController.getInstance().addToRequestQueue(jsonReq);
        }
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
                    params.put("postID", Integer.toString(postID));
                    params.put("userID", Integer.toString(userID));
                    params.put("comment", comment);
                    params.put("timeStamp", timeStamp);
                    return params;
                }
            };

            // Set max retry to 0 to avoid duplicate comments.
            jsonReq.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

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
        private RecyclerView recyclerView;
        private LinearLayout linearLayout;

        public FetchCommentVolley(LinearLayout linearLayout, RecyclerView recyclerView, CommentListAdapter commentListAdapter,
                                  List<CommentItem> commentItems, int postID, int userID) {
            this.commentListAdapter = commentListAdapter;
            this.commentItems = commentItems;
            this.postID = postID;
            this.userID = userID;
            this.recyclerView = recyclerView;
            this.linearLayout = linearLayout;
        }

        // Pull JSON directly from the PHP JSON result
        public void fetchCommentVolley() {
            // making fresh volley request and getting json
            JsonObjectRequest jsonReq = new JsonObjectRequest(Request.Method.GET,
                    "http://idex.site88.net/fetchComments.php?postID=" + postID + "&userID=" +
                            userID, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                        try {
                            // ISSUE OCCURS WHEN DELETE COMMENT AND NO COMMENT LEFT...
                            JSONArray feedArray = response.getJSONArray("feed");
                            JSONObject feedObj = (JSONObject) feedArray.get(0);
                            int result = feedObj.getInt("success");
                            if (result == 1) {
                                recyclerView.setVisibility(View.VISIBLE);
                                linearLayout.setVisibility(View.GONE);

                                for (int i = 0; i < feedArray.length(); i++) {
                                    feedObj = (JSONObject) feedArray.get(i);

                                    CommentItem item = new CommentItem();
                                    String name;
                                    name = feedObj.getString("firstname") + " " + feedObj.getString("lastname");
                                    item.setPostId(feedObj.getInt("postID"));
                                    item.setCommentID(feedObj.getInt("commentID"));
                                    item.setName(name);
                                    item.setUsername(feedObj.getString("username"));
                                    String comment = feedObj.getString("comment");
                                    comment = comment.replace("\\", "");
                                    item.setComment(comment);
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
                                // notify data changes to list adapter
                                commentListAdapter.notifyDataSetChanged();
                                //spinner.setVisibility(View.GONE);
                            } else {
                                // There is no comment. Hide comment list of recyclet view and display no comment
                                recyclerView.setVisibility(View.GONE);
                                linearLayout.setVisibility(View.VISIBLE);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        progressDialog.dismiss();
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
