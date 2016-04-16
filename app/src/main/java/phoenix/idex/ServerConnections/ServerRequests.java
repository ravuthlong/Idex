package phoenix.idex.ServerConnections;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Pair;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;

import phoenix.idex.Activities.MainActivity;
import phoenix.idex.ServerRequestCallBacks.FetchColumnAndValueCallBack;
import phoenix.idex.ServerRequestCallBacks.GetUserCallBack;
import phoenix.idex.ServerRequestCallBacks.PostExecutionCallBack;
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
    public void storeAPostInBackground(String post, int userID, String timeStamp) {
        progressDialog.show();
        new StoreAPostAsyncTask(post, userID, timeStamp).execute();
    }
    public void updateFillInBackground(int postID, int currentColumn) {
        new UpdateFill(postID, currentColumn).execute();
    }
    public void updateFillAndCancelKillInBackground(int postID, int currentColumn) {
        new UpdateFillAndCancelKill(postID, currentColumn).execute();
    }
    public void updateFillAndFillColumnInBackground(int postID, int columnToUpdate) {
        new UpdateFillAndCurrentColumn(postID, columnToUpdate).execute();
    }

    public void updateFillAndResetColumnInBackground(int postID, int currentColumn) {
        new UpdateFillAndResetColumn(postID, currentColumn).execute();
    }
    public void updateKillInBackground(int postID, int currentColumn) {
        new UpdateKill(postID, currentColumn).execute();
    }
    public void updateKillAndCancelFillInBackground(int postID, int currentColumn) {
        new UpdateKillAndCancelFill(postID, currentColumn).execute();
    }
    public void updateKillAndKillColumnInBackground(int postID, int currentColumn) {
        new UpdateKillAndCurrentColumn(postID, currentColumn).execute();
    }
    public void updateKillAndFloorColumnInBackground(int postID, int columnToUpdate) {
        new UpdateKillAndFloorColumn(postID, columnToUpdate).execute();
    }
    public void updateKillAndResetColumnInBackground(int postID, int currentColumn) {
        new UpdateKillAndResetColumn(postID, currentColumn).execute();
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
    public void minusFillInBackground(int postID) {
        new MinusFill(postID).execute();
    }
    public void minusKillInBackground(int postID) {
        new MinusKill(postID).execute();
    }

    public void fetchCurrentColumnInBackground(int postID, FetchColumnAndValueCallBack fetchColumnAndValueCallBack) {
        new FetchCurrentColumnAndValue(postID, fetchColumnAndValueCallBack).execute();
    }

    public void updateCurrentColumnInBackground(int postID, PostExecutionCallBack postExecutionCallBack) {
        new UpdateCurrentColumn(postID, postExecutionCallBack).execute();
    }

    public void updateValueInBackground(int postID, int value) {
        new UpdateValue(postID, value).execute();
    }

    public class FetchCurrentColumnAndValue extends AsyncTask<Void, Void, Pair> {
        private int postID;
        private FetchColumnAndValueCallBack fetchColumnAndValueCallBack;

        public FetchCurrentColumnAndValue(int postID, FetchColumnAndValueCallBack fetchColumnAndValueCallBack) {
            this.postID = postID;
            this.fetchColumnAndValueCallBack = fetchColumnAndValueCallBack;
        }

        @Override
        protected Pair doInBackground(Void... params) {

            HashMap<String, Object> userPost = new HashMap<>();
            userPost.put("postID", postID);

            JSONObject jObject = new JSONObject();
            int currentColumn = 0;
            int value = 0;
            Pair<Integer, Integer> pairValues = null;
            try {
                HttpRequest req = new HttpRequest("http://idex.site88.net/fetchCurrentColumnAndValue.php");
                jObject =  req.preparePost().withData(userPost).sendAndReadJSON();
                currentColumn = jObject.getInt("currentColumn");
                value = jObject.getInt("value");

                System.out.println("COLUMN : " + currentColumn);
                System.out.println("VALUE : " + value);

                pairValues = new Pair(currentColumn, value);

            }catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return pairValues;
        }

        @Override
        protected void onPostExecute(Pair pairValues) {
            super.onPostExecute(pairValues);
            fetchColumnAndValueCallBack.columnAndValueCallBack(pairValues);
        }
    }

    public class UpdateCurrentColumn extends AsyncTask<Void, Void, Void> {
        private int postID;
        private PostExecutionCallBack postExecutionCallBack;
        public UpdateCurrentColumn(int postID, PostExecutionCallBack postExecutionCallBack) {
            this.postID = postID;
            this.postExecutionCallBack = postExecutionCallBack;
        }

        @Override
        protected Void doInBackground(Void... params) {
            HashMap<String, Object> userPost = new HashMap<>();
            userPost.put("postID", postID);

            try {
                HttpRequest req = new HttpRequest("http://idex.site88.net/updateCurrentColumn.php");
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
            postExecutionCallBack.postExecution();
        }
    }

    public class UpdateValue extends AsyncTask<Void, Void, Void> {
        private int postID;
        private int value;

        public UpdateValue(int postID, int value) {
            this.postID = postID;
            this.value = value;
        }

        @Override
        protected Void doInBackground(Void... params) {
            HashMap<String, Object> userPost = new HashMap<>();
            userPost.put("postID", postID);
            userPost.put("value", value);

            try {
                HttpRequest req = new HttpRequest("http://idex.site88.net/updateValue.php");
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

    // Update fill AND current column to insert. Increment by 1 in the database.
    public class UpdateKillAndFloorColumn extends AsyncTask<Void, Void, Void> {

        private int postID;
        private int currentColumn;

        public UpdateKillAndFloorColumn(int postID, int currentColumn) {
            this.postID = postID;
            this.currentColumn = currentColumn;
        }

        @Override
        protected Void doInBackground(Void... params) {

            HashMap<String, Object> userPost = new HashMap<>();
            userPost.put("postID", postID);
            userPost.put("currentColumn", currentColumn);

            try {
                HttpRequest req = new HttpRequest("http://idex.site88.net/updateKillAndFloorColumn.php");
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
                HttpRequest req = new HttpRequest("http://idex.site88.net/updateKillAndKillColumn.php");
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

    public class UpdateKillAndResetColumn extends AsyncTask<Void, Void, Void> {

        private int postID;
        private int currentColumn;

        public UpdateKillAndResetColumn(int postID, int currentColumn) {
            this.postID = postID;
            this.currentColumn = currentColumn;
        }
        @Override
        protected Void doInBackground(Void... params) {

            HashMap<String, Object> userPost = new HashMap<>();
            userPost.put("postID", postID);
            userPost.put("currentColumn", currentColumn);

            try {
                HttpRequest req = new HttpRequest("http://idex.site88.net/updateKillAndResetColumn.php");
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

    public class UpdateFillAndResetColumn extends AsyncTask<Void, Void, Void> {

        private int postID;
        private int currentColumn;

        public UpdateFillAndResetColumn(int postID, int currentColumn) {
            this.postID = postID;
            this.currentColumn = currentColumn;
        }
        @Override
        protected Void doInBackground(Void... params) {

            HashMap<String, Object> userPost = new HashMap<>();
            userPost.put("postID", postID);
            userPost.put("currentColumn", currentColumn);

            try {
                HttpRequest req = new HttpRequest("http://idex.site88.net/updateFillAndResetColumn.php");
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
                HttpRequest req = new HttpRequest("http://idex.site88.net/updateFillAndFillColumn.php");
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

        private int postID;

        public MinusFill(int postID) {
            this.postID = postID;
        }

        @Override
        protected Void doInBackground(Void... params) {

            HashMap<String, Object> userPost = new HashMap<>();
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

        private int postID;

        public MinusKill(int postID) {
            this.postID = postID;
        }

        @Override
        protected Void doInBackground(Void... params) {

            HashMap<String, Object> userPost = new HashMap<>();
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
