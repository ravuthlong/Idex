package phoenix.idex;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import phoenix.idex.ServerRequestCallBacks.NetworkConnectionCallBack;

/**
 * Created by Ravinder on 4/22/16.
 */
public class Util {
    private static final Util instance = new Util();
    private static final String TAG = Util.class.getSimpleName();

    public static Util getInstance() {
        return instance;
    }

    public void getInternetStatus(Context context, NetworkConnectionCallBack networkConnectionCallBack) {
        new InternetAccess(context, networkConnectionCallBack).execute();
    }

    private Util() {
    }

    public class InternetAccess extends AsyncTask<Void, Void, Boolean> {
        private Context context;
        NetworkConnectionCallBack networkConnectionCallBack;

        public InternetAccess(Context context, NetworkConnectionCallBack networkConnectionCallBack) {
            this.context = context;
            this.networkConnectionCallBack = networkConnectionCallBack;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

            if (networkInfo != null) {
                try {
                    HttpURLConnection urlc = (HttpURLConnection)
                            (new URL("http://clients3.google.com/generate_204")
                                    .openConnection());
                    urlc.setRequestProperty("User-Agent", "Android");
                    urlc.setRequestProperty("Connection", "close");
                    urlc.setConnectTimeout(1500);
                    urlc.connect();
                    return (urlc.getResponseCode() == 204 &&
                            urlc.getContentLength() == 0);
                } catch (IOException e) {
                    Log.e(TAG, "Error checking internet connection", e);
                }
            } else {
                Log.d(TAG, "No network available!");
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            networkConnectionCallBack.networkConnection(aBoolean);
        }
    }

    public  static void displayNoInternet(Context context) {
        Toast toast= Toast.makeText(context,
                "Not connected to the internet", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.show();

    }
}
