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
import phoenix.idex.ServerRequestCallBacks.GetUserCallBack;
import phoenix.idex.ServerRequestCallBacks.UserPostsCallBack;
import phoenix.idex.User;
import phoenix.idex.UserLocalStore;

/**
 * Created by Ravinder on 3/2/16.
 */
public class ServerRequests {
    private ProgressDialog progressDialog;
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
    public void storeAPostInBackground(String post, int userID, String timeStamp, int currentColumn) {
        progressDialog.show();
        new StoreAPostAsyncTask(post, userID, timeStamp, currentColumn).execute();
    }
    public void updateFillInBackground(int postID, int currentColumn) {
        new UpdateFill(postID, currentColumn).execute();
    }
    public void updateFillAndCancelKillInBackground(int postID, int currentColumn) {
        new UpdateFillAndCancelKill(postID, currentColumn).execute();
    }
    public void updateFillAndCurrentColumnInBackground(int postID, int currentColumn) {
        new UpdateFillAndCurrentColumn(postID, currentColumn).execute();
    }
    public void updateKillInBackground(int postID, int currentColumn) {
        new UpdateKill(postID, currentColumn).execute();
    }
    public void updateKillAndCancelFillInBackground(int postID, int currentColumn) {
        new UpdateKillAndCancelFill(postID, currentColumn).execute();
    }
    public void updateKillAndCurrentColumnInBackground(int postID, int currentColumn) {
        new UpdateKillAndCurrentColumn(postID, currentColumn).execute();
    }
    public void fetchOneUserPostsInBackground(int userID, UserPostsCallBack userPostsCallBack) {
        new FetchOneUserPosts(userID, userPostsCallBack).execute();
    }
    public void deleteAPostInBackground(int postID) {
        progressDialog.show();
        new DeleteAPostAsyncTask(postID).execute();
    }
    public void addToFillListInBackground(int userID, int postID) {
        new AddToFillList(userID, postID).execute();
    }
    public void addToKillListInBackground(int userID, int postID) {
        new AddToKillList(userID, postID).execute();
    }
    public void cancelClickInBackground(int userID, int postID) {
        new CancelClickAsyncTask(userID, postID).execute();
    }
    public void minusFillInBackground(int currentColumn, int postID) {
        new MinusFill(currentColumn, postID).execute();
    }
    public void minusKillInBackground(int currentColumn, int postID) {
        new MinusKill(currentColumn, postID).execute();
    }

    // Delete a post from user click activity list. For example, they cancelled a fill or a kill.
    public class CancelClickAsyncTask extends AsyncTask<Void, Void, Void> {

        private int postID;
        private int userID;

        public CancelClickAsyncTask(int userID, int postID) {
            this.postID = postID;
            this.userID = userID;
        }
        @Override
        protected Void doInBackground(Void... params) {

            HashMap<String, Object> userPost = new HashMap<>();
            userPost.put("postID", postID);
            userPost.put("userID", userID);

            try {
                HttpRequest req = new HttpRequest("http://idex.site88.net/deleteFromUserClickList.php");
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
        }
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
            userPostsCallBack.jsonString(JSONString);
        }
    }

    // Update kill of a unique post. Increment by 1 in the database.
    public class UpdateKill extends AsyncTask<Void, Void, Void> {

        private int postID;
        private int currentColumn;

        public UpdateKill(int postID, int currentColumn) {
            this.postID = postID;
            this.currentColumn = currentColumn;
        }

        @Override
        protected Void doInBackground(Void... params) {

            HashMap<String, Object> userPost = new HashMap<>();
            userPost.put("postID", postID);
            userPost.put("currentColumn", currentColumn);

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


    // Update kill AND cancel Fill
    public class UpdateKillAndCancelFill extends AsyncTask<Void, Void, Void> {

        private int postID;
        private int currentColumn;

        public UpdateKillAndCancelFill(int postID, int currentColumn) {
            this.postID = postID;
            this.currentColumn = currentColumn;
        }

        @Override
        protected Void doInBackground(Void... params) {

            HashMap<String, Object> userPost = new HashMap<>();
            userPost.put("postID", postID);
            userPost.put("currentColumn", currentColumn);

            try {
                HttpRequest req = new HttpRequest("http://idex.site88.net/updateKillCancelFill.php");
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

    // Update kill AND current column to insert. Increment by 1 in the database.
    public class UpdateKillAndCurrentColumn extends AsyncTask<Void, Void, Void> {

        private int postID;
        private int currentColumn;

        public UpdateKillAndCurrentColumn(int postID, int currentColumn) {
            this.postID = postID;
            this.currentColumn = currentColumn;
        }

        @Override
        protected Void doInBackground(Void... params) {

            HashMap<String, Object> userPost = new HashMap<>();
            userPost.put("postID", postID);
            userPost.put("currentColumn", currentColumn);

            try {
                HttpRequest req = new HttpRequest("http://idex.site88.net/updateKillAndCurrentColumn.php");
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
        private int currentColumn;

        public UpdateFill(int postID, int currentColumn) {
            this.postID = postID;
            this.currentColumn = currentColumn;
        }

        @Override
        protected Void doInBackground(Void... params) {

            HashMap<String, Object> userPost = new HashMap<>();
            userPost.put("postID", postID);
            userPost.put("currentColumn", currentColumn);

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

    // Update fill AND cancel kill
    public class UpdateFillAndCancelKill extends AsyncTask<Void, Void, Void> {

        private int postID;
        private int currentColumn;

        public UpdateFillAndCancelKill(int postID, int currentColumn) {
            this.postID = postID;
            this.currentColumn = currentColumn;
        }

        @Override
        protected Void doInBackground(Void... params) {

            HashMap<String, Object> userPost = new HashMap<>();
            userPost.put("postID", postID);
            userPost.put("currentColumn", currentColumn);

            try {
                HttpRequest req = new HttpRequest("http://idex.site88.net/updateFillCancelKill.php");
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


    // Update fill AND current column to insert. Increment by 1 in the database.
    public class UpdateFillAndCurrentColumn extends AsyncTask<Void, Void, Void> {

        private int postID;
        private int currentColumn;

        public UpdateFillAndCurrentColumn(int postID, int currentColumn) {
            this.postID = postID;
            this.currentColumn = currentColumn;
        }

        @Override
        protected Void doInBackground(Void... params) {

            HashMap<String, Object> userPost = new HashMap<>();
            userPost.put("postID", postID);
            userPost.put("currentColumn", currentColumn);

            try {
                HttpRequest req = new HttpRequest("http://idex.site88.net/updateFillAndCurrentColumn.php");
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
        private int currentColumn;

        public StoreAPostAsyncTask(String post, int userID, String timeStamp, int currentColumn) {
            this.post = post;
            this.userID = userID;
            this.timeStamp = timeStamp;
            this.currentColumn = currentColumn;
        }

        @Override
        protected Void doInBackground(Void... params) {

            HashMap<String, Object> userPost = new HashMap<>();
            userPost.put("post", post);
            userPost.put("userID", userID);
            userPost.put("timeStamp", timeStamp);
            userPost.put("currentColumn", currentColumn);

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

    public class AddToKillList extends AsyncTask<Void, Void, Void> {

        private int userID;
        private int postID;

        public AddToKillList(int userID, int postID) {
            this.postID = postID;
            this.userID = userID;
        }

        @Override
        protected Void doInBackground(Void... params) {

            HashMap<String, Object> userPost = new HashMap<>();
            userPost.put("userID", userID);
            userPost.put("postID", postID);

            try {
                HttpRequest req = new HttpRequest("http://idex.site88.net/insertKillList.php");
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
        }
    }

    // Update kill of a unique post. Increment by 1 in the database.
    public class AddToFillList extends AsyncTask<Void, Void, Void> {

        private int userID;
        private int postID;

        public AddToFillList(int userID, int postID) {
            this.postID = postID;
            this.userID = userID;
        }

        @Override
        protected Void doInBackground(Void... params) {

            HashMap<String, Object> userPost = new HashMap<>();
            userPost.put("userID", userID);
            userPost.put("postID", postID);

            try {
                HttpRequest req = new HttpRequest("http://idex.site88.net/insertFillList.php");
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
        }
    }

    // Minus Fill
    public class MinusFill extends AsyncTask<Void, Void, Void> {

        private int currentColumn;
        private int postID;

        public MinusFill(int currentColumn, int postID) {
            this.postID = postID;
            this.currentColumn = currentColumn;
        }

        @Override
        protected Void doInBackground(Void... params) {

            HashMap<String, Object> userPost = new HashMap<>();
            userPost.put("currentColumn", currentColumn);
            userPost.put("postID", postID);

            try {
                HttpRequest req = new HttpRequest("http://idex.site88.net/minusFill.php");
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
        }
    }

    // Minus Kill
    public class MinusKill extends AsyncTask<Void, Void, Void> {

        private int currentColumn;
        private int postID;

        public MinusKill(int currentColumn, int postID) {
            this.postID = postID;
            this.currentColumn = currentColumn;
        }

        @Override
        protected Void doInBackground(Void... params) {

            HashMap<String, Object> userPost = new HashMap<>();
            userPost.put("currentColumn", currentColumn);
            userPost.put("postID", postID);

            try {
                HttpRequest req = new HttpRequest("http://idex.site88.net/minusKill.php");
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
        }
    }
}
