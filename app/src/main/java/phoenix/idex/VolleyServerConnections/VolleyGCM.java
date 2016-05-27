package phoenix.idex.VolleyServerConnections;

import android.app.ProgressDialog;
import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import phoenix.idex.RecyclerViewFeed.MainRecyclerView.app.AppController;

/**
 * Created by Ravinder on 5/26/16.
 */
public class VolleyGCM {
    private ProgressDialog progressDialog;
    private Context context;

    public VolleyGCM(Context context) {
        this.context = context;
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");
    }

    public void storeAToken(String token) {
        progressDialog.show();
        new StoreATokenVolley(token).storeAToken();
    }

    public void sendNotification(String message) {
        progressDialog.show();
        new SendNotificationVolley(message).sendNotification();
    }

    public void updateGCMToken(int userID, String newToken) {
        new UpdateTokenVolley(userID, newToken).updateToken();
    }

    public class UpdateTokenVolley {
        private int userID;
        private String newToken;

        public UpdateTokenVolley(int userID, String newToken) {
            this.userID = userID;
            this.newToken = newToken;
        }

        // Pull JSON directly from the PHP JSON result
        public void updateToken() {

            Map<String, String> params = new HashMap<>();
            params.put("userID", Integer.toString(userID));
            params.put("newToken", newToken);

            CustomRequest jsObjRequest = new CustomRequest(Request.Method.POST, "http://idex.site88.net/updateGCMToken.php",
                    params, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            });
            AppController.getInstance().addToRequestQueue(jsObjRequest);
        }
    }


    public class StoreATokenVolley {
        private String token;

        public StoreATokenVolley(String token) {
            this.token = token;
        }

        // Pull JSON directly from the PHP JSON result
        public void storeAToken() {

            // making fresh volley request and getting json
            StringRequest jsonReq = new StringRequest(Request.Method.POST,
                    "http://idex.site88.net/gcm.php", new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    progressDialog.dismiss();
                    //context.startActivity(new Intent(context, MainActivity.class));
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
                    params.put("tokenID", token);

                    return params;
                }
            };
            // Adding request to volley request queue
            AppController.getInstance().addToRequestQueue(jsonReq);
        }
    }

    public class SendNotificationVolley {
        private String message;

        public SendNotificationVolley(String message) {
            this.message = message;
        }

        // Pull JSON directly from the PHP JSON result
        public void sendNotification() {

            // making fresh volley request and getting json
            StringRequest jsonReq = new StringRequest(Request.Method.POST,
                    "http://idex.site88.net/sendNotification.php", new Response.Listener<String>() {
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
                    params.put("message", message);

                    return params;
                }
            };
            // Adding request to volley request queue
            AppController.getInstance().addToRequestQueue(jsonReq);
        }
    }
}
