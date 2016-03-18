package phoenix.idex.ServerConnections;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;

import phoenix.idex.User;
import phoenix.idex.UserLocalStore;

/**
 * Created by Ravinder on 3/2/16.
 */
public class ServerRequests {
    private ProgressDialog progressDialog;
    public static final int CONNECTION_TIMEOUT = 1000 * 15;
    Context context;

    public ServerRequests(Context context) {
        this.context = context;
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Processing");
        progressDialog.setMessage("Please Wait...");
    }

    public void storeUserDataInBackground(User user, GetUserCallBack userCallBack) {
        progressDialog.show();
        new StoreUserDataAsyncTask(user, userCallBack).execute();
    }

    public void logUserInDataInBackground(User user, GetUserCallBack userCallBack) {
        progressDialog.show();
        new FetchUserDataAsyncTask(user, userCallBack).execute();
    }

    public void storeAPostInBackground(String post, int userID, String timeStamp) {
        progressDialog.show();
        new StoreAPostAsyncTask(post, userID, timeStamp).execute();
    }

    public class StoreAPostAsyncTask extends AsyncTask<Void, Void, Void> {

        private String post;
        private int userID;
        private String timeStamp;

        public StoreAPostAsyncTask(String post, int userID, String timeStamp) {
            this.post = post;
            this.userID = userID;
            this.timeStamp = timeStamp;
        }

        @Override
        protected Void doInBackground(Void... params) {

            HashMap<String, Object> userPost = new HashMap<>();
            userPost.put("post", post);
            userPost.put("userID", userID);
            userPost.put("timeStamp", timeStamp);


            JSONObject jObject = new JSONObject();

            try {
                HttpRequest req = new HttpRequest("http://idex.site88.net/insertnewpost.php");
                jObject = req.preparePost().withData(userPost).sendAndReadJSON();

            } catch (JSONException e) {
                e.printStackTrace();
            }catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();

            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
            Toast.makeText(context, "Post sent to the database..", Toast.LENGTH_SHORT).show();

        }
    }

    public class StoreUserDataAsyncTask extends AsyncTask<Void, Void, User> {

        private User user;
        private GetUserCallBack userCallBack;

        public StoreUserDataAsyncTask(User user, GetUserCallBack userCallBack) {
            this.user = user;
            this.userCallBack = userCallBack;
        }

        @Override
        protected User doInBackground(Void... params) {
            User returnedUser = null;

            HashMap<String, Object> userInfo = new HashMap<>();
            userInfo.put("firstname", user.getFirstname());
            userInfo.put("lastname", user.getLastname());
            userInfo.put("email", user.getEmail());
            userInfo.put("username", user.getUsername());
            userInfo.put("password", user.getPassword());

            JSONObject jObject = new JSONObject();

            try {
                HttpRequest req = new HttpRequest("http://idex.site88.net/register.php");
                jObject = req.preparePost().withData(userInfo).sendAndReadJSON();

                if(jObject.getString("user").equals("")) {
                    // No user returned
                    returnedUser = null;
                } else {

                    int userID = jObject.getInt("id");
                    UserLocalStore.isUserLoggedIn = true;
                    returnedUser = new User(userID, user.getFirstname(), user.getLastname(), user.getEmail(),
                            user.getUsername());

                    System.out.println("!!!!!!!!!!!!!!!!!!USER WAS INSERTED AT LOCATION " + userID);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();

            }
            return returnedUser;
        }
        @Override
        protected void onPostExecute(User returnedUser) {
            super.onPostExecute(returnedUser);
            progressDialog.dismiss();
            userCallBack.done(returnedUser);
        }
    }

    public class FetchUserDataAsyncTask extends AsyncTask<Void, Void, User> {

        private User user;
        private GetUserCallBack userCallBack;

        public FetchUserDataAsyncTask(User user, GetUserCallBack userCallBack) {
            this.user = user;
            this.userCallBack = userCallBack;
        }

        @Override
        protected User doInBackground(Void... params) {


            User returnedUser = null;

            HashMap<String, Object> userInfo = new HashMap<>();
            userInfo.put("username", user.getUsername());
            userInfo.put("password", user.getPassword());

            JSONObject jObject = new JSONObject();

            try{

                HttpRequest req = new HttpRequest("http://idex.site88.net/login.php");
                jObject = req.preparePost().withData(userInfo).sendAndReadJSON();

                if(jObject.getString("username").equals("")){
                    // No user returned
                    returnedUser = null;

                }else{
                   UserLocalStore.isUserLoggedIn = true;
                    // Get the user details
                    int userID = jObject.getInt("userID");
                    String firstname = jObject.getString("firstname");
                    String lastname = jObject.getString("lastname");
                    String email = jObject.getString("email");
                    String username = jObject.getString("username");

                    returnedUser = new User(userID, firstname, lastname, email, username);
                }
            }catch(Exception e){
                e.printStackTrace();
            }

            return returnedUser;
        }
        @Override
        protected void onPostExecute(User returnedUser){
            super.onPostExecute(returnedUser);
            progressDialog.dismiss();
            userCallBack.done(returnedUser);
        }
    }
}
