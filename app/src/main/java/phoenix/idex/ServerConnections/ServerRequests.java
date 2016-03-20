package phoenix.idex.ServerConnections;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;

import phoenix.idex.MainActivity;
import phoenix.idex.User;
import phoenix.idex.UserLocalStore;
import phoenix.idex.UserPostsCallBack;

/**
 * Created by Ravinder on 3/2/16.
 */
public class ServerRequests {
    private ProgressDialog progressDialog;
    public static final int CONNECTION_TIMEOUT = 1000 * 15;
    private Context context;

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

    public void updateFillInBackground(int postID) {
        new UpdateFill(postID).execute();
    }

    public void updateKillInBackground(int postID) {
        new UpdateKill(postID).execute();
    }
    public void fetchOneUserPostsInBackground(int userID, UserPostsCallBack userPostsCallBack) {
        progressDialog.show();
        new FetchOneUserPosts(userID, userPostsCallBack).execute();
    }

    public void deleteAPostInBackground(int postID) {
        progressDialog.show();
        new DeleteAPostAsyncTask(postID).execute();
    }

    // Fetch all the posts from a unique logged in user. JSON string of data will be returned.
    public class FetchOneUserPosts extends AsyncTask<Void, Void, String> {

        private int userID;
        private UserPostsCallBack userPostsCallBack;

        public FetchOneUserPosts(int userID, UserPostsCallBack userPostsCallBack) {
            this.userID = userID;
            this.userPostsCallBack = userPostsCallBack;
        }

        @Override
        protected String doInBackground(Void... params) {

            HashMap<String, Object> userPost = new HashMap<>();
            userPost.put("userID", userID);
            String JSONPost = "";

            JSONObject jObject = new JSONObject();

            try {
                HttpRequest req = new HttpRequest("http://idex.site88.net/fetchOneUserPosts.php");
                JSONPost = req.preparePost().withData(userPost).sendAndReadString();

            }catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();

            }
            return JSONPost;
        }
        @Override
        protected void onPostExecute(String JSONString){
            super.onPostExecute(JSONString);
            progressDialog.dismiss();
            userPostsCallBack.jsonString(JSONString);
        }
    }

    // Update kill of a unique post. Increment by 1 in the database.
    public class UpdateKill extends AsyncTask<Void, Void, Void> {

        private int postID;

        public UpdateKill(int postID) {
            this.postID = postID;
        }

        @Override
        protected Void doInBackground(Void... params) {

            HashMap<String, Object> userPost = new HashMap<>();
            userPost.put("postID", postID);

            JSONObject jObject = new JSONObject();

            try {
                HttpRequest req = new HttpRequest("http://idex.site88.net/updateKill.php");
                req.preparePost().withData(userPost).send();

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
        }
    }

    // Update fill of a unique post. Increment by 1 in the database.
    public class UpdateFill extends AsyncTask<Void, Void, Void> {

        private int postID;

        public UpdateFill(int postID) {
            this.postID = postID;
        }

        @Override
        protected Void doInBackground(Void... params) {

            HashMap<String, Object> userPost = new HashMap<>();
            userPost.put("postID", postID);

            JSONObject jObject = new JSONObject();

            try {
                HttpRequest req = new HttpRequest("http://idex.site88.net/updateFill.php");
                req.preparePost().withData(userPost).send();

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
        }
    }

    // Delete a post
    public class DeleteAPostAsyncTask extends AsyncTask<Void, Void, Void> {

        private int postID;

        public DeleteAPostAsyncTask(int postID) {
            this.postID = postID;
        }

        @Override
        protected Void doInBackground(Void... params) {

            HashMap<String, Object> userPost = new HashMap<>();
            userPost.put("postID", postID);

            try {
                HttpRequest req = new HttpRequest("http://idex.site88.net/deleteAPost.php");
                req.preparePost().withData(userPost).send();
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
            UserLocalStore.allowRefresh = true;
            context.startActivity(new Intent(context, MainActivity.class));
        }
    }

    // Store a new post into the database, associating the the userID and time posted
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

            try {
                HttpRequest req = new HttpRequest("http://idex.site88.net/insertnewpost.php");
                req.preparePost().withData(userPost).send();
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
        }
    }

    // Signed up will be stored in the database. User type will be returned for UserLocalStore shared preference purpose.
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
                    //UserLocalStore.isUserLoggedIn = true;
                    returnedUser = new User(userID, user.getFirstname(), user.getLastname(), user.getEmail(),
                            user.getUsername());
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

    // When user logs in, get their information back. User type will be returned for UserLocalStore shared preference purpose.
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
                   //UserLocalStore.isUserLoggedIn = true;
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
