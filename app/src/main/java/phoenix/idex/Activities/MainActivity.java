package phoenix.idex.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.ArrayList;
import java.util.List;

import phoenix.idex.ButtonClickedSingleton;
import phoenix.idex.Fragments.AboutFragment;
import phoenix.idex.Fragments.LoginActivityFragment;
import phoenix.idex.Fragments.PostListFragment;
import phoenix.idex.GoogleCloudMessaging.GCMRegistrationIntentService;
import phoenix.idex.R;
import phoenix.idex.SlidingDrawer.ItemSlideMenu;
import phoenix.idex.SlidingDrawer.SlidingMenuAdapter;
import phoenix.idex.UserLocalStore;
import phoenix.idex.VolleyServerConnections.VolleyGCM;

public class MainActivity extends AppCompatActivity {

    private List<ItemSlideMenu> itemList;
    private List<Fragment> fragmentList;
    private SlidingMenuAdapter adapter;
    public static ListView listView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private int currentPos;
    private Toolbar toolbar;
    public static RelativeLayout rLayoutMain;
    private FragmentManager fragmentManager;
    public static boolean isMainShown = false;
    private UserLocalStore userLocalStore;
    private ButtonClickedSingleton clickActivity = ButtonClickedSingleton.getInstance();
    private BroadcastReceiver registrationBroadcastReceiver;
    private VolleyGCM volleyGCM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slidingdrawer);
        userLocalStore = new UserLocalStore(this);
        volleyGCM = new VolleyGCM(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        fragmentManager = getSupportFragmentManager();
        listView = (ListView) findViewById(R.id.listViewMain);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayoutMain);
        rLayoutMain = (RelativeLayout) findViewById(R.id.rLayoutMain);

        // If user logged in before, keep them logged in
        if (!userLocalStore.getLoggedInUser().getUsername().equals("")) {
            UserLocalStore.isUserLoggedIn = true;
            UserLocalStore.allowRefresh = true;
            clickActivity.setRollClicked();
            setUpLoggedInDrawerList();
            setUpLoggedInFragments();
            screenStartUpState();
            drawerListViewLoggedInUserListener();
            toggleListener();
        } else  {
            setUpDrawerList();
            setUpFragments();
            screenStartUpState();
            drawerListViewListener();
            toggleListener();
        }


        // Check status of google play in the device
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
        if (ConnectionResult.SUCCESS != resultCode) {
            // Check the type of error
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                Toast.makeText(getApplicationContext(), "Google play service is not enabled on this device ", Toast.LENGTH_SHORT).show();
                // So notify
                GooglePlayServicesUtil.showErrorNotification(resultCode, getApplicationContext());
            } else {
                Toast.makeText(getApplicationContext(), "Device doesn't support google play service ", Toast.LENGTH_SHORT).show();
            }
        } else {
            /*
             * Start service for registering GCM
             */
            Intent intent = new Intent(this, GCMRegistrationIntentService.class);
            startService(intent);

        }

        registrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Check the type of intent filter

                if (intent.getAction().endsWith(GCMRegistrationIntentService.REGISTRATION_SUCCESS)) {
                    // Registration success
                    String deviceToken = intent.getStringExtra("token");

                    // insert to database
                    volleyGCM.storeAToken(deviceToken);

                    // If the current logged in device doesn't match user's database token, update it
                    if ((!deviceToken.equals(userLocalStore.getLoggedInUser().getToken())) && UserLocalStore.isUserLoggedIn) {

                        // Update to the logged in device's token for push notification
                        volleyGCM.updateGCMToken(userLocalStore.getLoggedInUser().getUserID(), deviceToken);
                    }

                    // if user is logged in, check to update their token

                    //Toast.makeText(getApplicationContext(), "GCM token " + deviceToken, Toast.LENGTH_SHORT).show();
                } else if (intent.getAction().endsWith(GCMRegistrationIntentService.REGISTRATION_ERROR)) {
                    // Registration error
                    Toast.makeText(getApplicationContext(), "GCM registration error ", Toast.LENGTH_SHORT).show();
                } else {
                    // Tobe define
                }
            }
        };


    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v("MainActivity", "onResume");
        LocalBroadcastManager.getInstance(this).registerReceiver(registrationBroadcastReceiver,
                new IntentFilter(GCMRegistrationIntentService.REGISTRATION_SUCCESS));
        LocalBroadcastManager.getInstance(this).registerReceiver(registrationBroadcastReceiver,
                new IntentFilter(GCMRegistrationIntentService.REGISTRATION_ERROR));
    }

    @Override
    protected void onPause() {
        // Unregister broadcast receiver

        super.onPause();
        Log.v("MainActivity", "onPause");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(registrationBroadcastReceiver);
    }

    public static int getThemeAttributeDimensionSize(Context context, int attr)
    {
        TypedArray a = null;
        try{
            a = context.getTheme().obtainStyledAttributes(new int[] { attr });
            return a.getDimensionPixelSize(0, 0);
        }finally{
            if(a != null){
                a.recycle();
            }
        }
    }

    // Listens to when drawer navigation is opened or closed.
    // Disabled menu items on action bar
    private void toggleListener() {
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.drawerOpened, R.string.drawerClosed) {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
            }
        };
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
    }

    // Add items to the drawer list for navigation drawer
    private void setUpDrawerList() {
        itemList = new ArrayList<>();

        itemList.add(new ItemSlideMenu("Roll"));
        itemList.add(new ItemSlideMenu("About"));
        itemList.add(new ItemSlideMenu("Log In"));


        adapter = new SlidingMenuAdapter(this, itemList);
        listView.setAdapter(adapter);
    }

    // Set up the fragments
    private void setUpFragments() {
        fragmentList = new ArrayList<>();

        fragmentList.add(new PostListFragment());
        fragmentList.add(new AboutFragment());
        fragmentList.add(new LoginActivityFragment());

    }

    // Add items to the drawer list for navigation drawer
    private void setUpLoggedInDrawerList() {
        itemList = new ArrayList<>();

        itemList.add(new ItemSlideMenu("Roll"));
        //itemList.add(new ItemSlideMenu("Profile"));
        itemList.add(new ItemSlideMenu("About"));
        itemList.add(new ItemSlideMenu("Log Out"));

        adapter = new SlidingMenuAdapter(this, itemList);
        listView.setAdapter(adapter);
    }

    // Set up the fragments
    private void setUpLoggedInFragments() {
        fragmentList = new ArrayList<>();

        fragmentList.add(new PostListFragment());
        //fragmentList.add(new TabFragment());
        fragmentList.add(new AboutFragment());
        fragmentList.add(new PostListFragment());
    }

    // Start up state
    //Title Idex, with closed navigation drawer and default fragment 1
    private void screenStartUpState() {
        isMainShown = true;
        setTitle("");
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.rLayoutMain, fragmentList.get(0)).addToBackStack(null).commit();
        listView.setItemChecked(0, true);
        drawerLayout.closeDrawer(listView);
    }

    // Fragments to be displayed based on user selection from navigation drawer
    private void drawerListViewLoggedInUserListener() {
        // Handle click
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                currentPos = position;

                // Sign out option
                if (position == 2) {
                    currentPos = 0;
                    Toast.makeText(MainActivity.this, "Logged Out", Toast.LENGTH_SHORT).show();
                    // Erase local history of logged in user
                    clickActivity.cancelClicks();
                    userLocalStore.clearUserData();
                    UserLocalStore.visitCounter = 0;
                    UserLocalStore.isUserLoggedIn = false;
                    setUpDrawerList();
                    setUpFragments();
                    screenStartUpState();
                    drawerListViewListener();
                } else {
                    if (position == 0) {
                        clickActivity.cancelClicks();
                        clickActivity.setRollClicked();
                        isMainShown = true;
                        setTitle("");
                    } else {
                        isMainShown = false;
                        //setTitle(itemList.get(position).getTitle());
                    }
                    drawerLayout.closeDrawer(listView);
                    // Set item to selected
                    listView.setItemChecked(position, true);
                }
                // Delay to avoid lag between navigation drawer items
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        fragmentManager.beginTransaction().replace(R.id.rLayoutMain,
                                fragmentList.get(currentPos)).commit();
                    }
                }, 270);
            }
        });
    }

    // Fragments to be displayed based on user selection from navigation drawer
    private void drawerListViewListener() {
        // Handle click
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                currentPos = position;
                if (position == 0) {
                    isMainShown = true;
                    setTitle("");
                } else {
                    isMainShown = false;
                    //setTitle(itemList.get(position).getTitle());
                }
                drawerLayout.closeDrawer(listView);
                // Set item to selected
                listView.setItemChecked(position, true);

                // Delay to avoid lag between navigation drawer items
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        fragmentManager.beginTransaction().replace(R.id.rLayoutMain,
                                fragmentList.get(currentPos)).commit();
                    }
                }, 270);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync it based on if navigation drawer is selected or not.
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onBackPressed() {
        if (isMainShown) {
            finish();
        } else {
            listView.setItemChecked(0, true);
            setTitle(itemList.get(0).getTitle());
            fragmentManager.beginTransaction().replace(R.id.rLayoutMain, fragmentList.get(0)).commit();
            isMainShown = true;
        }
    }
}
