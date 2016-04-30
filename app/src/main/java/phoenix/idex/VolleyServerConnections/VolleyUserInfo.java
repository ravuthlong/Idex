package phoenix.idex.VolleyServerConnections;

import android.app.ProgressDialog;
import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import phoenix.idex.RecyclerViewFeed.MainRecyclerView.app.AppController;
import phoenix.idex.ServerRequestCallBacks.GetUserCallBack;
import phoenix.idex.ServerRequestCallBacks.JSONObjectCallBack;
import phoenix.idex.User;

/**
 * Created by Ravinder on 4/28/16.
 */
public class VolleyUserInfo {

    private ProgressDialog progressDialog;
    private Context context;

    public VolleyUserInfo(Context context) {
        this.context = context;
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");
    }

    public void updateUserInfo(User user) {
        progressDialog.show();
        new UpdateUserInfoVolley(user).updateUserInfo();
    }

    public void storeUserInfo(User user, GetUserCallBack userCallBack) {
        progressDialog.show();
        new StoreUserInfoVolley(user, userCallBack).storeUserInfo();
    }

    public void fetchPostStat(int userID, JSONObjectCallBack jsonObjectCallBack) {
        progressDialog.show();
        new FetchPostStatVolley(userID, jsonObjectCallBack).fetchPostStat();
    }

    public class FetchPostStatVolley {
        private int userID;
        private JSONObjectCallBack jsonObjectCallBack;

        public FetchPostStatVolley(int userID, JSONObjectCallBack jsonObjectCallBack) {
            this.userID = userID;
            this.jsonObjectCallBack = jsonObjectCallBack;
        }

        // Pull JSON directly from the PHP JSON result
        public void fetchPostStat() {

            CustomRequest jsObjRequest = new CustomRequest(Request.Method.GET,
                    "http://idex.site88.net/fetchUserPostStat.php?userID=" + userID,
                    null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    progressDialog.dismiss();
                    jsonObjectCallBack.returnedJSONObject(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    progressDialog.dismiss();
                    error.printStackTrace();
                }
            });

            AppController.getInstance().addToRequestQueue(jsObjRequest);
        }
    }


    public class StoreUserInfoVolley {
        private User user;
        private GetUserCallBack userCallBack;

        public StoreUserInfoVolley(User user, GetUserCallBack userCallBack) {
            this.user = user;
            this.userCallBack = userCallBack;
        }

        // Pull JSON directly from the PHP JSON result
        public void storeUserInfo() {

            Map<String, String> params = new HashMap<>();
            params.put("firstname", user.getFirstname());
            params.put("lastname", user.getLastname());
            params.put("email", user.getEmail());
            params.put("username", user.getUsername());
            params.put("password", user.getPassword());
            params.put("date", user.getTime());

            CustomRequest jsObjRequest = new CustomRequest(Request.Method.POST, "http://idex.site88.net/register.php",
                    params, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    progressDialog.dismiss();
                    int userID = 0;
                    try {
                        userID = response.getInt("id");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    userCallBack.done(new User(userID, user.getFirstname(), user.getLastname(), user.getEmail(),
                            user.getUsername(), user.getTime()));

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    progressDialog.dismiss();
                    error.printStackTrace();
                }
            });

            AppController.getInstance().addToRequestQueue(jsObjRequest);
        }
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

}
