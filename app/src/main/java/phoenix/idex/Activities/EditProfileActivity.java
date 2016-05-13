package phoenix.idex.Activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.params.BasicHttpParams;
import phoenix.idex.R;
import phoenix.idex.User;
import phoenix.idex.UserLocalStore;
import phoenix.idex.VolleyServerConnections.VolleyUserInfo;

public class EditProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView ivProfilePic;
    private EditText etChangeFirstName, etChangeLastName, etChangeEmail, etChangeUsername;
    private UserLocalStore userLocalStore;
    private Button bEditProfile, bChangePassword;
    private ProgressDialog progressDialog;
    private static final String SERVER_ADDRESS = "http://idex.site88.net/";
    private static final int RESULT_LOAD_IMAGE = 1;
    private static boolean isNewPhotoUploaded = false;
    private VolleyUserInfo volleyUserInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarEditProfile);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        bEditProfile = (Button) findViewById(R.id.bEditProfile);
        bChangePassword = (Button) findViewById(R.id.bChangePassword);
        ivProfilePic = (ImageView) findViewById(R.id.ivProfilePic);
        etChangeFirstName = (EditText) findViewById(R.id.etChangeFirstName);
        etChangeLastName = (EditText) findViewById(R.id.etChangeLastName);
        etChangeUsername = (EditText) findViewById(R.id.etChangeUsername);
        etChangeEmail = (EditText) findViewById(R.id.etChangeEmail);

        userLocalStore = new UserLocalStore(this);
        progressDialog = new ProgressDialog(this);

        etChangeFirstName.setText(userLocalStore.getLoggedInUser().getFirstname());
        etChangeLastName.setText(userLocalStore.getLoggedInUser().getLastname());
        etChangeUsername.setText(userLocalStore.getLoggedInUser().getUsername());
        etChangeEmail.setText(userLocalStore.getLoggedInUser().getEmail());

        // Get current Profile Pic
        new DownloadImage(userLocalStore.getLoggedInUser().getUserID()).execute();

        userLocalStore = new UserLocalStore(this);
        progressDialog = new ProgressDialog(this);
        volleyUserInfo = new VolleyUserInfo(this);
        progressDialog.setCancelable(false);

        ivProfilePic.setOnClickListener(this);
        bEditProfile.setOnClickListener(this);
        bChangePassword.setOnClickListener(this);
    }

    // Set the image from gallery onto ivProfilePic imageview
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null){
            Uri selectedImage = data.getData();
            ivProfilePic.setImageURI(selectedImage);
            // Update the new photo in the database
            new UploadImage(((BitmapDrawable) ivProfilePic.getDrawable()).getBitmap(), userLocalStore.getLoggedInUser().getUserID()).execute();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ivProfilePic:
                // Get the image from gallery
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);
                break;
            case R.id.bEditProfile:
                //isNewPhotoUploaded = true;
                //Bitmap image = ((BitmapDrawable) ivProfilePic.getDrawable()).getBitmap();
                //new UploadImage(image, userLocalStore.getLoggedInUser().getUsername()).execute();

                // Update user photo in the database
                User editUser = new User(userLocalStore.getLoggedInUser().getUserID(), etChangeFirstName.getText().toString(), etChangeLastName.getText().toString(),
                        etChangeEmail.getText().toString(), etChangeUsername.getText().toString());
                volleyUserInfo.updateUserInfo(editUser);

                // Update the shared-preference (local storage of current active user)
                //userLocalStore.clearUserData();
                userLocalStore.storeUserData(new User(userLocalStore.getLoggedInUser().getUserID(), etChangeFirstName.getText().toString(), etChangeLastName.getText().toString(),
                        etChangeEmail.getText().toString(), etChangeUsername.getText().toString()));
                UserLocalStore.allowRefresh = true;

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                dialogBuilder.setMessage("Changes saved");
                dialogBuilder.setPositiveButton("Ok", null);
                dialogBuilder.show();
                break;
            case R.id.bChangePassword:
                startActivity(new Intent(this, EditPasswordActivity.class));
                break;

        }
    }

    public static boolean isNewPhotoUploaded() {
        return isNewPhotoUploaded;
    }
    public static void setIsNewPhotoUploaded(boolean isNewPhoto) {
        isNewPhotoUploaded = isNewPhoto;
    }


    private cz.msebera.android.httpclient.params.HttpParams getHttpRequestParams(){
        cz.msebera.android.httpclient.params.HttpParams httpRequestParams = (cz.msebera.android.httpclient.params.HttpParams) new BasicHttpParams();
        cz.msebera.android.httpclient.params.HttpConnectionParams.setConnectionTimeout(httpRequestParams, 1000 * 30);
        cz.msebera.android.httpclient.params.HttpConnectionParams.setSoTimeout(httpRequestParams, 1000*30);
        return httpRequestParams;
    }

    // Download Profile_Pic
    private class DownloadImage extends AsyncTask<Void, Void, Bitmap>{
        int userID;
        public DownloadImage(int userID){
            this.userID = userID;
        }
        @Override
        protected Bitmap doInBackground(Void... params) {
            String url = SERVER_ADDRESS + "profile_pic/" + userID + ".JPG";

            try{
                URLConnection connection = new URL(url).openConnection();
                connection.setConnectTimeout(1000 * 30);
                connection.setReadTimeout(1000*30);

                return BitmapFactory.decodeStream((InputStream) connection.getContent(), null, null);

            }catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);

            if(bitmap != null){
                ivProfilePic.setImageBitmap(bitmap);
            }
            else{
                ivProfilePic.setImageDrawable(ContextCompat.getDrawable(EditProfileActivity.this, R.drawable.default_profile_pic));
            }
        }
    }

    private void UploadImageInBackground(Bitmap image, int userID) {
        new UploadImage(image, userID).execute();
    }




    // Upload new Profile Pic to Server
    private class UploadImage extends AsyncTask<Void,Void, Void> {
        Bitmap image;
        int userID;
        public UploadImage(Bitmap image, int userID){
            this.image = image;
            this.userID = userID;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 15, byteArrayOutputStream);
            String encodedImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);

            ArrayList<NameValuePair> dataToSend = new ArrayList<>();
            dataToSend.add(new BasicNameValuePair("image", encodedImage));
            dataToSend.add(new BasicNameValuePair("userID", Integer.toString(userID)));

            cz.msebera.android.httpclient.params.HttpParams httpRequestParams = getHttpRequestParams();

            HttpClient client = new DefaultHttpClient(httpRequestParams);
            HttpPost post = new HttpPost( SERVER_ADDRESS + "upload_profile_pic.php");

            try{
                post.setEntity(new UrlEncodedFormEntity(dataToSend));
                client.execute(post);
            }catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            // Delay progress dialog to give enough time for image to upload to the server
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();
                }
            }, 500);
            isNewPhotoUploaded = true;
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}