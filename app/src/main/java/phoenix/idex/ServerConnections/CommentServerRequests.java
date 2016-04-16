package phoenix.idex.ServerConnections;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;

/**
 * Created by Ravinder on 4/9/16.
 */
public class CommentServerRequests {

    private Context context;
    private ProgressDialog progressDialog;

    public CommentServerRequests(Context context) {
        this.context = context;
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Processing");
        progressDialog.setMessage("Please Wait...");
    }

    public void storeACommentInBackground(int postID, int userID, String comment, String timeStamp) {
        progressDialog.show();
        new StoreCommentAsyncTask(postID, userID, comment, timeStamp).execute();
    }

    public class StoreCommentAsyncTask extends AsyncTask<Void, Void, Void> {

        private int postID;
        private int userID;
        private String comment;
        private String timeStamp;

        public StoreCommentAsyncTask(int postID, int userID, String comment, String timeStamp) {
            this.postID = postID;
            this.userID = userID;
            this.comment = comment;
            this.timeStamp = timeStamp;
        }

        @Override
        protected Void doInBackground(Void... params) {

            HashMap<String, Object> commentInfo = new HashMap<>();
            commentInfo.put("postID", postID);
            commentInfo.put("userID", userID);
            commentInfo.put("comment", comment);
            commentInfo.put("timeStamp", timeStamp);

            try {
                HttpRequest req = new HttpRequest("http://idex.site88.net/insertNewComment.php");
                req.preparePost().withData(commentInfo).send();

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
}
